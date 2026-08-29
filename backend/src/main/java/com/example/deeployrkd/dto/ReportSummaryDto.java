package com.example.deeployrkd.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReportSummaryDto {
    private String periodTitle;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal totalAmount;
    private long totalSalesCount;
    private BigDecimal averageCheck;
    private List<RegionSummaryDto> regionSummaries;
    private List<ManagerSummaryDto> managerSummaries;
    private List<SaleResponse> sales;
    private String storageType;

    public ReportSummaryDto() {
    }

    public ReportSummaryDto(String periodTitle, LocalDate fromDate, LocalDate toDate, BigDecimal totalAmount, long totalSalesCount, BigDecimal averageCheck, List<RegionSummaryDto> regionSummaries, List<ManagerSummaryDto> managerSummaries, List<SaleResponse> sales, String storageType) {
        this.periodTitle = periodTitle;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.totalAmount = totalAmount;
        this.totalSalesCount = totalSalesCount;
        this.averageCheck = averageCheck;
        this.regionSummaries = regionSummaries;
        this.managerSummaries = managerSummaries;
        this.sales = sales;
        this.storageType = storageType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getPeriodTitle() { return periodTitle; }
    public void setPeriodTitle(String periodTitle) { this.periodTitle = periodTitle; }

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public long getTotalSalesCount() { return totalSalesCount; }
    public void setTotalSalesCount(long totalSalesCount) { this.totalSalesCount = totalSalesCount; }

    public BigDecimal getAverageCheck() { return averageCheck; }
    public void setAverageCheck(BigDecimal averageCheck) { this.averageCheck = averageCheck; }

    public List<RegionSummaryDto> getRegionSummaries() { return regionSummaries; }
    public void setRegionSummaries(List<RegionSummaryDto> regionSummaries) { this.regionSummaries = regionSummaries; }

    public List<ManagerSummaryDto> getManagerSummaries() { return managerSummaries; }
    public void setManagerSummaries(List<ManagerSummaryDto> managerSummaries) { this.managerSummaries = managerSummaries; }

    public List<SaleResponse> getSales() { return sales; }
    public void setSales(List<SaleResponse> sales) { this.sales = sales; }

    public String getStorageType() { return storageType; }
    public void setStorageType(String storageType) { this.storageType = storageType; }

    public static class Builder {
        private String periodTitle;
        private LocalDate fromDate;
        private LocalDate toDate;
        private BigDecimal totalAmount;
        private long totalSalesCount;
        private BigDecimal averageCheck;
        private List<RegionSummaryDto> regionSummaries;
        private List<ManagerSummaryDto> managerSummaries;
        private List<SaleResponse> sales;
        private String storageType;

        public Builder periodTitle(String periodTitle) { this.periodTitle = periodTitle; return this; }
        public Builder fromDate(LocalDate fromDate) { this.fromDate = fromDate; return this; }
        public Builder toDate(LocalDate toDate) { this.toDate = toDate; return this; }
        public Builder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public Builder totalSalesCount(long totalSalesCount) { this.totalSalesCount = totalSalesCount; return this; }
        public Builder averageCheck(BigDecimal averageCheck) { this.averageCheck = averageCheck; return this; }
        public Builder regionSummaries(List<RegionSummaryDto> regionSummaries) { this.regionSummaries = regionSummaries; return this; }
        public Builder managerSummaries(List<ManagerSummaryDto> managerSummaries) { this.managerSummaries = managerSummaries; return this; }
        public Builder sales(List<SaleResponse> sales) { this.sales = sales; return this; }
        public Builder storageType(String storageType) { this.storageType = storageType; return this; }

        public ReportSummaryDto build() {
            return new ReportSummaryDto(periodTitle, fromDate, toDate, totalAmount, totalSalesCount, averageCheck, regionSummaries, managerSummaries, sales, storageType);
        }
    }
}
