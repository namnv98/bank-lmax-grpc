package com.namnv.core.command;

import com.namnv.core.BufferEventDispatcher;

public interface CommandBufferEventDispatcher
    extends CommandBufferChannel, BufferEventDispatcher<CommandBufferEvent> {}
