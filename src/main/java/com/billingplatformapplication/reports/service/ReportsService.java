package com.billingplatformapplication.reports.service;

import com.billingplatformapplication.billing.dto.BillingCalculationResultDto;
import com.billingplatformapplication.billing.service.BillingCalculationService;
import com.billingplatformapplication.client.repository.ClientRepository;
import com.billingplatformapplication.preinvoices.entity.PreInvoiceEntity;
import com.billingplatformapplication.preinvoices.repository.PreInvoiceRepository;
import com.billingplatformapplication.profitability.service.ProfitabilityService;
import com.billingplatformapplication.reports.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportsService {

    private final BillingCalculationService           billingCalculationService;
    private final ClientRepository                    clientRepository;
    private final PreInvoiceRepository                preInvoiceRepository;
    private final ProfitabilityService                profitabilityService;

    private static final String[] MONTH_NAMES = {
            "", "Enero","Febrero","Marzo","Abril","Mayo","Junio",
            "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
    };

    // ---- Reporte mensual (mantiene el original) ----
    @Transactional(readOnly = true)
    public PeriodSummaryDto periodSummary(int year, int month) {
        List<BillingCalculationResultDto> results = clientRepository.findByActiveTrue()
                .stream()
                .map(c -> billingCalculationService.calculateBilling(c.getId(), year, month))
                .filter(r -> !r.getLines().isEmpty())
                .collect(Collectors.toList());

        BigDecimal grandTotal = results.stream()
                .map(BillingCalculationResultDto::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PeriodSummaryDto.builder()
                .billingYear(year).billingMonth(month)
                .clientCount(results.size())
                .grandTotal(grandTotal)
                .clientResults(results)
                .build();
    }

    // ---- Reporte individual por cliente y año (solo pre-facturas APPROVED) ----
    @Transactional(readOnly = true)
    public ClientAnnualReportDto clientAnnualReport(UUID clientId, int year) {

        var client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        List<PreInvoiceEntity> invoices =
                preInvoiceRepository.findApprovedByClientAndYear(clientId, year);

        // Meses con datos reales
        List<ClientMonthDetailDto> months = invoices.stream()
                .map(pi -> ClientMonthDetailDto.builder()
                        .month(pi.getBillingMonth())
                        .monthName(MONTH_NAMES[pi.getBillingMonth()])
                        .invoiceNumber(pi.getInvoiceNumber())
                        .subtotal(pi.getSubtotal())
                        .noveltyDiscounts(pi.getTotalNoveltyDiscounts())
                        .taxAmount(pi.getTaxAmount())
                        .total(pi.getTotalAmount())
                        .build())
                .collect(Collectors.toList());

        BigDecimal annualSubtotal  = sum(invoices, PreInvoiceEntity::getSubtotal);
        BigDecimal annualDiscounts = sum(invoices, PreInvoiceEntity::getTotalNoveltyDiscounts);
        BigDecimal annualTax       = sum(invoices, PreInvoiceEntity::getTaxAmount);
        BigDecimal annualTotal     = sum(invoices, PreInvoiceEntity::getTotalAmount);
        BigDecimal monthlyAvg      = invoices.isEmpty() ? BigDecimal.ZERO
                : annualTotal.divide(new BigDecimal(invoices.size()), 0, RoundingMode.HALF_UP);

        // Rentabilidad del cliente
        BigDecimal annualCost   = BigDecimal.ZERO;
        BigDecimal annualMargin = BigDecimal.ZERO;
        BigDecimal marginPct    = BigDecimal.ZERO;

        try {
            var profitLines = profitabilityService.calculateByClient(clientId);
            BigDecimal monthlyCost = profitLines.stream()
                    .map(p -> p.totalCost())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            annualCost   = monthlyCost.multiply(new BigDecimal(invoices.size()));
            annualMargin = annualTotal.subtract(annualCost);
            if (annualTotal.compareTo(BigDecimal.ZERO) > 0) {
                marginPct = annualMargin.divide(annualTotal, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }
        } catch (Exception ignored) {}

        return ClientAnnualReportDto.builder()
                .clientId(clientId)
                .clientName(client.getCompanyName())
                .year(year)
                .monthsWithInvoices(invoices.size())
                .annualSubtotal(annualSubtotal)
                .annualNoveltyDiscounts(annualDiscounts)
                .annualTaxAmount(annualTax)
                .annualTotal(annualTotal)
                .monthlyAverage(monthlyAvg)
                .annualCost(annualCost)
                .annualMargin(annualMargin)
                .marginPct(marginPct)
                .months(months)
                .build();
    }

    // ---- Reporte general anual (todos los clientes, solo APPROVED) ----
    @Transactional(readOnly = true)
    public GeneralAnnualReportDto generalAnnualReport(int year) {

        List<PreInvoiceEntity> allInvoices =
                preInvoiceRepository.findApprovedByYear(year);

        // Agrupar por cliente
        Map<UUID, List<PreInvoiceEntity>> byClient = allInvoices.stream()
                .collect(Collectors.groupingBy(pi -> pi.getClient().getId()));

        List<ClientAnnualReportDto> clientSummaries = byClient.entrySet().stream()
                .map(e -> {
                    UUID clientId       = e.getKey();
                    List<PreInvoiceEntity> invoices = e.getValue();
                    String clientName   = invoices.get(0).getClient().getCompanyName();

                    BigDecimal total    = sum(invoices, PreInvoiceEntity::getTotalAmount);
                    BigDecimal discount = sum(invoices, PreInvoiceEntity::getTotalNoveltyDiscounts);
                    BigDecimal tax      = sum(invoices, PreInvoiceEntity::getTaxAmount);
                    BigDecimal subtotal = sum(invoices, PreInvoiceEntity::getSubtotal);
                    BigDecimal avg      = invoices.isEmpty() ? BigDecimal.ZERO
                            : total.divide(new BigDecimal(invoices.size()), 0, RoundingMode.HALF_UP);

                    List<ClientMonthDetailDto> months = invoices.stream()
                            .sorted(Comparator.comparingInt(PreInvoiceEntity::getBillingMonth))
                            .map(pi -> ClientMonthDetailDto.builder()
                                    .month(pi.getBillingMonth())
                                    .monthName(MONTH_NAMES[pi.getBillingMonth()])
                                    .invoiceNumber(pi.getInvoiceNumber())
                                    .subtotal(pi.getSubtotal())
                                    .noveltyDiscounts(pi.getTotalNoveltyDiscounts())
                                    .taxAmount(pi.getTaxAmount())
                                    .total(pi.getTotalAmount())
                                    .build())
                            .collect(Collectors.toList());

                    return ClientAnnualReportDto.builder()
                            .clientId(clientId)
                            .clientName(clientName)
                            .year(year)
                            .monthsWithInvoices(invoices.size())
                            .annualSubtotal(subtotal)
                            .annualNoveltyDiscounts(discount)
                            .annualTaxAmount(tax)
                            .annualTotal(total)
                            .monthlyAverage(avg)
                            .months(months)
                            .build();
                })
                .sorted(Comparator.comparing(ClientAnnualReportDto::getClientName))
                .collect(Collectors.toList());

        // Totales por mes para el consolidado
        Map<Integer, List<PreInvoiceEntity>> byMonth = allInvoices.stream()
                .collect(Collectors.groupingBy(PreInvoiceEntity::getBillingMonth));

        List<MonthSummaryDto> monthlyTotals = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            List<PreInvoiceEntity> monthInvoices = byMonth.getOrDefault(m, List.of());
            monthlyTotals.add(MonthSummaryDto.builder()
                    .month(m)
                    .monthName(MONTH_NAMES[m])
                    .clientCount((int) monthInvoices.stream()
                            .map(pi -> pi.getClient().getId()).distinct().count())
                    .developerCount(0)
                    .subtotal(sum(monthInvoices, PreInvoiceEntity::getSubtotal))
                    .noveltyDiscounts(sum(monthInvoices, PreInvoiceEntity::getTotalNoveltyDiscounts))
                    .taxAmount(sum(monthInvoices, PreInvoiceEntity::getTaxAmount))
                    .total(sum(monthInvoices, PreInvoiceEntity::getTotalAmount))
                    .build());
        }

        BigDecimal grandTotal     = sum(allInvoices, PreInvoiceEntity::getTotalAmount);
        BigDecimal grandDiscounts = sum(allInvoices, PreInvoiceEntity::getTotalNoveltyDiscounts);
        BigDecimal grandTax       = sum(allInvoices, PreInvoiceEntity::getTaxAmount);

        return GeneralAnnualReportDto.builder()
                .year(year)
                .totalClients(byClient.size())
                .totalInvoices(allInvoices.size())
                .grandTotal(grandTotal)
                .grandNoveltyDiscounts(grandDiscounts)
                .grandTaxAmount(grandTax)
                .clientSummaries(clientSummaries)
                .monthlyTotals(monthlyTotals)
                .build();
    }

    private BigDecimal sum(List<PreInvoiceEntity> list,
                           java.util.function.Function<PreInvoiceEntity, BigDecimal> getter) {
        return list.stream().map(getter).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}