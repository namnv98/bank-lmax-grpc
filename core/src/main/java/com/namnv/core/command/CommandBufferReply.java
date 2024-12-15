package com.namnv.core.command;

import com.lmax.disruptor.EventHandler;

public interface CommandBufferReply
    extends EventHandler<CommandBufferEvent>, CommandBufferChannel {}
