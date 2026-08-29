package com.example.deeployrkd.repository;

import com.example.deeployrkd.model.Sale;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class DynamicSaleRepository implements SaleRepository {

    private static final Logger log = LoggerFactory.getLogger(DynamicSaleRepository.class);

    private final InMemorySaleRepository inMemoryRepository;

    @Value("${DB_HOST:${spring.datasource.host:}}")
    private String dbHost;

    @Value("${DB_PORT:${spring.datasource.port:5432}}")
    private String dbPort;

    @Value("${DB_NAME:${spring.datasource.dbname:postgres}}")
    private String dbName;

    @Value("${DB_USERNAME:${spring.datasource.username:postgres}}")
    private String dbUser;

    @Value("${DB_PASSWORD:${spring.datasource.password:postgres}}")
    private String dbPassword;

    private SaleRepository activeRepository;
    private HikariDataSource dataSource;

    public DynamicSaleRepository(InMemorySaleRepository inMemoryRepository) {
        this.inMemoryRepository = inMemoryRepository;
        this.activeRepository = inMemoryRepository;
    }

    @PostConstruct
    public void initialize() {
        if (dbHost == null || dbHost.isBlank()) {
            log.info("ℹ️ DB_HOST не вказано. Використовується сховище в пам'яті InMemory (ConcurrentHashMap).");
            this.activeRepository = inMemoryRepository;
            return;
        }

        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", dbHost.trim(), dbPort.trim(), dbName.trim());
        log.info("Спроба підключення до PostgreSQL: {}", jdbcUrl);

        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(dbUser != null ? dbUser.trim() : "postgres");
            config.setPassword(dbPassword != null ? dbPassword.trim() : "postgres");
            config.setDriverClassName("org.postgresql.Driver");
            config.setConnectionTimeout(2000); // 2 seconds timeout
            config.setValidationTimeout(1500);
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);

            this.dataSource = new HikariDataSource(config);

            // Test connection
            try (var conn = dataSource.getConnection()) {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                this.activeRepository = new PostgresSaleRepository(jdbcTemplate);
                log.info("✅ Успішно підключено до PostgreSQL! Використовується реальна база даних (таблиця 'sales').");
            }
        } catch (Exception e) {
            log.warn("⚠️ Не вдалося з'єднатися з PostgreSQL ({}): {}. Автоматично перемикаємось на InMemorySaleRepository (ConcurrentHashMap).",
                    e.getClass().getSimpleName(), e.getMessage());
            if (this.dataSource != null && !this.dataSource.isClosed()) {
                this.dataSource.close();
                this.dataSource = null;
            }
            this.activeRepository = inMemoryRepository;
        }
    }

    @PreDestroy
    public void cleanup() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public boolean isUsingDatabase() {
        return activeRepository instanceof PostgresSaleRepository;
    }

    @Override
    public Sale save(Sale sale) {
        return activeRepository.save(sale);
    }

    @Override
    public Optional<Sale> findById(Long id) {
        return activeRepository.findById(id);
    }

    @Override
    public List<Sale> findAll() {
        return activeRepository.findAll();
    }

    @Override
    public List<Sale> findByFilters(String region, LocalDate from, LocalDate to) {
        return activeRepository.findByFilters(region, from, to);
    }

    @Override
    public boolean deleteById(Long id) {
        return activeRepository.deleteById(id);
    }

    @Override
    public void clear() {
        activeRepository.clear();
    }

    @Override
    public long count() {
        return activeRepository.count();
    }
}
