package com.namnv.blance;

import com.namnv.blance.command.CreateBalanceCommand;
import com.namnv.blance.command.DepositCommand;
import com.namnv.blance.command.TransferCommand;
import com.namnv.blance.command.WithdrawCommand;
import com.namnv.blance.core.RequestBufferEvent;
import com.namnv.blance.rest.BaseResponse;
import com.namnv.proto.BalanceCommandServiceGrpc;
import com.namnv.proto.BalanceProto;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BalanceCommandStub
    extends BaseAsyncStub<BalanceProto.BalanceCommand, BalanceProto.BaseResult> {

  private final BalanceCommandServiceGrpc.BalanceCommandServiceStub balanceCommandServiceStub;

  public BalanceCommandStub(
      BalanceCommandServiceGrpc.BalanceCommandServiceStub balanceCommandServiceStub) {
    this.balanceCommandServiceStub = balanceCommandServiceStub;
    this.initRequestStreamObserver();
  }

  @Override
  protected StreamObserver<BalanceProto.BalanceCommand> initRequestStreamObserver(
      StreamObserver<BalanceProto.BaseResult> responseObserver) {
    return balanceCommandServiceStub.sendCommand(responseObserver);
  }

  @Override
  protected void sendGrpcMessage(RequestBufferEvent request) {
    try {
      replyFutures.put(request.getRequest().getCorrelationId(), request.getResponseFuture());
      switch (request.getRequest()) {
        case CreateBalanceCommand createCommand -> create(createCommand);
        case DepositCommand depositCommand -> deposit(depositCommand);
        case WithdrawCommand withdrawCommand -> withdraw(withdrawCommand);
        case TransferCommand transferCommand -> transfer(transferCommand);
        default -> replyFutures.remove(request.getRequest().getCorrelationId());
      }
    } catch (Exception e) {
      log.error("Error while sending message", e);
      replyFutures.remove(request.getRequest().getCorrelationId());
      throw e;
    }
  }

  @Override
  protected String extractResultCorrelationId(BalanceProto.BaseResult result) {
    return result.getCorrelationId();
  }

  @Override
  protected BaseResponse extractResult(BalanceProto.BaseResult result) {
    return new BaseResponse(result.getCode(), result.getMessage());
  }

  private void create(CreateBalanceCommand createCommand) {
    var clusterCommand =
        BalanceProto.CreateBalanceCommand.newBuilder()
            .setCorrelationId(createCommand.getCorrelationId())
            .build();
    requestStreamObserver.onNext(
        BalanceProto.BalanceCommand.newBuilder().setCreateBalanceCommand(clusterCommand).build());
  }

  private void deposit(DepositCommand depositCommand) {
    var clusterCommand =
        BalanceProto.DepositCommand.newBuilder()
            .setCorrelationId(depositCommand.getCorrelationId())
            .setId(depositCommand.getId())
            .setAmount(depositCommand.getAmount())
            .build();
    requestStreamObserver.onNext(
        BalanceProto.BalanceCommand.newBuilder().setDepositCommand(clusterCommand).build());
  }

  private void withdraw(WithdrawCommand withdrawCommand) {
    var clusterCommand =
        BalanceProto.WithdrawCommand.newBuilder()
            .setCorrelationId(withdrawCommand.getCorrelationId())
            .setId(withdrawCommand.getId())
            .setAmount(withdrawCommand.getAmount())
            .build();
    requestStreamObserver.onNext(
        BalanceProto.BalanceCommand.newBuilder().setWithdrawCommand(clusterCommand).build());
  }

  private void transfer(TransferCommand transferCommand) {
    var clusterCommand =
        BalanceProto.TransferCommand.newBuilder()
            .setCorrelationId(transferCommand.getCorrelationId())
            .setFromId(transferCommand.getFromId())
            .setToId(transferCommand.getToId())
            .setAmount(transferCommand.getAmount())
            .build();
    requestStreamObserver.onNext(
        BalanceProto.BalanceCommand.newBuilder().setTransferCommand(clusterCommand).build());
  }
}
