package com.example.deeployrkd;

import com.example.deeployrkd.dto.SaleRequest;
import com.example.deeployrkd.repository.SaleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SaleRepository saleRepository;

    @MockBean
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        saleRepository.clear();
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        // Create 2 sales
        saleRepository.save(com.example.deeployrkd.model.Sale.builder()
                .manager("Олександр")
                .product("ERP")
                .amount(new BigDecimal("100000.00"))
                .date(LocalDate.of(2026, 9, 5))
                .region("Київ")
                .build());
        saleRepository.save(com.example.deeployrkd.model.Sale.builder()
                .manager("Марія")
                .product("Cloud")
                .amount(new BigDecimal("50000.00"))
                .date(LocalDate.of(2026, 9, 15))
                .region("Західний")
                .build());
    }

    @Test
    @DisplayName("GET /reports/summary повертає коректний JSON зі статистикою")
    void testGetSummary() throws Exception {
        mockMvc.perform(get("/reports/summary?month=2026-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount", is(150000.00)))
                .andExpect(jsonPath("$.totalSalesCount", is(2)))
                .andExpect(jsonPath("$.regionSummaries", hasSize(2)))
                .andExpect(jsonPath("$.managerSummaries", hasSize(2)));
    }

    @Test
    @DisplayName("GET /reports/sales.pdf повертає валідний PDF файл (application/pdf)")
    void testGetPdfReport() throws Exception {
        mockMvc.perform(get("/reports/sales.pdf?month=2026-09"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", containsString("sales-report-2026-09.pdf")));
    }

    @Test
    @DisplayName("GET /reports/sales.xlsx повертає Excel документ")
    void testGetExcelReport() throws Exception {
        mockMvc.perform(get("/reports/sales.xlsx?month=2026-09"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", containsString("sales-report-2026-09.xlsx")));
    }

    @Test
    @DisplayName("POST /reports/send успішно відправляє звіт з вкладенням на email")
    void testSendReportEmail() throws Exception {
        mockMvc.perform(post("/reports/send?month=2026-09&emails=boss@company.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipients", hasItem("boss@company.com")))
                .andExpect(jsonPath("$.salesCount", is(2)))
                .andExpect(jsonPath("$.totalAmount", is(150000.00)));
    }
}
