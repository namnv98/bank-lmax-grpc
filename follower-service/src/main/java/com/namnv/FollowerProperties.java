package com.namnv;

import lombok.Data;

@Data
public class FollowerProperties {
  private int bufferSize = 1 << 10;
  private int pollingInterval = 100;
}
