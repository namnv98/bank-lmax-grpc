package com.namnv.core.replay;

import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.namnv.core.DisruptorDSL;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReplayBufferDisruptorDSL implements DisruptorDSL<ReplayBufferEvent> {
  private final ReplayBufferHandler replayBufferHandler;

  @Override
  public Disruptor<ReplayBufferEvent> build(int bufferSize, WaitStrategy waitStrategy) {
    var disruptor =
        new Disruptor<>(
            ReplayBufferEvent::new,
            bufferSize,
            DaemonThreadFactory.INSTANCE,
            ProducerType.SINGLE,
            waitStrategy);
    disruptor.handleEventsWith(replayBufferHandler);
    return disruptor;
  }
}
