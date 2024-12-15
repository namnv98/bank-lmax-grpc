package com.namnv.core.replay;

import com.namnv.core.BufferEventDispatcher;

public interface ReplayBufferEventDispatcher
    extends ReplayBufferChanel, BufferEventDispatcher<ReplayBufferEvent> {}
