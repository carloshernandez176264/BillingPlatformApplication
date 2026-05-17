package com.billingplatformapplication.preinvoices.builder;

import com.billingplatformapplication.billing.dto.BillingLineDto;
import com.billingplatformapplication.client.entity.ClientEntity;
import com.billingplatformapplication.currencies.entity.CurrencyEntity;
import com.billingplatformapplication.preinvoices.entity.PreInvoiceEntity;
import com.billingplatformapplication.preinvoices.entity.PreInvoiceItemEntity;
import com.billingplatformapplication.shared.exception.ResourceNotFoundException;
import com.billingplatformapplication.worklogs.entity.WorkLogEntity;
import com.billingplatformapplication.worklogs.repository.WorkLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Builder pattern for PreInvoiceEntity.
 * Encapsulates the assembly logic so PreInvoiceService stays clean.
 */
@Component
@RequiredArgsConstructor
public class PreInvoiceBuilder {

    private final WorkLogRepository workLogRepository;

    public PreInvoiceEntity build(String invoiceNumber,
                                  ClientEntity client,
                                  CurrencyEntity currency,
                                  int year, int month,
                                  List<BillingLineDto> lines,
                                  String observations) {
        String periodDesc = LocalDate.of(year, month, 1)
                .getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year;

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
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        invoice.setTotalNoveltyDiscounts(lines.stream()
                .map(BillingLineDto::getNoveltyDiscount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        invoice.setTotalOtherDiscounts(java.math.BigDecimal.ZERO);
        invoice.setTaxableAmount(invoice.getSubtotal()
                .subtract(invoice.getTotalNoveltyDiscounts())
                .max(java.math.BigDecimal.ZERO));
        invoice.setTaxAmount(java.math.BigDecimal.ZERO);
        invoice.setTotalAmount(invoice.getTaxableAmount());

        List<PreInvoiceItemEntity> items = buildItems(lines, invoice);
        invoice.setItems(items);
        return invoice;
    }

    private List<PreInvoiceItemEntity> buildItems(List<BillingLineDto> lines,
                                                  PreInvoiceEntity invoice) {
        List<PreInvoiceItemEntity> items = new ArrayList<>();
        AtomicInteger order = new AtomicInteger(1);

        for (BillingLineDto line : lines) {
            WorkLogEntity wl = workLogRepository.findById(line.getWorkLogId())
                    .orElseThrow(() -> new ResourceNotFoundException("WorkLog", line.getWorkLogId()));

            items.add(PreInvoiceItemEntity.builder()
                    .preInvoice(invoice)
                    .workLog(wl)
                    .developer(wl.getDeveloper())
                    .developerProfile(wl.getDeveloperProfile())
                    .rate(wl.getAppliedRate())
                    .rateType(line.getRateType())
                    .rateValue(line.getRateValue())
                    .billedHours(line.getBilledHours())
                    .billedDays(line.getBilledDays())
                    .grossAmount(line.getGrossAmount())
                    .noveltyDiscount(line.getNoveltyDiscount())
                    .otherDiscount(line.getOtherDiscount())
                    .netAmount(line.getNetAmount())
                    .lineDescription(String.format("%s — %s — %.2f h",
                            line.getProfileName(), line.getDeveloperName(),
                            line.getBilledHours()))
                    .sortOrder(order.getAndIncrement())
                    .build());
        }
        return items;
    }
}

