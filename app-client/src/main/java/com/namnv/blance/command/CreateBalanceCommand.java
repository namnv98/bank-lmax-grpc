package com.namnv.blance.command;

import java.util.UUID;
import lombok.Data;

@Data
public class CreateBalanceCommand implements BalanceCommand {
  private final String correlationId = UUID.randomUUID().toString();
}
