package com.example.deeployrkd.service;

import com.example.deeployrkd.dto.ManagerSummaryDto;
import com.example.deeployrkd.dto.RegionSummaryDto;
import com.example.deeployrkd.dto.ReportSummaryDto;
import com.example.deeployrkd.dto.SaleResponse;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class ExcelGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(ExcelGeneratorService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public byte[] generateExcelReport(ReportSummaryDto summary) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            DataFormat format = workbook.createDataFormat();

            // Fonts
            Font fontHeader = workbook.createFont();
            fontHeader.setBold(true);
            fontHeader.setColor(IndexedColors.WHITE.getIndex());

            Font fontTitle = workbook.createFont();
            fontTitle.setBold(true);
            fontTitle.setFontHeightInPoints((short) 14);

            Font fontBold = workbook.createFont();
            fontBold.setBold(true);

            // Styles
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(fontTitle);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(fontHeader);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorders(headerStyle);

            CellStyle totalStyle = workbook.createCellStyle();
            totalStyle.setFont(fontBold);
            totalStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalStyle.setDataFormat(format.getFormat("#,##0.00 ₴"));
            setBorders(totalStyle);

            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(format.getFormat("#,##0.00 ₴"));
            setBorders(currencyStyle);

            CellStyle percentStyle = workbook.createCellStyle();
            percentStyle.setDataFormat(format.getFormat("0.0%"));
            setBorders(percentStyle);

            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorders(centerStyle);

            CellStyle normalStyle = workbook.createCellStyle();
            setBorders(normalStyle);

            // Sheet 1: Summary
            createSummarySheet(workbook, summary, titleStyle, headerStyle, totalStyle, currencyStyle, percentStyle, centerStyle, normalStyle, fontBold);

            // Sheet 2: Detailed Sales
            createDetailsSheet(workbook, summary, headerStyle, totalStyle, currencyStyle, centerStyle, normalStyle);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Помилка під час генерації Excel звіту: ", e);
            throw new RuntimeException("Не вдалося згенерувати Excel звіт: " + e.getMessage(), e);
        }
    }

    private void createSummarySheet(Workbook workbook, ReportSummaryDto summary,
                                    CellStyle titleStyle, CellStyle headerStyle,
                                    CellStyle totalStyle, CellStyle currencyStyle,
                                    CellStyle percentStyle, CellStyle centerStyle,
                                    CellStyle normalStyle, Font fontBold) {
        Sheet sheet = workbook.createSheet("Підсумки");
        sheet.setDisplayGridlines(true);

        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Звіт про результати продажів");
        titleCell.setCellStyle(titleStyle);

        Row periodRow = sheet.createRow(rowNum++);
        periodRow.createCell(0).setCellValue("Період: " + summary.getPeriodTitle());
        rowNum++; // blank

        // KPIs
        Row kpiHeaderRow = sheet.createRow(rowNum++);
        kpiHeaderRow.createCell(0).setCellValue("Показник");
        kpiHeaderRow.createCell(1).setCellValue("Значення");
        kpiHeaderRow.getCell(0).setCellStyle(headerStyle);
        kpiHeaderRow.getCell(1).setCellStyle(headerStyle);

        Row kpi1 = sheet.createRow(rowNum++);
        kpi1.createCell(0).setCellValue("Загальний виторг");
        kpi1.getCell(0).setCellStyle(normalStyle);
        Cell kpiVal1 = kpi1.createCell(1);
        kpiVal1.setCellValue(summary.getTotalAmount().doubleValue());
        kpiVal1.setCellStyle(totalStyle);

        Row kpi2 = sheet.createRow(rowNum++);
        kpi2.createCell(0).setCellValue("Кількість угод");
        kpi2.getCell(0).setCellStyle(normalStyle);
        Cell kpiVal2 = kpi2.createCell(1);
        kpiVal2.setCellValue(summary.getTotalSalesCount());
        kpiVal2.setCellStyle(centerStyle);

        Row kpi3 = sheet.createRow(rowNum++);
        kpi3.createCell(0).setCellValue("Середній чек");
        kpi3.getCell(0).setCellStyle(normalStyle);
        Cell kpiVal3 = kpi3.createCell(1);
        kpiVal3.setCellValue(summary.getAverageCheck().doubleValue());
        kpiVal3.setCellStyle(currencyStyle);

        rowNum += 2; // blank

        // Regions Breakdown
        Row regTitleRow = sheet.createRow(rowNum++);
        regTitleRow.createCell(0).setCellValue("Розподіл за регіонами");
        regTitleRow.getCell(0).getCellStyle().setFont(fontBold);

        Row regHeader = sheet.createRow(rowNum++);
        String[] regHeaders = {"Регіон", "Сума (₴)", "Кількість", "Частка (%)"};
        for (int i = 0; i < regHeaders.length; i++) {
            Cell c = regHeader.createCell(i);
            c.setCellValue(regHeaders[i]);
            c.setCellStyle(headerStyle);
        }

        for (RegionSummaryDto r : summary.getRegionSummaries()) {
            Row rRow = sheet.createRow(rowNum++);
            Cell c0 = rRow.createCell(0);
            c0.setCellValue(r.getRegion());
            c0.setCellStyle(normalStyle);

            Cell c1 = rRow.createCell(1);
            c1.setCellValue(r.getTotalAmount().doubleValue());
            c1.setCellStyle(currencyStyle);

            Cell c2 = rRow.createCell(2);
            c2.setCellValue(r.getCount());
            c2.setCellStyle(centerStyle);

            Cell c3 = rRow.createCell(3);
            c3.setCellValue(r.getPercentage() / 100.0);
            c3.setCellStyle(percentStyle);
        }

        rowNum += 2;

        // Managers Breakdown
        Row mgrTitleRow = sheet.createRow(rowNum++);
        mgrTitleRow.createCell(0).setCellValue("Топ-продавці (по менеджерах)");
        mgrTitleRow.getCell(0).getCellStyle().setFont(fontBold);

        Row mgrHeader = sheet.createRow(rowNum++);
        String[] mgrHeaders = {"Менеджер", "Сума (₴)", "Кількість", "Частка (%)"};
        for (int i = 0; i < mgrHeaders.length; i++) {
            Cell c = mgrHeader.createCell(i);
            c.setCellValue(mgrHeaders[i]);
            c.setCellStyle(headerStyle);
        }

        for (ManagerSummaryDto m : summary.getManagerSummaries()) {
            Row mRow = sheet.createRow(rowNum++);
            Cell c0 = mRow.createCell(0);
            c0.setCellValue(m.getManager());
            c0.setCellStyle(normalStyle);

            Cell c1 = mRow.createCell(1);
            c1.setCellValue(m.getTotalAmount().doubleValue());
            c1.setCellStyle(currencyStyle);

            Cell c2 = mRow.createCell(2);
            c2.setCellValue(m.getCount());
            c2.setCellStyle(centerStyle);

            Cell c3 = mRow.createCell(3);
            c3.setCellValue(m.getPercentage() / 100.0);
            c3.setCellStyle(percentStyle);
        }

        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createDetailsSheet(Workbook workbook, ReportSummaryDto summary,
                                    CellStyle headerStyle, CellStyle totalStyle,
                                    CellStyle currencyStyle, CellStyle centerStyle,
                                    CellStyle normalStyle) {
        Sheet sheet = workbook.createSheet("Деталі продажів");
        sheet.setDisplayGridlines(true);

        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"№", "Дата", "Менеджер", "Товар / Послуга", "Регіон", "Сума (₴)"};
        for (int i = 0; i < headers.length; i++) {
            Cell c = headerRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(headerStyle);
        }

        int index = 1;
        for (SaleResponse s : summary.getSales()) {
            Row row = sheet.createRow(rowNum++);

            Cell c0 = row.createCell(0);
            c0.setCellValue(index++);
            c0.setCellStyle(centerStyle);

            Cell c1 = row.createCell(1);
            c1.setCellValue(s.getDate().format(DATE_FORMAT));
            c1.setCellStyle(centerStyle);

            Cell c2 = row.createCell(2);
            c2.setCellValue(s.getManager());
            c2.setCellStyle(normalStyle);

            Cell c3 = row.createCell(3);
            c3.setCellValue(s.getProduct());
            c3.setCellStyle(normalStyle);

            Cell c4 = row.createCell(4);
            c4.setCellValue(s.getRegion());
            c4.setCellStyle(normalStyle);

            Cell c5 = row.createCell(5);
            c5.setCellValue(s.getAmount().doubleValue());
            c5.setCellStyle(currencyStyle);
        }

        // Total Row
        Row totalRow = sheet.createRow(rowNum);
        Cell tLabel = totalRow.createCell(0);
        tLabel.setCellValue("РАЗОМ");
        tLabel.setCellStyle(totalStyle);

        for (int i = 1; i <= 4; i++) {
            Cell blank = totalRow.createCell(i);
            blank.setCellStyle(totalStyle);
        }

        Cell tVal = totalRow.createCell(5);
        tVal.setCellValue(summary.getTotalAmount().doubleValue());
        tVal.setCellStyle(totalStyle);

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
