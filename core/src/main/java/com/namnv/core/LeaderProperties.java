package com.namnv.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderProperties {
  private int commandBufferSize = 1 << 15;
  private int replyBufferSize = 1 << 16;
  private int logsChunkSize = 200;
}
