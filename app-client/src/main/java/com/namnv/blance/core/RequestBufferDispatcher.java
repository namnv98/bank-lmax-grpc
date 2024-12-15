package com.namnv.blance.core;

import com.namnv.blance.rest.BaseResponse;
import java.util.concurrent.CompletableFuture;

public interface RequestBufferDispatcher<T> extends RequestBufferChannel {
  CompletableFuture<BaseResponse> dispatch(T request);
}
