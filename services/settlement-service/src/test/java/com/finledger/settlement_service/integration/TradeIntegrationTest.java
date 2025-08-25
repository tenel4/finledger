package com.finledger.settlement_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finledger.settlement_service.model.Trade;
import com.finledger.settlement_service.model.dto.CreateTradeRequest;
import com.finledger.settlement_service.model.dto.CreateTradeResponse;
import com.finledger.settlement_service.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static com.finledger.settlement_service.config.RabbitConfig.*;
import static com.finledger.settlement_service.model.enums.Side.*;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TradeIntegrationTest {

    @SuppressWarnings("resource")
    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("finledger")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    public static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management")
            .withExposedPorts(5672, 15672);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TradeRepository tradeRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void createTrade_shouldPersistTradeAndPublishEvent() throws Exception {
        CreateTradeRequest request = new CreateTradeRequest("AAPL", BUY, 100L, BigDecimal.valueOf(785.00), "USD", UUID.randomUUID(), UUID.randomUUID());
        CreateTradeResponse response =  webTestClient.post().uri("/api/trades")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(request))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CreateTradeResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.tradeTime()).isNotNull();
        assertThat(response.messageKey()).isNotNull();

        Trade saved = tradeRepository.findAll().getFirst();
        assertThat(saved.getSymbol()).isEqualTo(request.symbol());
        assertThat(saved.getSide()).isEqualTo(request.side());

        Object receivedMessage = rabbitTemplate.receiveAndConvert(TRADE_QUEUE);
        assertThat(receivedMessage).isNotNull();
    }
}
