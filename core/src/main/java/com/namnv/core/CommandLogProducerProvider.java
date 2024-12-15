package com.namnv.core;

import org.apache.kafka.clients.producer.KafkaProducer;

public interface CommandLogProducerProvider {
  KafkaProducer<String, byte[]> initProducer(CommandLogKafkaProperties properties);
}
