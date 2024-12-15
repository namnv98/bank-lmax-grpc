package com.namnv.core.state;

public enum StateMachineStatus {
  INITIALIZING,
  LOADING_SNAPSHOT,
  LOADED_SNAPSHOT,
  REPLAYING_LOGS,
  REPLAYED_LOGS,
  ACTIVE
}
