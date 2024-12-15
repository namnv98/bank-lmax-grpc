package com.namnv;

import java.util.List;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public class ENVStart {

  public static void main(String[] args) {
    KafkaContainer();
    MySQLContainer();
    while (true) {}
  }

  private static void KafkaContainer() {
    KafkaContainer kafka =
        new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"))
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
            .withEnv("KAFKA_CREATE_TOPICS", "kafka_topic");

    int hostPort = 9093;
    int containerPort = 9093;
    kafka.setPortBindings(List.of(hostPort + ":" + containerPort));

    kafka.start();
    var bootstrapServers = kafka.getBootstrapServers();
    System.out.println(bootstrapServers);
  }

  private static void MySQLContainer() {
    var mySQLContainer =
        new MySQLContainer(DockerImageName.parse("mysql:latest"))
            .withDatabaseName("p2pc")
            .withInitScript("init.sql")
            .withUsername("root")
            .withPassword("root");

    int hostPort = 3306;
    int containerPort = 3306;
    mySQLContainer.setPortBindings(List.of(hostPort + ":" + containerPort));

    mySQLContainer.start();
    System.out.println(
        ((JdbcDatabaseContainer<?>) mySQLContainer).getExposedPorts().getFirst().toString());
  }
}
