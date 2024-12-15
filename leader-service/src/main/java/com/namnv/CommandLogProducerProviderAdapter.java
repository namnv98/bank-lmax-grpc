package com.namnv;

import com.namnv.core.ClusterKafkaConfig;
import com.namnv.core.CommandLogKafkaProperties;
import com.namnv.core.CommandLogProducerProvider;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandLogProducerProviderAdapter implements CommandLogProducerProvider {

  private final ClusterKafkaConfig clusterKafkaConfig;

  @Override
  public KafkaProducer<String, byte[]> initProducer(CommandLogKafkaProperties properties) {
    var props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, clusterKafkaConfig.getBootstrapServers());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
    props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
    return new KafkaProducer<>(props);
  }
}
