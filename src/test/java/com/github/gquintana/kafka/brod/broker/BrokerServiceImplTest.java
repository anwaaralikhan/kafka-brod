package com.github.gquintana.kafka.brod.broker;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.github.gquintana.kafka.brod.EmbeddedKafkaRule;
import com.github.gquintana.kafka.brod.KafkaService;
import com.github.gquintana.kafka.brod.ZookeeperService;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class BrokerServiceImplTest {

    @ClassRule
    public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

    @ClassRule
    public static final EmbeddedKafkaRule KAFKA_RULE = new EmbeddedKafkaRule(TEMPORARY_FOLDER, 2);

    private static BrokerServiceImpl brokerService;

    @BeforeClass
    public static void setUpClass() throws Exception {
        ZookeeperService zookeeperService = new ZookeeperService("localhost:2181", 3000, 3000);
        KafkaService kafkaService = new KafkaService(KAFKA_RULE.bootstrapServers(), "broker-service-test");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        brokerService = new BrokerServiceImpl(zookeeperService, objectMapper, 1000, kafkaService);
    }

    @Test
    public void testGetBrokers() throws Exception {
        // When
        List<Broker> brokers = brokerService.getBrokers();
        // Then
        assertThat(brokers, hasSize(2));
        assertThat(brokers.stream().map(Broker::getId).collect(toList()), hasItems(0, 1));
        assertThat(brokers.stream().map(Broker::getHost).distinct().collect(toList()), hasSize(1));
    }

    @Test
    public void testParseBrokerPlainText() throws Exception {
        // Given
        String json = "{\"jmx_port\":-1,\"timestamp\":\"1474316866507\",\"endpoints\":[\"PLAINTEXT://kafkahost1:9092\"],\"host\":\"kafkahost1\",\"version\":3,\"port\":9092}";
        // When
        Broker broker = brokerService.parseBroker(1, json);
        // Then
        assertThat(broker.getHost(), equalTo("kafkahost1"));
        assertThat(broker.getPort(), equalTo(9092));
        assertThat(broker.getJmxPort(), nullValue());
        assertThat(broker.getEndpoints().size(), equalTo(1));
    }

    @Test
    public void testParseBrokerSslAndJmx() throws Exception {
        // Given
        String json = "{\"jmx_port\":9999,\"timestamp\":\"1474317250472\",\"endpoints\":[\"SSL://kafkahost1:9092\"],\"host\":null,\"version\":3,\"port\":-1}";
        // When
        Broker broker = brokerService.parseBroker(1, json);
        // Then
        assertThat(broker.getHost(), equalTo("kafkahost1"));
        assertThat(broker.getPort(), equalTo(9092));
        assertThat(broker.getJmxPort(), equalTo(9999));
        assertThat(broker.getEndpoints().size(), equalTo(1));
    }

    @Test
    public void testParseBrokerPlainAndSsl() throws Exception {
        // Given
        String json = "{\"jmx_port\":-1,\"timestamp\":\"1474317250472\",\"endpoints\":[\"PLAINTEXT://:9092\",\"SSL://kafkahost2:9093\"],\"host\":null,\"version\":3,\"port\":-1}";
        // When
        Broker broker = brokerService.parseBroker(1, json);
        // Then
        assertThat(broker.getHost(), equalTo("kafkahost2"));
        assertThat(broker.getPort(), nullValue());
        assertThat(broker.getJmxPort(), nullValue());
        assertThat(broker.getEndpoints().size(), equalTo(2));
    }

    @Test
    public void testGetBroker() throws Exception {
        // When
        Broker broker = brokerService.getBroker(0).get();
        // Then
        assertThat(broker, notNullValue());
        assertThat(broker.getId(), is(0));
        assertThat(broker.getPort(), equalTo(9092));
        assertThat(broker.getEndpoints().size(), equalTo(1));
        assertThat(broker.getController(), is(true));
        assertThat(broker.getAvailable(), is(true));

    }

    @Test
    public void testController() throws Exception {
        // When
        Optional<Broker> controller = brokerService.getControllerBroker();
        int controllerId = controller.get().getId();
        // Then
        assertThat(controllerId, equalTo(0));

    }

}
