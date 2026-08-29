package com.example.deeployrkd.dto;

import java.math.BigDecimal;

public class RegionSummaryDto {
    private String region;
    private BigDecimal totalAmount;
    private long count;
    private double percentage;

    public RegionSummaryDto() {
    }

    public RegionSummaryDto(String region, BigDecimal totalAmount, long count, double percentage) {
        this.region = region;
        this.totalAmount = totalAmount;
        this.count = count;
        this.percentage = percentage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public static class Builder {
        private String region;
        private BigDecimal totalAmount;
        private long count;
        private double percentage;

        public Builder region(String region) { this.region = region; return this; }
        public Builder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public Builder count(long count) { this.count = count; return this; }
        public Builder percentage(double percentage) { this.percentage = percentage; return this; }

        public RegionSummaryDto build() {
            return new RegionSummaryDto(region, totalAmount, count, percentage);
        }
    }
}
