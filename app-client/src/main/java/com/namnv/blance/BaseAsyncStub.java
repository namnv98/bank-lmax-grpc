package com.namnv.blance;

import com.namnv.blance.core.RequestBufferEvent;
import com.namnv.blance.rest.BaseResponse;
import io.grpc.stub.StreamObserver;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.agrona.collections.Object2ObjectHashMap;

@Slf4j
public abstract class BaseAsyncStub<REQUEST, RESULT> {

  protected final Map<String, CompletableFuture<BaseResponse>> replyFutures =
      new Object2ObjectHashMap<>();
  protected StreamObserver<REQUEST> requestStreamObserver;

  protected void initRequestStreamObserver() {
    requestStreamObserver = initRequestStreamObserver(responseObserver());
  }

  protected abstract StreamObserver<REQUEST> initRequestStreamObserver(
      StreamObserver<RESULT> responseObserver);

  protected abstract void sendGrpcMessage(RequestBufferEvent request);

  protected abstract String extractResultCorrelationId(RESULT result);

  protected abstract BaseResponse extractResult(RESULT result);

  protected StreamObserver<RESULT> responseObserver() {
    return new StreamObserver<RESULT>() {
      @Override
      public void onNext(RESULT result) {
        Optional.ofNullable(replyFutures.remove(extractResultCorrelationId(result)))
            .ifPresent(
                future -> {
                  future.complete(extractResult(result));
                });
      }

      @Override
      public void onError(Throwable throwable) {
        replyFutures.clear();
        silentSleep(5_000);
        initRequestStreamObserver();
      }

      @Override
      public void onCompleted() {
        replyFutures.clear();
      }
    };
  }

  private void silentSleep(long time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
    }
  }
}
