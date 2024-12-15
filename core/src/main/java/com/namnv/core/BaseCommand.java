package com.namnv.core;

import com.namnv.proto.BalanceProto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BaseCommand {
  private BalanceProto.CommandLog commandLog;
}
