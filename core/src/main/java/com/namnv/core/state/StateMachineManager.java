package com.namnv.core.state;

public interface StateMachineManager {

  /** Load state machine from snapshot and replay command logs. */
  default void loadingStateMachine() {
    reloadSnapshot();
    replayCommandLogs();
  }

  StateMachineStatus getStatus();

  void reloadSnapshot();

  void replayCommandLogs();

  void takeSnapshot();

  void active();
}
