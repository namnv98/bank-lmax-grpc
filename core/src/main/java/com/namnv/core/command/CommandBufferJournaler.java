package com.namnv.core.command;

import com.lmax.disruptor.EventHandler;

public interface CommandBufferJournaler
    extends EventHandler<CommandBufferEvent>, CommandBufferChannel {}
