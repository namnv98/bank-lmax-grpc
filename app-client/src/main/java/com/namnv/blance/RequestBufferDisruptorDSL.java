package com.namnv.blance;

import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.namnv.blance.core.RequestBufferEvent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RequestBufferDisruptorDSL implements DisruptorDSL<RequestBufferEvent> {

  private final RequestBufferHandler requestBufferHandler;

  @Override
  public Disruptor<RequestBufferEvent> build(int bufferSize, WaitStrategy waitStrategy) {
    Disruptor disruptor =
        new Disruptor<>(
            RequestBufferEvent::new,
            bufferSize,
            DaemonThreadFactory.INSTANCE,
            ProducerType.MULTI,
            waitStrategy);
    disruptor.handleEventsWith(requestBufferHandler);
    return disruptor;
  }
}
