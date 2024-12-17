package com.namnv.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandLogKafkaProperties {
  private String topic;
  private String groupId;
  private long nextOffset;
}
