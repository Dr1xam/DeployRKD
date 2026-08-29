package com.example.deeployrkd;

import com.example.deeployrkd.dto.ManagerSummaryDto;
import com.example.deeployrkd.dto.RegionSummaryDto;
import com.example.deeployrkd.dto.ReportSummaryDto;
import com.example.deeployrkd.dto.SaleRequest;
import com.example.deeployrkd.dto.SaleResponse;
import com.example.deeployrkd.exception.InvalidPeriodException;
import com.example.deeployrkd.exception.ResourceNotFoundException;
import com.example.deeployrkd.model.Sale;
import com.example.deeployrkd.repository.InMemorySaleRepository;
import com.example.deeployrkd.repository.SaleRepository;
import com.example.deeployrkd.service.SaleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaleServiceTest {

    private SaleRepository saleRepository;
    private SaleService saleService;

    @BeforeEach
    void setUp() {
        saleRepository = new InMemorySaleRepository();
        saleService = new SaleService(saleRepository);
    }

    @Test
    @DisplayName("Створення продажу з валідними даними")
    void testCreateSale() {
        SaleRequest request = SaleRequest.builder()
                .manager("Іван Франко")
                .product("Підписка Pro")
                .amount(new BigDecimal("1500.50"))
                .date(LocalDate.of(2026, 9, 10))
                .region("Київ")
                .build();

        SaleResponse response = saleService.createSale(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getManager()).isEqualTo("Іван Франко");
        assertThat(response.getProduct()).isEqualTo("Підписка Pro");
        assertThat(response.getAmount()).isEqualByComparingTo("1500.50");
        assertThat(response.getDate()).isEqualTo(LocalDate.of(2026, 9, 10));
        assertThat(response.getRegion()).isEqualTo("Київ");
    }

    @Test
    @DisplayName("Отримання списку продажів з фільтром за регіоном")
    void testGetSalesByRegion() {
        saleRepository.save(Sale.builder().manager("A").product("P1").amount(new BigDecimal("100")).date(LocalDate.of(2026, 9, 1)).region("Київ").build());
        saleRepository.save(Sale.builder().manager("B").product("P2").amount(new BigDecimal("200")).date(LocalDate.of(2026, 9, 2)).region("Львів").build());

        List<SaleResponse> kyivSales = saleService.getSales("Київ", null, null, null);
        assertThat(kyivSales).hasSize(1);
        assertThat(kyivSales.get(0).getRegion()).isEqualTo("Київ");
    }

    @Test
    @DisplayName("Отримання списку продажів з фільтром за місяцем")
    void testGetSalesByMonth() {
        saleRepository.save(Sale.builder().manager("A").product("P1").amount(new BigDecimal("100")).date(LocalDate.of(2026, 8, 15)).region("Київ").build());
        saleRepository.save(Sale.builder().manager("B").product("P2").amount(new BigDecimal("200")).date(LocalDate.of(2026, 9, 10)).region("Київ").build());
        saleRepository.save(Sale.builder().manager("C").product("P3").amount(new BigDecimal("300")).date(LocalDate.of(2026, 9, 20)).region("Львів").build());

        List<SaleResponse> sepSales = saleService.getSales(null, "2026-09", null, null);
        assertThat(sepSales).hasSize(2);
    }

    @Test
    @DisplayName("Підрахунок підсумків: загальна сума, середній чек, розбивка по регіонах і топ-менеджерах")
    void testReportSummaryCalculations() {
        saleRepository.save(Sale.builder().manager("Олександр").product("ERP").amount(new BigDecimal("100000.00")).date(LocalDate.of(2026, 9, 5)).region("Київ").build());
        saleRepository.save(Sale.builder().manager("Олександр").product("CRM").amount(new BigDecimal("50000.00")).date(LocalDate.of(2026, 9, 15)).region("Київ").build());
        saleRepository.save(Sale.builder().manager("Марія").product("Cloud").amount(new BigDecimal("50000.00")).date(LocalDate.of(2026, 9, 20)).region("Західний").build());

        ReportSummaryDto summary = saleService.getReportSummary("2026-09", null, null, null);

        assertThat(summary.getTotalSalesCount()).isEqualTo(3);
        assertThat(summary.getTotalAmount()).isEqualByComparingTo("200000.00");
        assertThat(summary.getAverageCheck()).isEqualByComparingTo("66666.67");

        // Regions check
        List<RegionSummaryDto> regions = summary.getRegionSummaries();
        assertThat(regions).hasSize(2);
        assertThat(regions.get(0).getRegion()).isEqualTo("Київ");
        assertThat(regions.get(0).getTotalAmount()).isEqualByComparingTo("150000.00");
        assertThat(regions.get(0).getCount()).isEqualTo(2);
        assertThat(regions.get(0).getPercentage()).isEqualTo(75.0);

        assertThat(regions.get(1).getRegion()).isEqualTo("Західний");
        assertThat(regions.get(1).getTotalAmount()).isEqualByComparingTo("50000.00");
        assertThat(regions.get(1).getCount()).isEqualTo(1);
        assertThat(regions.get(1).getPercentage()).isEqualTo(25.0);

        // Managers check
        List<ManagerSummaryDto> managers = summary.getManagerSummaries();
        assertThat(managers).hasSize(2);
        assertThat(managers.get(0).getManager()).isEqualTo("Олександр");
        assertThat(managers.get(0).getTotalAmount()).isEqualByComparingTo("150000.00");
        assertThat(managers.get(0).getCount()).isEqualTo(2);
        assertThat(managers.get(0).getPercentage()).isEqualTo(75.0);

        assertThat(managers.get(1).getManager()).isEqualTo("Марія");
        assertThat(managers.get(1).getTotalAmount()).isEqualByComparingTo("50000.00");
        assertThat(managers.get(1).getCount()).isEqualTo(1);
        assertThat(managers.get(1).getPercentage()).isEqualTo(25.0);
    }

    @Test
    @DisplayName("Помилка при некоректному діапазоні дат")
    void testInvalidDateRangeThrows() {
        LocalDate from = LocalDate.of(2026, 9, 20);
        LocalDate to = LocalDate.of(2026, 9, 10);

        assertThatThrownBy(() -> saleService.getReportSummary(null, from, to, null))
                .isInstanceOf(InvalidPeriodException.class);
    }

    @Test
    @DisplayName("Помилка при некоректному форматі місяця")
    void testInvalidMonthThrows() {
        assertThatThrownBy(() -> saleService.getReportSummary("invalid-month", null, null, null))
                .isInstanceOf(InvalidPeriodException.class);
    }

    @Test
    @DisplayName("Видалення неіснуючого продажу викликає ResourceNotFoundException")
    void testDeleteNotFoundThrows() {
        assertThatThrownBy(() -> saleService.deleteSale(9999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
