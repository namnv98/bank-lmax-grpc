package com.namnv;

import com.lmax.disruptor.dsl.Disruptor;
import com.namnv.core.ClusterBootstrap;
import com.namnv.core.ClusterStatus;
import com.namnv.core.command.CommandBufferEvent;
import com.namnv.core.reply.ReplyBufferEvent;
import com.namnv.core.state.StateMachineManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LeaderBootstrap implements ClusterBootstrap {

  private final StateMachineManager stateMachineManager;
  private final Disruptor<CommandBufferEvent> commandBufferDisruptor;
  private final Disruptor<ReplyBufferEvent> replyBufferEventDisruptor;

  @Override
  public void onStart() {
    try {
      log.info("Leader start");
      loadingStateMachine();
      activeReplyChannel();
      activeCommandChannel();
      activeCLuster();
      log.info("Leader started");
    } catch (Exception e) {
      log.error("Leader start failed", e);
      System.exit(-9);
    }
  }

  @Override
  public void onStop() {
    commandBufferDisruptor.shutdown();
  }

  private void loadingStateMachine() {
    stateMachineManager.loadingStateMachine();
  }

  private void activeReplyChannel() {
    log.info("On starting reply-buffer channel");
    replyBufferEventDisruptor.start();
    log.info("On started reply-buffer channel");
  }

  private void activeCommandChannel() {
    log.info("On starting command-buffer channel");
    commandBufferDisruptor.start();
    log.info("On started command-buffer channel");
  }

  private void activeCLuster() {
    ClusterStatus.STATE.set(ClusterStatus.ACTIVE);
  }
}
