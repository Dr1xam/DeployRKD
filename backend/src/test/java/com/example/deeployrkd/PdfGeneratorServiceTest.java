package com.example.deeployrkd;

import com.example.deeployrkd.dto.ManagerSummaryDto;
import com.example.deeployrkd.dto.RegionSummaryDto;
import com.example.deeployrkd.dto.ReportSummaryDto;
import com.example.deeployrkd.dto.SaleResponse;
import com.example.deeployrkd.service.PdfGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PdfGeneratorServiceTest {

    private PdfGeneratorService pdfGeneratorService;

    @BeforeEach
    void setUp() {
        pdfGeneratorService = new PdfGeneratorService();
    }

    @Test
    @DisplayName("Генерація валідного PDF-звіту з кирилицею та графіком")
    void testGeneratePdfReport() {
        ReportSummaryDto summary = ReportSummaryDto.builder()
                .periodTitle("Місяць: 2026-09")
                .fromDate(LocalDate.of(2026, 9, 1))
                .toDate(LocalDate.of(2026, 9, 30))
                .totalAmount(new BigDecimal("350000.00"))
                .totalSalesCount(4)
                .averageCheck(new BigDecimal("87500.00"))
                .regionSummaries(List.of(
                        RegionSummaryDto.builder().region("Київ").totalAmount(new BigDecimal("200000.00")).count(2).percentage(57.14).build(),
                        RegionSummaryDto.builder().region("Західний").totalAmount(new BigDecimal("150000.00")).count(2).percentage(42.86).build()
                ))
                .managerSummaries(List.of(
                        ManagerSummaryDto.builder().manager("Олександр Коваленко").totalAmount(new BigDecimal("200000.00")).count(2).percentage(57.14).build(),
                        ManagerSummaryDto.builder().manager("Марія Шевченко").totalAmount(new BigDecimal("150000.00")).count(2).percentage(42.86).build()
                ))
                .sales(List.of(
                        SaleResponse.builder().id(1L).manager("Олександр Коваленко").product("ERP Система").amount(new BigDecimal("120000.00")).date(LocalDate.of(2026, 9, 5)).region("Київ").build(),
                        SaleResponse.builder().id(2L).manager("Марія Шевченко").product("Хмарне сховище").amount(new BigDecimal("80000.00")).date(LocalDate.of(2026, 9, 10)).region("Західний").build()
                ))
                .build();

        byte[] pdfBytes = pdfGeneratorService.generatePdfReport(summary);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(1000); // Should be a valid non-empty document

        // Check PDF header "%PDF-"
        String header = new String(pdfBytes, 0, Math.min(pdfBytes.length, 10), StandardCharsets.US_ASCII);
        assertThat(header).startsWith("%PDF-");
    }
}
