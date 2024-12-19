package com.namnv;

import io.grpc.*;

public class ConcurrentStreamInterceptor implements ServerInterceptor {
  private static int activeStreams = 0; // Biến để theo dõi số lượng stream đồng thời

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
    ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

    // Tăng số lượng stream đang hoạt động
    activeStreams++;
    System.out.println("Active Streams: " + activeStreams);

    // Khi stream kết thúc, giảm số lượng stream đang hoạt động
    ServerCall.Listener<ReqT> listener = next.startCall(call, headers);
    return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
      @Override
      public void onComplete() {
        super.onComplete();
        activeStreams--;
        System.out.println("Active Streams: " + activeStreams);
      }
    };
  }
}
