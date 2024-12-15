package com.namnv.blance;

import com.lmax.disruptor.EventHandler;
import com.namnv.blance.core.RequestBufferChannel;
import com.namnv.blance.core.RequestBufferEvent;

public interface RequestBufferHandler
    extends EventHandler<RequestBufferEvent>, RequestBufferChannel {}
