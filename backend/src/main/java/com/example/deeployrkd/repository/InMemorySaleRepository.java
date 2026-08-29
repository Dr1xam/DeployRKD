package com.example.deeployrkd.repository;

import com.example.deeployrkd.model.Sale;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemorySaleRepository implements SaleRepository {

    private final Map<Long, Sale> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public Sale save(Sale sale) {
        if (sale.getId() == null) {
            sale.setId(idSequence.getAndIncrement());
        }
        storage.put(sale.getId(), sale);
        return sale;
    }

    @Override
    public Optional<Sale> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Sale> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(Sale::getDate).reversed().thenComparing(Sale::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Sale> findByFilters(String region, LocalDate from, LocalDate to) {
        return storage.values().stream()
                .filter(sale -> {
                    if (region != null && !region.trim().isEmpty()) {
                        if (!sale.getRegion().equalsIgnoreCase(region.trim())) {
                            return false;
                        }
                    }
                    if (from != null && sale.getDate().isBefore(from)) {
                        return false;
                    }
                    if (to != null && sale.getDate().isAfter(to)) {
                        return false;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(Sale::getDate).reversed().thenComparing(Sale::getId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }

    @Override
    public void clear() {
        storage.clear();
        idSequence.set(1);
    }

    @Override
    public long count() {
        return storage.size();
    }
}
