package com.namnv.core.command;

import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.namnv.core.DisruptorDSL;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandBufferDisruptorDSL implements DisruptorDSL<CommandBufferEvent> {
  private final CommandBufferJournaler commandBufferJournaler;
  private final CommandBufferHandler commandBufferHandler;
  private final CommandBufferReply commandBufferReply;

  @Override
  public Disruptor<CommandBufferEvent> build(int bufferSize, WaitStrategy waitStrategy) {
    Disruptor<CommandBufferEvent> disruptor =
        new Disruptor<CommandBufferEvent>(
            CommandBufferEvent::new,
            bufferSize,
            DaemonThreadFactory.INSTANCE,
            ProducerType.MULTI,
            waitStrategy);
    disruptor
        .handleEventsWith(commandBufferJournaler)
        .then(commandBufferHandler)
        .then(commandBufferReply);
    return disruptor;
  }
}
