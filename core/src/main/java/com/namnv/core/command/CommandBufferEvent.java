package com.namnv.core.command;

import com.namnv.core.BaseCommand;
import com.namnv.core.BaseResult;
import com.namnv.core.BufferEvent;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommandBufferEvent implements BufferEvent {
  private String replyChannel;
  private String correlationId;

  private BaseCommand command;
  private BaseResult result;

  public CommandBufferEvent(String replyChannel, String correlationId, BaseCommand command) {
    this.replyChannel = replyChannel;
    this.correlationId = correlationId;
    this.command = command;
  }

  public void copy(CommandBufferEvent event) {
    this.replyChannel = event.replyChannel;
    this.correlationId = event.correlationId;
    this.command = event.command;
    this.result = event.result;
  }
}
