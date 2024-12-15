package com.namnv.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cluster.kafka")
public class ClusterKafkaConfig {
  private String bootstrapServers;
  private String topic;
  private String groupId;
}
