package com.namnv.core.command;

import com.namnv.core.reply.ReplyBufferEvent;
import com.namnv.core.reply.ReplyBufferEventDispatcher;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandBufferReplyImpl implements CommandBufferReply {
  private final ReplyBufferEventDispatcher replyBufferEventDispatcher;

  @Override
  public void onEvent(CommandBufferEvent event, long sequence, boolean endOfBatch) {
    replyBufferEventDispatcher.dispatch(
        new ReplyBufferEvent(event.getReplyChannel(), event.getCorrelationId(), event.getResult()));
  }
}
