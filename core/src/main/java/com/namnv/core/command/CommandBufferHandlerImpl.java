package com.namnv.core.command;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandBufferHandlerImpl implements CommandBufferHandler {

  private final CommandHandler commandHandler;

  @Override
  public void onEvent(CommandBufferEvent event, long sequence, boolean endOfBatch)
      throws Exception {
    event.setResult(commandHandler.onCommand(event.getCommand()));
  }
}
