package com.namnv.blance.core;

import com.lmax.disruptor.dsl.Disruptor;
import com.namnv.blance.rest.BaseRequest;
import com.namnv.blance.rest.BaseResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RequestBufferDispatcherImpl<T extends BaseRequest>
    implements RequestBufferDispatcher<T> {

  private final Disruptor<RequestBufferEvent> requestBufferEventDisruptor;
  private final long timeoutMilliseconds;

  @Override
  public CompletableFuture<BaseResponse> dispatch(T request) {
    CompletableFuture<BaseResponse> responseFuture = new CompletableFuture<>();
    responseFuture.completeOnTimeout(
        new BaseResponse(408, "Request timeout"), timeoutMilliseconds, TimeUnit.MILLISECONDS);
    requestBufferEventDisruptor.publishEvent(
        ((event, sequence) -> {
          event.setRequest(request);
          event.setResponseFuture(responseFuture);
        }));
    return responseFuture;
  }
}
