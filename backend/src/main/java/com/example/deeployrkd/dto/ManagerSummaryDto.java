package com.example.deeployrkd.dto;

import java.math.BigDecimal;

public class ManagerSummaryDto {
    private String manager;
    private BigDecimal totalAmount;
    private long count;
    private double percentage;

    public ManagerSummaryDto() {
    }

    public ManagerSummaryDto(String manager, BigDecimal totalAmount, long count, double percentage) {
        this.manager = manager;
        this.totalAmount = totalAmount;
        this.count = count;
        this.percentage = percentage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public static class Builder {
        private String manager;
        private BigDecimal totalAmount;
        private long count;
        private double percentage;

        public Builder manager(String manager) { this.manager = manager; return this; }
        public Builder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public Builder count(long count) { this.count = count; return this; }
        public Builder percentage(double percentage) { this.percentage = percentage; return this; }

        public ManagerSummaryDto build() {
            return new ManagerSummaryDto(manager, totalAmount, count, percentage);
        }
    }
}
