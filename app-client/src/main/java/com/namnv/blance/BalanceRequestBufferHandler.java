package com.namnv.blance;

import com.namnv.blance.command.BalanceCommand;
import com.namnv.blance.core.RequestBufferEvent;
import com.namnv.blance.query.BalanceQuery;
import com.namnv.blance.rest.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class BalanceRequestBufferHandler implements RequestBufferHandler {

  private final BalanceCommandStub balanceCommandStub;
  private final BalanceQueryStub balanceQueryStub;

  @Override
  public void onEvent(RequestBufferEvent event, long sequence, boolean endOfBatch)
      throws Exception {
    try {
      if (event.getRequest() instanceof BalanceCommand) {
        balanceCommandStub.sendGrpcMessage(event);
      } else if (event.getRequest() instanceof BalanceQuery) {
        balanceQueryStub.sendGrpcMessage(event);
      } else {
        log.error("Unknown event type: {}", event.getClass().getName());
        event.getResponseFuture().complete(new BaseResponse(400, "Unknown event type"));
      }
    } catch (Exception e) {
      log.error("Error while sending message", e);
      event.getResponseFuture().complete(new BaseResponse(500, "Internal Server Error"));
    }
  }
}
