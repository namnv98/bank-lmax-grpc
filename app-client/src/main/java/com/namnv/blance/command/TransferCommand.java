package com.namnv.blance.command;

import java.util.UUID;
import lombok.Data;

@Data
public class TransferCommand implements BalanceCommand {
  private final String correlationId = UUID.randomUUID().toString();
  private Long fromId;
  private Long toId;
  private Long amount;
}
