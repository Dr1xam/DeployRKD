package com.example.deeployrkd.service;

import com.example.deeployrkd.dto.ManagerSummaryDto;
import com.example.deeployrkd.dto.RegionSummaryDto;
import com.example.deeployrkd.dto.ReportSummaryDto;
import com.example.deeployrkd.dto.SaleRequest;
import com.example.deeployrkd.dto.SaleResponse;
import com.example.deeployrkd.exception.InvalidPeriodException;
import com.example.deeployrkd.exception.ResourceNotFoundException;
import com.example.deeployrkd.model.Sale;
import com.example.deeployrkd.repository.SaleRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SaleService {

    private static final Logger log = LoggerFactory.getLogger(SaleService.class);
    private final SaleRepository saleRepository;

    @Value("${app.reports.seed-initial-data:true}")
    private boolean seedInitialData = true;

    public SaleService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @PostConstruct
    public void init() {
        if (seedInitialData && saleRepository.count() == 0) {
            seedSampleSales();
        }
    }

    public SaleResponse createSale(SaleRequest request) {
        Sale sale = Sale.builder()
                .manager(request.getManager().trim())
                .product(request.getProduct().trim())
                .amount(request.getAmount().setScale(2, RoundingMode.HALF_UP))
                .date(request.getDate())
                .region(request.getRegion().trim())
                .build();

        Sale saved = saleRepository.save(sale);
        log.info("Створено продаж ID: {}, менеджер: {}, сума: {}", saved.getId(), saved.getManager(), saved.getAmount());
        return mapToResponse(saved);
    }

    public List<SaleResponse> getSales(String region, String month, LocalDate from, LocalDate to) {
        LocalDate[] dates = resolveDateRange(month, from, to);
        return saleRepository.findByFilters(region, dates[0], dates[1]).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SaleResponse getSaleById(Long id) {
        return saleRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Продаж з ID " + id + " не знайдено"));
    }

    public void deleteSale(Long id) {
        if (!saleRepository.deleteById(id)) {
            throw new ResourceNotFoundException("Продаж з ID " + id + " не знайдено");
        }
        log.info("Видалено продаж з ID: {}", id);
    }

    public ReportSummaryDto getReportSummary(String month, LocalDate from, LocalDate to, String region) {
        LocalDate[] dates = resolveDateRange(month, from, to);
        LocalDate fromDate = dates[0];
        LocalDate toDate = dates[1];

        List<Sale> filteredSales = saleRepository.findByFilters(region, fromDate, toDate);

        BigDecimal totalAmount = filteredSales.stream()
                .map(Sale::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        long totalCount = filteredSales.size();

        BigDecimal averageCheck = totalCount > 0
                ? totalAmount.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        // Group by Region
        Map<String, List<Sale>> byRegion = filteredSales.stream()
                .collect(Collectors.groupingBy(Sale::getRegion));

        List<RegionSummaryDto> regionSummaries = byRegion.entrySet().stream()
                .map(entry -> {
                    String reg = entry.getKey();
                    List<Sale> sales = entry.getValue();
                    BigDecimal regTotal = sales.stream()
                            .map(Sale::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .setScale(2, RoundingMode.HALF_UP);
                    double percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
                            ? regTotal.divide(totalAmount, 4, RoundingMode.HALF_UP).doubleValue() * 100
                            : 0.0;
                    return RegionSummaryDto.builder()
                            .region(reg)
                            .totalAmount(regTotal)
                            .count(sales.size())
                            .percentage(Math.round(percentage * 100.0) / 100.0)
                            .build();
                })
                .sorted(Comparator.comparing(RegionSummaryDto::getTotalAmount).reversed())
                .collect(Collectors.toList());

        // Group by Manager
        Map<String, List<Sale>> byManager = filteredSales.stream()
                .collect(Collectors.groupingBy(Sale::getManager));

        List<ManagerSummaryDto> managerSummaries = byManager.entrySet().stream()
                .map(entry -> {
                    String mgr = entry.getKey();
                    List<Sale> sales = entry.getValue();
                    BigDecimal mgrTotal = sales.stream()
                            .map(Sale::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .setScale(2, RoundingMode.HALF_UP);
                    double percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
                            ? mgrTotal.divide(totalAmount, 4, RoundingMode.HALF_UP).doubleValue() * 100
                            : 0.0;
                    return ManagerSummaryDto.builder()
                            .manager(mgr)
                            .totalAmount(mgrTotal)
                            .count(sales.size())
                            .percentage(Math.round(percentage * 100.0) / 100.0)
                            .build();
                })
                .sorted(Comparator.comparing(ManagerSummaryDto::getTotalAmount).reversed())
                .collect(Collectors.toList());

        String periodTitle = formatPeriodTitle(month, fromDate, toDate, region);

        String storageType = "InMemory (ConcurrentHashMap)";
        if (saleRepository instanceof com.example.deeployrkd.repository.DynamicSaleRepository dynamic && dynamic.isUsingDatabase()) {
            storageType = "PostgreSQL";
        }

        return ReportSummaryDto.builder()
                .periodTitle(periodTitle)
                .fromDate(fromDate)
                .toDate(toDate)
                .totalAmount(totalAmount)
                .totalSalesCount(totalCount)
                .averageCheck(averageCheck)
                .regionSummaries(regionSummaries)
                .managerSummaries(managerSummaries)
                .sales(filteredSales.stream().map(this::mapToResponse).collect(Collectors.toList()))
                .storageType(storageType)
                .build();
    }

    public LocalDate[] resolveDateRange(String month, LocalDate from, LocalDate to) {
        if (month != null && !month.trim().isEmpty()) {
            try {
                YearMonth ym = YearMonth.parse(month.trim());
                return new LocalDate[]{ym.atDay(1), ym.atEndOfMonth()};
            } catch (DateTimeParseException e) {
                try {
                    if (month.contains(".")) {
                        String[] parts = month.split("\\.");
                        if (parts.length == 2) {
                            YearMonth ym = YearMonth.of(Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
                            return new LocalDate[]{ym.atDay(1), ym.atEndOfMonth()};
                        }
                    }
                } catch (Exception ignored) {
                }
                throw new InvalidPeriodException("Некоректний формат місяця: '" + month + "'. Очікується формат РРРР-ММ (наприклад, 2026-09)");
            }
        }

        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidPeriodException("Початкова дата (" + from + ") не може бути пізнішою за кінцеву дату (" + to + ")");
        }

        return new LocalDate[]{from, to};
    }

    private String formatPeriodTitle(String month, LocalDate from, LocalDate to, String region) {
        StringBuilder sb = new StringBuilder();
        if (month != null && !month.trim().isEmpty()) {
            sb.append("Місяць: ").append(month.trim());
        } else if (from != null && to != null) {
            sb.append("Період: ").append(from.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .append(" — ").append(to.format(DateTimeFormatter.ISO_LOCAL_DATE));
        } else if (from != null) {
            sb.append("З ").append(from.format(DateTimeFormatter.ISO_LOCAL_DATE));
        } else if (to != null) {
            sb.append("До ").append(to.format(DateTimeFormatter.ISO_LOCAL_DATE));
        } else {
            sb.append("Весь час");
        }

        if (region != null && !region.trim().isEmpty()) {
            sb.append(" (Регіон: ").append(region.trim()).append(")");
        }
        return sb.toString();
    }

    private SaleResponse mapToResponse(Sale sale) {
        return SaleResponse.builder()
                .id(sale.getId())
                .manager(sale.getManager())
                .product(sale.getProduct())
                .amount(sale.getAmount())
                .date(sale.getDate())
                .region(sale.getRegion())
                .build();
    }

    private void seedSampleSales() {
        log.info("Ініціалізація тестових даних продажів...");
        LocalDate baseDate = LocalDate.now();
        int year = baseDate.getYear();
        int month = baseDate.getMonthValue();

        List<Sale> sampleList = List.of(
                Sale.builder().manager("Олександр Коваленко").product("Корпоративна ERP-система").amount(new BigDecimal("125000.00")).date(LocalDate.of(year, month, 2)).region("Київ").build(),
                Sale.builder().manager("Марія Шевченко").product("Хмарна CRM Enterprise").amount(new BigDecimal("84000.00")).date(LocalDate.of(year, month, 4)).region("Західний").build(),
                Sale.builder().manager("Дмитро Мельник").product("Модуль аналітики BI Pro").amount(new BigDecimal("45500.00")).date(LocalDate.of(year, month, 5)).region("Центральний").build(),
                Sale.builder().manager("Олена Бондаренко").product("Пакет технічної підтримки 24/7").amount(new BigDecimal("32000.00")).date(LocalDate.of(year, month, 7)).region("Південний").build(),
                Sale.builder().manager("Олександр Коваленко").product("Інтеграційний шлюз API").amount(new BigDecimal("67000.00")).date(LocalDate.of(year, month, 9)).region("Київ").build(),
                Sale.builder().manager("Андрій Кравченко").product("Кібербезпека Endpoint Defense").amount(new BigDecimal("98000.00")).date(LocalDate.of(year, month, 11)).region("Східний").build(),
                Sale.builder().manager("Марія Шевченко").product("Ліцензії хмарного сховища 10TB").amount(new BigDecimal("29500.00")).date(LocalDate.of(year, month, 12)).region("Західний").build(),
                Sale.builder().manager("Дмитро Мельник").product("Корпоративна ERP-система").amount(new BigDecimal("130000.00")).date(LocalDate.of(year, month, 15)).region("Центральний").build(),
                Sale.builder().manager("Олена Бондаренко").product("CRM Starter Pack").amount(new BigDecimal("22000.00")).date(LocalDate.of(year, month, 18)).region("Південний").build(),
                Sale.builder().manager("Олександр Коваленко").product("Модуль автоматизації складу").amount(new BigDecimal("54000.00")).date(LocalDate.of(year, month, 20)).region("Київ").build(),
                Sale.builder().manager("Андрій Кравченко").product("Річна підписка на хмару").amount(new BigDecimal("76000.00")).date(LocalDate.of(year, month, 22)).region("Східний").build(),
                Sale.builder().manager("Марія Шевченко").product("Консалтинг та впровадження").amount(new BigDecimal("48000.00")).date(LocalDate.of(year, month, 25)).region("Західний").build()
        );

        for (Sale sale : sampleList) {
            saleRepository.save(sale);
        }
        log.info("Успішно завантажено {} тестових записів продажів", sampleList.size());
    }
}
