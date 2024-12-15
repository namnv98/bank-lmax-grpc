package com.namnv.core.reply;

import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.namnv.core.DisruptorDSL;

public class ReplyBufferDisruptorDSL implements DisruptorDSL<ReplyBufferEvent> {
  private final ReplyBufferHandler replyBufferHandler;

  public ReplyBufferDisruptorDSL(ReplyBufferHandler replyBufferHandler) {
    this.replyBufferHandler = replyBufferHandler;
  }

  @Override
  public Disruptor<ReplyBufferEvent> build(int bufferSize, WaitStrategy waitStrategy) {
    Disruptor disruptor =
        new Disruptor<>(
            ReplyBufferEvent::new,
            bufferSize,
            DaemonThreadFactory.INSTANCE,
            ProducerType.SINGLE,
            waitStrategy);
    disruptor.handleEventsWith(replyBufferHandler);
    return disruptor;
  }
}
