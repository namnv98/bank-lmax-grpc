package com.namnv.core.reply;

import com.lmax.disruptor.dsl.Disruptor;

public class ReplyBufferEventDispatcherImpl implements ReplyBufferEventDispatcher {

  private final Disruptor<ReplyBufferEvent> replyBufferEventDisruptor;

  public ReplyBufferEventDispatcherImpl(Disruptor<ReplyBufferEvent> replyBufferEventDisruptor) {
    this.replyBufferEventDisruptor = replyBufferEventDisruptor;
  }

  @Override
  public void dispatch(ReplyBufferEvent replyBufferEvent) {
    replyBufferEventDisruptor.publishEvent(
        (event, sequence) -> {
          event.setCorrelationId(replyBufferEvent.getCorrelationId());
          event.setResult(replyBufferEvent.getResult());
          event.setReplyChannel(replyBufferEvent.getReplyChannel());
        });
  }
}
