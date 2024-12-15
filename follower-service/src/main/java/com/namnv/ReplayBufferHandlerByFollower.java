package com.namnv;

import com.namnv.core.command.CommandHandler;
import com.namnv.core.replay.ReplayBufferEvent;
import com.namnv.core.replay.ReplayBufferHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReplayBufferHandlerByFollower implements ReplayBufferHandler {

  private final CommandHandler commandHandler;

  @Override
  public void onEvent(ReplayBufferEvent event, long sequence, boolean endOfBatch) throws Exception {
    commandHandler.onCommand(event.getCommand());
  }
}
