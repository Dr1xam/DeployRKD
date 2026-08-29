package com.example.deeployrkd.repository;

import com.example.deeployrkd.model.Sale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgresSaleRepository implements SaleRepository {

    private static final Logger log = LoggerFactory.getLogger(PostgresSaleRepository.class);
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Sale> rowMapper = (rs, rowNum) -> Sale.builder()
            .id(rs.getLong("id"))
            .manager(rs.getString("manager"))
            .product(rs.getString("product"))
            .amount(rs.getBigDecimal("amount"))
            .date(rs.getDate("date").toLocalDate())
            .region(rs.getString("region"))
            .build();

    public PostgresSaleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        initSchema();
    }

    private void initSchema() {
        String sql = "CREATE TABLE IF NOT EXISTS sales (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "manager VARCHAR(255) NOT NULL, " +
                "product VARCHAR(255) NOT NULL, " +
                "amount NUMERIC(19, 2) NOT NULL, " +
                "date DATE NOT NULL, " +
                "region VARCHAR(100) NOT NULL" +
                ");";
        jdbcTemplate.execute(sql);
        log.info("Таблиця 'sales' у PostgreSQL перевірена / створена.");
    }

    @Override
    public Sale save(Sale sale) {
        if (sale.getId() == null) {
            String sql = "INSERT INTO sales (manager, product, amount, date, region) VALUES (?, ?, ?, ?, ?) RETURNING id";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            Long id = jdbcTemplate.queryForObject(
                    sql,
                    Long.class,
                    sale.getManager(),
                    sale.getProduct(),
                    sale.getAmount(),
                    Date.valueOf(sale.getDate()),
                    sale.getRegion()
            );

            sale.setId(id);
            return sale;
        } else {
            String sql = "UPDATE sales SET manager = ?, product = ?, amount = ?, date = ?, region = ? WHERE id = ?";
            jdbcTemplate.update(
                    sql,
                    sale.getManager(),
                    sale.getProduct(),
                    sale.getAmount(),
                    Date.valueOf(sale.getDate()),
                    sale.getRegion(),
                    sale.getId()
            );
            return sale;
        }
    }

    @Override
    public Optional<Sale> findById(Long id) {
        String sql = "SELECT id, manager, product, amount, date, region FROM sales WHERE id = ?";
        List<Sale> results = jdbcTemplate.query(sql, rowMapper, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<Sale> findAll() {
        String sql = "SELECT id, manager, product, amount, date, region FROM sales ORDER BY date DESC, id DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<Sale> findByFilters(String region, LocalDate from, LocalDate to) {
        StringBuilder sql = new StringBuilder("SELECT id, manager, product, amount, date, region FROM sales WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (region != null && !region.trim().isEmpty()) {
            sql.append(" AND LOWER(region) = LOWER(?)");
            params.add(region.trim());
        }
        if (from != null) {
            sql.append(" AND date >= ?");
            params.add(Date.valueOf(from));
        }
        if (to != null) {
            sql.append(" AND date <= ?");
            params.add(Date.valueOf(to));
        }

        sql.append(" ORDER BY date DESC, id DESC");
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM sales WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public void clear() {
        jdbcTemplate.update("DELETE FROM sales");
    }

    @Override
    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sales", Long.class);
        return count != null ? count : 0L;
    }
}
