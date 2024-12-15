package com.namnv.grpc;

import com.namnv.core.Balances;
import com.namnv.core.ClusterStatus;
import com.namnv.proto.BalanceProto;
import com.namnv.proto.BalanceQueryServiceGrpc;
import com.namnv.core.Balance;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class BalanceGrpcQuery extends BalanceQueryServiceGrpc.BalanceQueryServiceImplBase {

  private final Balances balances;

  @Override
  public StreamObserver<BalanceProto.BalanceQuery> sendQuery(
      StreamObserver<BalanceProto.BalanceQueryResult> responseObserver) {
    return new StreamObserver<>() {
      @Override
      public void onNext(BalanceProto.BalanceQuery balanceQuery) {
        if (ClusterStatus.STATE.get().equals(ClusterStatus.NOT_AVAILABLE)) {
          responseObserver.onNext(
              BalanceProto.BalanceQueryResult.newBuilder()
                  .setBaseResult(BalanceProto.BaseResult.newBuilder().setCode(503).build())
                  .build());
          return;
        }
        switch (balanceQuery.getTypeCase()) {
          case SINGLEBALANCEQUERY -> {
            Balance balance = balances.getBalance(balanceQuery.getSingleBalanceQuery().getId());
            if (balance != null) {
              responseObserver.onNext(
                  BalanceProto.BalanceQueryResult.newBuilder()
                      .setSingleBalanceResult(
                          BalanceProto.SingleBalanceResult.newBuilder()
                              .setId(balance.getId())
                              .setAmount(balance.getAmount())
                              .setPrecision(balance.getPrecision())
                              .setActive(balance.isActive())
                              .setCorrelationId(
                                  balanceQuery.getSingleBalanceQuery().getCorrelationId())
                              .build())
                      .build());
            } else {
              responseObserver.onNext(
                  BalanceProto.BalanceQueryResult.newBuilder()
                      .setBaseResult(
                          BalanceProto.BaseResult.newBuilder()
                              .setCode(404)
                              .setCorrelationId(
                                  balanceQuery.getSingleBalanceQuery().getCorrelationId())
                              .build())
                      .build());
            }
          }
          case TYPE_NOT_SET -> {}
        }
      }

      @Override
      public void onError(Throwable throwable) {
        log.error("Error in BalanceQueryGrpc: {}", throwable.getMessage());
        responseObserver.onError(throwable);
      }

      @Override
      public void onCompleted() {
        log.info("on completed in BalanceQueryGrpc");
        responseObserver.onCompleted();
      }
    };
  }
}
