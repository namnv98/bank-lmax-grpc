package com.namnv.repo.offset;

public interface SnapshotRepository {
  Long getLastOffset();

  void persistLastOffset(long offset);
}
