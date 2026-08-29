package com.example.deeployrkd;

import com.example.deeployrkd.dto.SaleRequest;
import com.example.deeployrkd.repository.SaleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SaleControllerTest {

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
    }

    @Test
    @DisplayName("POST /sales успішно створює новий продаж (201 Created)")
    void testCreateSaleSuccess() throws Exception {
        SaleRequest request = SaleRequest.builder()
                .manager("Олександр Коваленко")
                .product("CRM Enterprise")
                .amount(new BigDecimal("75000.00"))
                .date(LocalDate.of(2026, 9, 10))
                .region("Київ")
                .build();

        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.manager", is("Олександр Коваленко")))
                .andExpect(jsonPath("$.product", is("CRM Enterprise")))
                .andExpect(jsonPath("$.amount", is(75000.00)))
                .andExpect(jsonPath("$.region", is("Київ")));
    }

    @Test
    @DisplayName("POST /sales з невалідними даними повертає 400 Bad Request з описом помилок")
    void testCreateSaleValidationError() throws Exception {
        SaleRequest invalidRequest = SaleRequest.builder()
                .manager("") // blank
                .product("") // blank
                .amount(new BigDecimal("-100")) // negative
                .date(null) // null
                .region("") // blank
                .build();

        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.manager", notNullValue()))
                .andExpect(jsonPath("$.validationErrors.product", notNullValue()))
                .andExpect(jsonPath("$.validationErrors.amount", notNullValue()))
                .andExpect(jsonPath("$.validationErrors.date", notNullValue()))
                .andExpect(jsonPath("$.validationErrors.region", notNullValue()));
    }

    @Test
    @DisplayName("GET /sales повертає список та підтримує фільтрацію")
    void testGetSalesWithFilter() throws Exception {
        SaleRequest r1 = SaleRequest.builder()
                .manager("Іван")
                .product("P1")
                .amount(new BigDecimal("1000.00"))
                .date(LocalDate.of(2026, 9, 1))
                .region("Київ")
                .build();
        SaleRequest r2 = SaleRequest.builder()
                .manager("Петро")
                .product("P2")
                .amount(new BigDecimal("2000.00"))
                .date(LocalDate.of(2026, 9, 2))
                .region("Західний")
                .build();

        mockMvc.perform(post("/sales").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(r1)));
        mockMvc.perform(post("/sales").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(r2)));

        // Filter by region
        mockMvc.perform(get("/sales?region=Київ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].region", is("Київ")));

        // All sales
        mockMvc.perform(get("/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
