package com.namnv.core;

import com.namnv.proto.BalanceProto;
import io.grpc.stub.StreamObserver;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class SimpleReplier {
  public final Map<String, StreamObserver<BalanceProto.BaseResult>> repliers =
      new ConcurrentHashMap<>();
}
