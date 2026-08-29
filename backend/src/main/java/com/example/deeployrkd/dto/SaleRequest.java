package com.example.deeployrkd.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SaleRequest {

    @NotBlank(message = "Ім'я менеджера є обов'язковим")
    private String manager;

    @NotBlank(message = "Назва товару є обов'язковою")
    private String product;

    @NotNull(message = "Сума продажу є обов'язковою")
    @DecimalMin(value = "0.01", message = "Сума продажу має бути більшою за 0")
    private BigDecimal amount;

    @NotNull(message = "Дата продажу є обов'язковою")
    private LocalDate date;

    @NotBlank(message = "Регіон є обов'язковим")
    private String region;

    public SaleRequest() {
    }

    public SaleRequest(String manager, String product, BigDecimal amount, LocalDate date, String region) {
        this.manager = manager;
        this.product = product;
        this.amount = amount;
        this.date = date;
        this.region = region;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public static class Builder {
        private String manager;
        private String product;
        private BigDecimal amount;
        private LocalDate date;
        private String region;

        public Builder manager(String manager) { this.manager = manager; return this; }
        public Builder product(String product) { this.product = product; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder date(LocalDate date) { this.date = date; return this; }
        public Builder region(String region) { this.region = region; return this; }

        public SaleRequest build() {
            return new SaleRequest(manager, product, amount, date, region);
        }
    }
}
