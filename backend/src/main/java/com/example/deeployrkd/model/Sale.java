package com.example.deeployrkd.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Sale {
    private Long id;
    private String manager;
    private String product;
    private BigDecimal amount;
    private LocalDate date;
    private String region;

    public Sale() {
    }

    public Sale(Long id, String manager, String product, BigDecimal amount, LocalDate date, String region) {
        this.id = id;
        this.manager = manager;
        this.product = product;
        this.amount = amount;
        this.date = date;
        this.region = region;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sale sale = (Sale) o;
        return Objects.equals(id, sale.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Sale{" +
                "id=" + id +
                ", manager='" + manager + '\'' +
                ", product='" + product + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                ", region='" + region + '\'' +
                '}';
    }

    public static class Builder {
        private Long id;
        private String manager;
        private String product;
        private BigDecimal amount;
        private LocalDate date;
        private String region;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder manager(String manager) {
            this.manager = manager;
            return this;
        }

        public Builder product(String product) {
            this.product = product;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder region(String region) {
            this.region = region;
            return this;
        }

        public Sale build() {
            return new Sale(id, manager, product, amount, date, region);
        }
    }
}
