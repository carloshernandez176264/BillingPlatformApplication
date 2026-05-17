package com.billingplatformapplication.preinvoices.export;




import com.billingplatformapplication.preinvoices.dto.response.PreInvoiceItemResponseDto;
import com.billingplatformapplication.preinvoices.dto.response.PreInvoiceResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@Slf4j
@Service
public class ExcelExportService {

    public byte[] export(PreInvoiceResponseDto invoice) {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Pre-Invoice");
            sheet.setColumnWidth(0, 8000); sheet.setColumnWidth(1, 8000);
            sheet.setColumnWidth(2, 5000); sheet.setColumnWidth(3, 4000);
            sheet.setColumnWidth(4, 5500); sheet.setColumnWidth(5, 5500);
            sheet.setColumnWidth(6, 5500);

            CellStyle headerStyle = buildHeaderStyle(wb);
            CellStyle titleStyle  = buildTitleStyle(wb);
            CellStyle moneyStyle  = buildMoneyStyle(wb);
            CellStyle totalStyle  = buildTotalStyle(wb);
            CellStyle boldStyle   = buildBoldStyle(wb);
            CellStyle normalStyle = wb.createCellStyle();

            int row = writeTitle(sheet, 0, invoice, titleStyle);
            row = writeClientInfo(sheet, row, invoice, boldStyle, normalStyle);
            row = writeItemsTable(sheet, row, invoice, headerStyle, normalStyle, moneyStyle);
            writeTotals(sheet, row, invoice, boldStyle, totalStyle, moneyStyle);

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Excel export failed for {}: {}", invoice.getInvoiceNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate Excel", e);
        }
    }

    private int writeTitle(Sheet sheet, int r, PreInvoiceResponseDto inv, CellStyle style) {
        Row row = sheet.createRow(r++);
        Cell c = row.createCell(0);
        c.setCellValue("PRE-INVOICE — " + inv.getInvoiceNumber());
        c.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 6));
        Row r2 = sheet.createRow(r++);
        r2.createCell(0).setCellValue("Period: " + s(inv.getPeriodDescription()));
        r2.createCell(3).setCellValue("Status: " + s(inv.getStatus()));
        r2.createCell(5).setCellValue("Currency: " + s(inv.getCurrencyCode()));
        sheet.createRow(r++);
        return r;
    }

    private int writeClientInfo(Sheet sheet, int r, PreInvoiceResponseDto inv,
                                CellStyle bold, CellStyle normal) {
        writeKV(sheet, r++, "Client:",        s(inv.getClientName()),        bold, normal);
        writeKV(sheet, r++, "Tax ID:",        s(inv.getClientTaxId()),       bold, normal);
        writeKV(sheet, r++, "Billing Email:", s(inv.getClientBillingEmail()), bold, normal);
        sheet.createRow(r++);
        return r;
    }

    private int writeItemsTable(Sheet sheet, int r, PreInvoiceResponseDto inv,
                                CellStyle headerStyle, CellStyle normal, CellStyle money) {
        String[] cols = {"Developer","Profile","Rate Type","Hours","Gross Amount","Discount","Net Amount"};
        Row hdr = sheet.createRow(r++);
        for (int i = 0; i < cols.length; i++) {
            Cell c = hdr.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(headerStyle);
        }
        for (PreInvoiceItemResponseDto item : inv.getItems()) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(s(item.developerName()));
            row.createCell(1).setCellValue(s(item.developerProfileName()));
            row.createCell(2).setCellValue(s(item.rateType()));
            row.createCell(3).setCellValue(d(item.billedHours()));
            Cell g = row.createCell(4); g.setCellValue(d(item.grossAmount()));    g.setCellStyle(money);
            Cell dc = row.createCell(5); dc.setCellValue(d(item.noveltyDiscount())); dc.setCellStyle(money);
            Cell n = row.createCell(6); n.setCellValue(d(item.netAmount()));      n.setCellStyle(money);
        }
        sheet.createRow(r++);
        return r;
    }

    private void writeTotals(Sheet sheet, int r, PreInvoiceResponseDto inv,
                             CellStyle bold, CellStyle total, CellStyle money) {
        writeMoneyRow(sheet, r++, "Subtotal",          inv.getSubtotal(),             bold, money);
        writeMoneyRow(sheet, r++, "Novelty Discounts", inv.getTotalNoveltyDiscounts(), bold, money);
        writeMoneyRow(sheet, r++, "Other Discounts",   inv.getTotalOtherDiscounts(),   bold, money);
        writeMoneyRow(sheet, r++, "Taxable Amount",    inv.getTaxableAmount(),         bold, money);
        writeMoneyRow(sheet, r++, "Tax",               inv.getTaxAmount(),             bold, money);
        Row tot = sheet.createRow(r);
        Cell l = tot.createCell(5); l.setCellValue("TOTAL AMOUNT"); l.setCellStyle(total);
        Cell v = tot.createCell(6); v.setCellValue(d(inv.getTotalAmount())); v.setCellStyle(total);
    }

    // Styles
    private CellStyle buildTitleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setFontHeightInPoints((short)14);
        f.setColor(IndexedColors.WHITE.getIndex()); s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private CellStyle buildHeaderStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private CellStyle buildMoneyStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
        s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    private CellStyle buildTotalStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
        s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    private CellStyle buildBoldStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); s.setFont(f);
        return s;
    }

    private void writeKV(Sheet sheet, int r, String key, String val,
                         CellStyle boldStyle, CellStyle normalStyle) {
        Row row = sheet.createRow(r);
        Cell k = row.createCell(0); k.setCellValue(key); k.setCellStyle(boldStyle);
        Cell v = row.createCell(1); v.setCellValue(val); v.setCellStyle(normalStyle);
    }

    private void writeMoneyRow(Sheet sheet, int r, String label, BigDecimal value,
                               CellStyle boldStyle, CellStyle moneyStyle) {
        Row row = sheet.createRow(r);
        Cell l = row.createCell(5); l.setCellValue(label); l.setCellStyle(boldStyle);
        Cell v = row.createCell(6); v.setCellValue(d(value)); v.setCellStyle(moneyStyle);
    }

    private double d(BigDecimal v) { return v != null ? v.doubleValue() : 0.0; }
    private String s(String v)     { return v != null ? v : ""; }
}

