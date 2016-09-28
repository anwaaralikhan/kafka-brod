package com.github.gquintana.kafka.brod;

import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

import java.io.File;

public class EmbeddedKafkaRule extends ExternalResource {
    private final TemporaryFolder temporaryFolder;

    private EmbeddedZookeeper zookeeper;
    private final EmbeddedKafka[] kafkas;

    public EmbeddedKafkaRule(TemporaryFolder temporaryFolder) {
        this(temporaryFolder, 1);
    }

    /**
     * @param kafkaNb Number of Kafka brokers
     */
    public EmbeddedKafkaRule(TemporaryFolder temporaryFolder, int kafkaNb) {
        this.temporaryFolder = temporaryFolder;
        this.kafkas = new EmbeddedKafka[kafkaNb];
    }

    public EmbeddedZookeeper getZookeeper() {
        return zookeeper;
    }

    public EmbeddedKafka getKafka() {
        return kafkas[0];
    }

    @Override
    protected void before() throws Throwable {
        zookeeper = EmbeddedZookeeper.createAndStart(temporaryFolder);
        for (int i = 0; i < kafkas.length; i++) {
            kafkas[i] = EmbeddedKafka.createAndStart(temporaryFolder, i);
        }
    }

    @Override
    protected void after() {
        for(EmbeddedKafka kafka:kafkas) {
            kafka.stop();
        }
        zookeeper.stop();
    }
}
