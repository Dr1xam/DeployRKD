package com.example.deeployrkd;

import com.example.deeployrkd.dto.ManagerSummaryDto;
import com.example.deeployrkd.dto.RegionSummaryDto;
import com.example.deeployrkd.dto.ReportSummaryDto;
import com.example.deeployrkd.dto.SaleResponse;
import com.example.deeployrkd.service.ExcelGeneratorService;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelGeneratorServiceTest {

    private ExcelGeneratorService excelGeneratorService;

    @BeforeEach
    void setUp() {
        excelGeneratorService = new ExcelGeneratorService();
    }

    @Test
    @DisplayName("Генерація валідного Excel-звіту з двома аркушами")
    void testGenerateExcelReport() throws IOException {
        ReportSummaryDto summary = ReportSummaryDto.builder()
                .periodTitle("Місяць: 2026-09")
                .fromDate(LocalDate.of(2026, 9, 1))
                .toDate(LocalDate.of(2026, 9, 30))
                .totalAmount(new BigDecimal("100000.00"))
                .totalSalesCount(1)
                .averageCheck(new BigDecimal("100000.00"))
                .regionSummaries(List.of(
                        RegionSummaryDto.builder().region("Київ").totalAmount(new BigDecimal("100000.00")).count(1).percentage(100.0).build()
                ))
                .managerSummaries(List.of(
                        ManagerSummaryDto.builder().manager("Іван").totalAmount(new BigDecimal("100000.00")).count(1).percentage(100.0).build()
                ))
                .sales(List.of(
                        SaleResponse.builder().id(1L).manager("Іван").product("ERP").amount(new BigDecimal("100000.00")).date(LocalDate.of(2026, 9, 5)).region("Київ").build()
                ))
                .build();

        byte[] excelBytes = excelGeneratorService.generateExcelReport(summary);

        assertThat(excelBytes).isNotNull();
        assertThat(excelBytes.length).isGreaterThan(500);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(2);
            assertThat(workbook.getSheetName(0)).isEqualTo("Підсумки");
            assertThat(workbook.getSheetName(1)).isEqualTo("Деталі продажів");
        }
    }
}
