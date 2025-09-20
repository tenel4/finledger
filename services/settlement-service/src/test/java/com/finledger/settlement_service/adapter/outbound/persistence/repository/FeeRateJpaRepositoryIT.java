package com.finledger.settlement_service.adapter.outbound.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.finledger.settlement_service.adapter.outbound.persistance.repository.FeeRateJpaRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@JdbcTest // loads JdbcTemplate and DataSource
@Import(FeeRateJpaRepository.class) // bring in the class under test
class FeeRateJpaRepositoryIT {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void configureProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private FeeRateJpaRepository repository;

  @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

  @Test
  void getFeeRateForProduct_shouldReturnRateFromDatabase() {
    // Arrange: create table and insert test data
    jdbcTemplate.execute(
        """
            CREATE TABLE fee_rates (
                product_code VARCHAR(50) PRIMARY KEY,
                rate NUMERIC(10,6) NOT NULL
            )
        """);
    jdbcTemplate.update(
        "INSERT INTO fee_rates (product_code, rate) VALUES (?, ?)",
        "AAPL",
        BigDecimal.valueOf(0.05));

    // Act
    BigDecimal rate = repository.getFeeRateForProduct("AAPL");

    // Assert
    assertThat(rate).isEqualByComparingTo(BigDecimal.valueOf(0.05));
  }
}
