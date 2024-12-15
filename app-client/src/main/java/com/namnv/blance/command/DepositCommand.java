package com.namnv.blance.command;

import java.util.UUID;
import lombok.Data;

@Data
public class DepositCommand implements BalanceCommand {
  private final String correlationId = UUID.randomUUID().toString();
  private Long id;
  private Long amount;
}
