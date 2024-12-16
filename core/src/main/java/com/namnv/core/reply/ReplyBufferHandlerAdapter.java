package com.namnv.core.reply;

import com.namnv.core.SimpleReplier;
import com.namnv.proto.BalanceProto;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReplyBufferHandlerAdapter implements ReplyBufferHandler {

  private final SimpleReplier simpleReplier;

  @Override
  public void onEvent(ReplyBufferEvent event, long sequence, boolean endOfBatch) throws Exception {
    Optional.ofNullable(simpleReplier.repliers.get(event.getReplyChannel()))
      .ifPresent(
        streamObserver ->
        {
          try {
            streamObserver.onNext(
              BalanceProto.BaseResult.newBuilder()
                .setCorrelationId(event.getCorrelationId())
                .setMessage(event.getResult().toString())
                .build());
          } catch (Exception e) {
            System.out.println("error");
          }
        });
  }
}
