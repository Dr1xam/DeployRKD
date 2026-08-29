package com.example.deeployrkd.repository;

import com.example.deeployrkd.model.Sale;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SaleRepository {
    Sale save(Sale sale);
    Optional<Sale> findById(Long id);
    List<Sale> findAll();
    List<Sale> findByFilters(String region, LocalDate from, LocalDate to);
    boolean deleteById(Long id);
    void clear();
    long count();
}
