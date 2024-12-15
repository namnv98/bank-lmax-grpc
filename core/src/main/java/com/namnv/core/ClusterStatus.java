package com.namnv.core;

import java.util.concurrent.atomic.AtomicReference;

public enum ClusterStatus {
  NOT_AVAILABLE,
  ACTIVE;

  public static final AtomicReference<ClusterStatus> STATE = new AtomicReference<>(ACTIVE);
}
