package com.billingplatformapplication.preinvoices.builder;

import com.billingplatformapplication.billing.dto.BillingLineDto;
import com.billingplatformapplication.client.entity.ClientEntity;
import com.billingplatformapplication.currencies.entity.CurrencyEntity;
import com.billingplatformapplication.developers.repository.DeveloperRepository;
import com.billingplatformapplication.developerprofiles.repository.DeveloperProfileRepository;
import com.billingplatformapplication.preinvoices.entity.PreInvoiceEntity;
import com.billingplatformapplication.preinvoices.entity.PreInvoiceItemEntity;
import com.billingplatformapplication.worklogs.entity.WorkLogEntity;
import com.billingplatformapplication.worklogs.repository.WorkLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class PreInvoiceBuilder {

    private final WorkLogRepository         workLogRepository;
    private final DeveloperRepository       developerRepository;
    private final DeveloperProfileRepository profileRepository;

    public PreInvoiceEntity build(String invoiceNumber,
                                  ClientEntity client,
                                  CurrencyEntity currency,
                                  int year, int month,
                                  List<BillingLineDto> lines,
                                  String observations) {

        String periodDesc = LocalDate.of(year, month, 1)
                .getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-CO"))
                + " " + year;

        PreInvoiceEntity invoice = PreInvoiceEntity.builder()
                .invoiceNumber(invoiceNumber)
                .client(client)
                .currency(currency)
                .billingYear(year)
                .billingMonth(month)
                .periodDescription(periodDesc)
                .generationDate(LocalDate.now())
                .status(PreInvoiceEntity.PreInvoiceStatus.GENERATED)
                .observations(observations)
                .version(1)
                .build();

        invoice.setSubtotal(lines.stream()
                .map(BillingLineDto::getGrossAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        invoice.setTotalNoveltyDiscounts(lines.stream()
                .map(BillingLineDto::getNoveltyDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        invoice.setTotalOtherDiscounts(BigDecimal.ZERO);
        invoice.setTaxableAmount(invoice.getSubtotal()
                .subtract(invoice.getTotalNoveltyDiscounts())
                .max(BigDecimal.ZERO));
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(invoice.getTaxableAmount());

        invoice.setItems(buildItems(lines, invoice));
        return invoice;
    }

    private List<PreInvoiceItemEntity> buildItems(List<BillingLineDto> lines,
                                                  PreInvoiceEntity invoice) {
        List<PreInvoiceItemEntity> items = new ArrayList<>();
        AtomicInteger order = new AtomicInteger(1);

        for (BillingLineDto line : lines) {

            // workLog es OPCIONAL — puede ser null si no hay registro de horas
            WorkLogEntity workLog = null;
            if (line.getWorkLogId() != null) {
                workLog = workLogRepository.findById(line.getWorkLogId()).orElse(null);
            }

            // Developer y perfil — los buscamos directamente por ID
            var developer = developerRepository.findById(line.getDeveloperId())
                    .orElseThrow(() -> new RuntimeException(
                            "Developer not found: " + line.getDeveloperId()));

            var profile = developer.getProfile();

            items.add(PreInvoiceItemEntity.builder()
                    .preInvoice(invoice)
                    .workLog(workLog)                    // puede ser null
                    .developer(developer)
                    .developerProfile(profile)
                    .rateType(line.getRateType())
                    .rateValue(line.getRateValue())
                    .billedHours(line.getBilledHours())
                    .billedDays(line.getBilledDays())
                    .grossAmount(line.getGrossAmount())
                    .noveltyDiscount(line.getNoveltyDiscount())
                    .otherDiscount(line.getOtherDiscount())
                    .netAmount(line.getNetAmount())
                    .lineDescription(String.format("%s — %s — %.0f h",
                            line.getProfileName(),
                            line.getDeveloperName(),
                            line.getBilledHours()))
                    .sortOrder(order.getAndIncrement())
                    .build());
        }
        return items;
    }
}