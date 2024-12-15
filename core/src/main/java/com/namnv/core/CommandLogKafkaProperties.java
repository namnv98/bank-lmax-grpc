package com.namnv.core;

import lombok.Data;

@Data
public class CommandLogKafkaProperties {
  private String topic;
  private String groupId;
  private long nextOffset;
}
