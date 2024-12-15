package com.namnv.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode(callSuper = false)
public class BalanceResult extends BaseResult {
  private String message;
  private int code;

  @Override
  public String toString() {
    return String.format("%s::%s", code, message);
  }
}
