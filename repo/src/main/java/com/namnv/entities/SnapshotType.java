package com.namnv.entities;

import lombok.Getter;

@Getter
public enum SnapshotType {
  LAST_KAFKA_OFFSET("LAST_KAFKA_OFFSET"),
  LAST_BALANCE_ID("LAST_BALANCE_ID");

  private final String type;

  SnapshotType(String type) {
    this.type = type;
  }
}
