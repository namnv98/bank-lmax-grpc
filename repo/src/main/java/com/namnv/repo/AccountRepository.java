package com.namnv.repo;

import com.namnv.core.Balance;

import java.util.List;
import java.util.stream.Stream;

public interface AccountRepository {
  Stream<Balance> balances();

  Long lastedId();

  void persistBalances(List<Balance> balances);

  void persistLastId(Long id);
}
