package com.example.deeployrkd.dto;

import java.math.BigDecimal;
import java.util.List;

public class EmailReportResponse {
    private boolean success;
    private String message;
    private List<String> recipients;
    private int salesCount;
    private BigDecimal totalAmount;
    private String period;

    public EmailReportResponse() {
    }

    public EmailReportResponse(boolean success, String message, List<String> recipients, int salesCount, BigDecimal totalAmount, String period) {
        this.success = success;
        this.message = message;
        this.recipients = recipients;
        this.salesCount = salesCount;
        this.totalAmount = totalAmount;
        this.period = period;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getRecipients() { return recipients; }
    public void setRecipients(List<String> recipients) { this.recipients = recipients; }

    public int getSalesCount() { return salesCount; }
    public void setSalesCount(int salesCount) { this.salesCount = salesCount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public static class Builder {
        private boolean success;
        private String message;
        private List<String> recipients;
        private int salesCount;
        private BigDecimal totalAmount;
        private String period;

        public Builder success(boolean success) { this.success = success; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder recipients(List<String> recipients) { this.recipients = recipients; return this; }
        public Builder salesCount(int salesCount) { this.salesCount = salesCount; return this; }
        public Builder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public Builder period(String period) { this.period = period; return this; }

        public EmailReportResponse build() {
            return new EmailReportResponse(success, message, recipients, salesCount, totalAmount, period);
        }
    }
}
