package com.namnv.core.replay;

import com.lmax.disruptor.dsl.Disruptor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReplayBufferEventDispatcherImpl implements ReplayBufferEventDispatcher {
  private final Disruptor<ReplayBufferEvent> replayBufferEventDisruptor;

  @Override
  public void dispatch(ReplayBufferEvent replayBufferEvent) {
    replayBufferEventDisruptor.publishEvent(
        ((event, sequence) -> event.setCommand(replayBufferEvent.getCommand())));
  }
}
