package com.finledger.settlement_service.adapter.outbound.persistance.repository;

import com.finledger.settlement_service.application.port.outbound.FeeRateRepositoryPort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class FeeRateJpaRepository implements FeeRateRepositoryPort {

    private final JdbcTemplate jdbcTemplate;

    public FeeRateJpaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Cacheable("feeRates") // Spring Cache abstraction
    public BigDecimal getFeeRateForProduct(String productCode) {
        return jdbcTemplate.queryForObject(
                "SELECT rate FROM fee_rates WHERE product_code = ?",
                BigDecimal.class,
                productCode
        );
    }
}
