package com.namnv;

public interface TransactionManager {
  void doInNewTransaction(Runnable runnable);
}
