package com.namnv.core.replay;

import com.namnv.core.BaseCommand;
import com.namnv.core.BufferEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplayBufferEvent implements BufferEvent {
  private BaseCommand command;
}
