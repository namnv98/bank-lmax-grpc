package com.namnv.core.reply;

import com.lmax.disruptor.EventHandler;

public interface ReplyBufferHandler extends EventHandler<ReplyBufferEvent>, ReplyBufferChanel {}
