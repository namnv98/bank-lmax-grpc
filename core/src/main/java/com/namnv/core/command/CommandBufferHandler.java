package com.namnv.core.command;

import com.lmax.disruptor.EventHandler;

public interface CommandBufferHandler
    extends EventHandler<CommandBufferEvent>, CommandBufferChannel {}
