package com.namnv.blance;

import com.namnv.blance.core.RequestBufferEvent;
import com.namnv.blance.query.BalanceDetailQuery;
import com.namnv.blance.rest.BaseResponse;
import com.namnv.proto.BalanceProto;
import com.namnv.proto.BalanceQueryServiceGrpc;
import io.grpc.stub.StreamObserver;

public class BalanceQueryStub
    extends BaseAsyncStub<BalanceProto.BalanceQuery, BalanceProto.BalanceQueryResult> {

  private final BalanceQueryServiceGrpc.BalanceQueryServiceStub balanceQueryServiceStub;

  public BalanceQueryStub(BalanceQueryServiceGrpc.BalanceQueryServiceStub balanceQueryServiceStub) {
    this.balanceQueryServiceStub = balanceQueryServiceStub;
    this.initRequestStreamObserver();
  }

  @Override
  protected StreamObserver<BalanceProto.BalanceQuery> initRequestStreamObserver(
      StreamObserver<BalanceProto.BalanceQueryResult> responseObserver) {
    return balanceQueryServiceStub.sendQuery(responseObserver);
  }

  @Override
  protected void sendGrpcMessage(RequestBufferEvent request) {
    try {
      replyFutures.put(request.getRequest().getCorrelationId(), request.getResponseFuture());
      switch (request.getRequest()) {
        case BalanceDetailQuery balanceDetailQuery -> balanceDetail(balanceDetailQuery);
        default -> replyFutures.remove(request.getRequest().getCorrelationId());
      }
    } catch (Exception e) {
      replyFutures.remove(request.getRequest().getCorrelationId());
      throw e;
    }
  }

  @Override
  protected String extractResultCorrelationId(BalanceProto.BalanceQueryResult balanceQueryResult) {
    return switch (balanceQueryResult.getTypeCase()) {
      case SINGLEBALANCERESULT -> balanceQueryResult.getSingleBalanceResult().getCorrelationId();
      default -> balanceQueryResult.getBaseResult().getCorrelationId();
    };
  }

  @Override
  protected BaseResponse extractResult(BalanceProto.BalanceQueryResult balanceQueryResult) {
    return switch (balanceQueryResult.getTypeCase()) {
      case SINGLEBALANCERESULT ->
          new BalanceDetailResponse(
              balanceQueryResult.getSingleBalanceResult().getId(),
              balanceQueryResult.getSingleBalanceResult().getAmount());
      case BASERESULT ->
          new BaseResponse(
              balanceQueryResult.getBaseResult().getCode(),
              balanceQueryResult.getBaseResult().getMessage());
      default -> new BaseResponse(500, "Unknown response!!!");
    };
  }

  private void balanceDetail(BalanceDetailQuery balanceDetailQuery) {
    var balanceDetail =
        BalanceProto.SingleBalanceQuery.newBuilder()
            .setCorrelationId(balanceDetailQuery.getCorrelationId())
            .setId(balanceDetailQuery.getId())
            .build();
    requestStreamObserver.onNext(
        BalanceProto.BalanceQuery.newBuilder().setSingleBalanceQuery(balanceDetail).build());
  }
}
