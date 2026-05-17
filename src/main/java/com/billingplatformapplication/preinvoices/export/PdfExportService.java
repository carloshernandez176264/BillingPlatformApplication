package com.billingplatformapplication.preinvoices.export;


import com.billingplatformapplication.preinvoices.dto.response.PreInvoiceItemResponseDto;
import com.billingplatformapplication.preinvoices.dto.response.PreInvoiceResponseDto;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class PdfExportService {

    private static final DeviceRgb HEADER_BG   = new DeviceRgb(30, 64, 120);
    private static final DeviceRgb ROW_ALT_BG  = new DeviceRgb(245, 247, 250);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    public byte[] export(PreInvoiceResponseDto invoice) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfDocument pdf = new PdfDocument(new PdfWriter(out));
            Document    doc = new Document(pdf);

            PdfFont bold   = PdfFontFactory.createFont(
                    com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            PdfFont normal = PdfFontFactory.createFont(
                    com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

            addTitle(doc, invoice, bold, normal);
            addClientSection(doc, invoice, bold, normal);
            addItemsTable(doc, invoice, bold, normal);
            addTotalsSection(doc, invoice, bold, normal);
            addFooter(doc, invoice, normal);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("PDF export failed for invoice {}: {}", invoice.getInvoiceNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addTitle(Document doc, PreInvoiceResponseDto inv, PdfFont bold, PdfFont normal) {
        doc.add(new Paragraph("PRE-INVOICE")
                .setFont(bold).setFontSize(22)
                .setFontColor(HEADER_BG).setTextAlignment(TextAlignment.RIGHT));
        doc.add(new Paragraph()
                .setFont(normal).setFontSize(10).setTextAlignment(TextAlignment.RIGHT)
                .add(new Text("Number: ").setFont(bold)).add(inv.getInvoiceNumber() + "\n")
                .add(new Text("Date: ").setFont(bold))
                .add(inv.getGenerationDate() != null ? inv.getGenerationDate().format(DATE) : "")
                .add("\n")
                .add(new Text("Status: ").setFont(bold)).add(s(inv.getStatus())));
        doc.add(new Paragraph(" ").setFontSize(2)
                .setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(
                        ColorConstants.LIGHT_GRAY, 1)));
        doc.add(new Paragraph("\n").setFontSize(4));
    }

    private void addClientSection(Document doc, PreInvoiceResponseDto inv, PdfFont bold, PdfFont normal) {
        Table t = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));
        t.addCell(new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .add(new Paragraph("BILL TO").setFont(bold).setFontSize(9).setFontColor(HEADER_BG))
                .add(new Paragraph(s(inv.getClientName())).setFont(bold).setFontSize(11))
                .add(new Paragraph("Tax ID: " + s(inv.getClientTaxId())).setFont(normal).setFontSize(10))
                .add(new Paragraph(s(inv.getClientBillingEmail())).setFont(normal).setFontSize(10)));
        t.addCell(new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph("BILLING PERIOD").setFont(bold).setFontSize(9).setFontColor(HEADER_BG))
                .add(new Paragraph(s(inv.getPeriodDescription())).setFont(bold).setFontSize(14))
                .add(new Paragraph("Currency: " + s(inv.getCurrencyCode())).setFont(normal).setFontSize(10)));
        doc.add(t);
        doc.add(new Paragraph("\n").setFontSize(6));
    }

    private void addItemsTable(Document doc, PreInvoiceResponseDto inv, PdfFont bold, PdfFont normal) {
        Table t = new Table(UnitValue.createPercentArray(new float[]{25, 20, 12, 10, 15, 15, 15}))
                .setWidth(UnitValue.createPercentValue(100));
        String[] headers = {"Developer", "Profile", "Rate Type", "Hours", "Gross", "Discount", "Net"};
        for (String h : headers) {
            t.addHeaderCell(new Cell().setBackgroundColor(HEADER_BG)
                    .add(new Paragraph(h).setFont(bold).setFontSize(9)
                            .setFontColor(ColorConstants.WHITE)));
        }
        boolean alt = false;
        for (PreInvoiceItemResponseDto item : inv.getItems()) {
            com.itextpdf.kernel.colors.Color bg = alt ? ROW_ALT_BG : ColorConstants.WHITE;
            alt = !alt;
            t.addCell(cell(s(item.developerName()),        normal, 9, bg));
            t.addCell(cell(s(item.developerProfileName()), normal, 9, bg));
            t.addCell(cell(s(item.rateType()),             normal, 9, bg));
            t.addCell(cell(fmt(item.billedHours()),        normal, 9, bg, TextAlignment.RIGHT));
            t.addCell(cell(money(item.grossAmount(), inv.getCurrencySymbol()),    normal, 9, bg, TextAlignment.RIGHT));
            t.addCell(cell(money(item.noveltyDiscount(), inv.getCurrencySymbol()), normal, 9, bg, TextAlignment.RIGHT));
            t.addCell(cell(money(item.netAmount(), inv.getCurrencySymbol()),      bold,   9, bg, TextAlignment.RIGHT));
        }
        doc.add(t);
        doc.add(new Paragraph("\n").setFontSize(6));
    }

    private void addTotalsSection(Document doc, PreInvoiceResponseDto inv, PdfFont bold, PdfFont normal) {
        Table t = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .setWidth(UnitValue.createPercentValue(100));
        addTotalRow(t, "Subtotal",          inv.getSubtotal(),              inv.getCurrencySymbol(), normal, bold);
        addTotalRow(t, "Novelty Discounts", inv.getTotalNoveltyDiscounts(), inv.getCurrencySymbol(), normal, bold);
        addTotalRow(t, "Other Discounts",   inv.getTotalOtherDiscounts(),   inv.getCurrencySymbol(), normal, bold);
        addTotalRow(t, "Taxable Amount",    inv.getTaxableAmount(),         inv.getCurrencySymbol(), normal, bold);
        addTotalRow(t, "Tax",               inv.getTaxAmount(),             inv.getCurrencySymbol(), normal, bold);
        t.addCell(new Cell().setBackgroundColor(HEADER_BG)
                .add(new Paragraph("TOTAL AMOUNT").setFont(bold).setFontSize(12)
                        .setFontColor(ColorConstants.WHITE).setTextAlignment(TextAlignment.RIGHT)));
        t.addCell(new Cell().setBackgroundColor(HEADER_BG)
                .add(new Paragraph(money(inv.getTotalAmount(), inv.getCurrencySymbol()))
                        .setFont(bold).setFontSize(12)
                        .setFontColor(ColorConstants.WHITE).setTextAlignment(TextAlignment.RIGHT)));
        doc.add(t);
    }

    private void addFooter(Document doc, PreInvoiceResponseDto inv, PdfFont normal) {
        doc.add(new Paragraph("\n").setFontSize(8));
        if (inv.getObservations() != null && !inv.getObservations().isBlank()) {
            doc.add(new Paragraph("Notes: " + inv.getObservations())
                    .setFont(normal).setFontSize(9).setFontColor(ColorConstants.GRAY));
        }
        doc.add(new Paragraph("Generated by Billing Platform — " + s(inv.getCreatedBy()))
                .setFont(normal).setFontSize(8)
                .setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER));
    }

    // Helpers
    private Cell cell(String text, PdfFont font, float size,
                      com.itextpdf.kernel.colors.Color bg) {
        return cell(text, font, size, bg, TextAlignment.LEFT);
    }

    private Cell cell(String text, PdfFont font, float size,
                      com.itextpdf.kernel.colors.Color bg, TextAlignment align) {
        return new Cell().setBackgroundColor(bg)
                .add(new Paragraph(text).setFont(font).setFontSize(size).setTextAlignment(align));
    }

    private void addTotalRow(Table t, String label, BigDecimal value, String symbol,
                             PdfFont normal, PdfFont bold) {
        t.addCell(new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .add(new Paragraph(label).setFont(normal).setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT)));
        t.addCell(new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .add(new Paragraph(money(value, symbol)).setFont(bold).setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT)));
    }

    private String money(BigDecimal v, String symbol) {
        if (v == null) return s(symbol) + " 0.00";
        return s(symbol) + String.format("%,.2f", v);
    }

    private String fmt(BigDecimal v) {
        return v != null ? String.format("%.2f", v) : "0.00";
    }

    private String s(String v) { return v != null ? v : ""; }
}

