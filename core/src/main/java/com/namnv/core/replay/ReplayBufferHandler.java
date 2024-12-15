package com.namnv.core.replay;

import com.lmax.disruptor.EventHandler;

public interface ReplayBufferHandler extends EventHandler<ReplayBufferEvent>, ReplayBufferChanel {}
