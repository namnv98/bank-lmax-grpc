package com.namnv.core;

import org.apache.kafka.clients.consumer.KafkaConsumer;

public interface CommandLogConsumerProvider {
  KafkaConsumer<String, byte[]> initConsumer(CommandLogKafkaProperties properties);
}
