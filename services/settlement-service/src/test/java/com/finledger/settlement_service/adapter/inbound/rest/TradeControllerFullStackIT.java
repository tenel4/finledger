package com.finledger.settlement_service.adapter.inbound.rest;

import com.finledger.settlement_service.adapter.inbound.rest.dto.CreateTradeRequest;
import com.finledger.settlement_service.adapter.inbound.rest.dto.CreateTradeResponse;
import com.finledger.settlement_service.adapter.inbound.rest.dto.GetTradesResponse;
import com.finledger.settlement_service.domain.model.Trade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
class TradeControllerFullStackIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private TestRestTemplate rest;

    @Test
    void createTrade_thenListTrades_endToEnd() {
        // 1) Create trade via HTTP
        CreateTradeRequest request = new CreateTradeRequest(
                "AAPL",
                Trade.Side.BUY,
                10,
                BigDecimal.valueOf(150.25),
                "USD",
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        ResponseEntity<CreateTradeResponse> createResponse =
                rest.postForEntity("/api/trades", request, CreateTradeResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();

        UUID createdId = createResponse.getBody().id();
        Instant createdTime = createResponse.getBody().tradeTime();
        UUID messageId = createResponse.getBody().messageId();

        assertThat(createdId).isNotNull();
        assertThat(createdTime).isNotNull();
        assertThat(messageId).isNotNull();

        String today = LocalDate.now().toString();
        String tomorrow = LocalDate.now().plusDays(1).toString();

        // 2) List trades via HTTP
        ResponseEntity<GetTradesResponse[]> listResponse =
                rest.getForEntity("/api/trades?symbol=AAPL&from=" + today + "&to=" + tomorrow + "&side=BUY", GetTradesResponse[].class);

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotEmpty();

        GetTradesResponse response = listResponse.getBody()[0];

        // 3) Validate stored trade matches response
        assertThat(response.id()).isEqualTo(createdId);
        assertThat(response.symbol()).isEqualTo("AAPL");
        assertThat(response.side()).isEqualTo(Trade.Side.BUY);
        assertThat(response.quantity()).isEqualTo(10L);
        assertThat(response.price()).isEqualByComparingTo("150.25");
    }
}
