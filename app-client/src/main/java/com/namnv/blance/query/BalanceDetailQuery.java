package com.namnv.blance.query;

import java.util.UUID;
import lombok.Data;

@Data
public class BalanceDetailQuery implements BalanceQuery {
  private final String correlationId = UUID.randomUUID().toString();
  private Long id;

  public BalanceDetailQuery() {}

  public BalanceDetailQuery(Long id) {
    this.id = id;
  }
}
