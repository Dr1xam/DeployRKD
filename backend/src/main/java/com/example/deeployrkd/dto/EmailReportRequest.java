package com.example.deeployrkd.dto;

import java.time.LocalDate;
import java.util.List;

public class EmailReportRequest {
    private List<String> emails;
    private String month;
    private LocalDate from;
    private LocalDate to;
    private String region;

    public EmailReportRequest() {
    }

    public EmailReportRequest(List<String> emails, String month, LocalDate from, LocalDate to, String region) {
        this.emails = emails;
        this.month = month;
        this.from = from;
        this.to = to;
        this.region = region;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<String> getEmails() { return emails; }
    public void setEmails(List<String> emails) { this.emails = emails; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public LocalDate getFrom() { return from; }
    public void setFrom(LocalDate from) { this.from = from; }

    public LocalDate getTo() { return to; }
    public void setTo(LocalDate to) { this.to = to; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public static class Builder {
        private List<String> emails;
        private String month;
        private LocalDate from;
        private LocalDate to;
        private String region;

        public Builder emails(List<String> emails) { this.emails = emails; return this; }
        public Builder month(String month) { this.month = month; return this; }
        public Builder from(LocalDate from) { this.from = from; return this; }
        public Builder to(LocalDate to) { this.to = to; return this; }
        public Builder region(String region) { this.region = region; return this; }

        public EmailReportRequest build() {
            return new EmailReportRequest(emails, month, from, to, region);
        }
    }
}
