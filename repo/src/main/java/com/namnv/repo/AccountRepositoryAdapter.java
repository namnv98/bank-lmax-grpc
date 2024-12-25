package com.namnv.repo;

import com.namnv.EntityManagerContextHolder;
import com.namnv.core.Balance;
import com.namnv.entities.SnapshotEntity;
import com.namnv.entities.SnapshotType;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {

  private final AccountRepositoryJpa accountRepositoryJpa;
  private final SnapshotRepositoryJpa snapshotRepositoryJpa;

  @Override
  public Stream<Balance> balances() {
    return accountRepositoryJpa.findAll().stream()
        .map(
            entity -> {
              Balance balance = new Balance();
              balance.setId(entity.getId());
              balance.setAmount(entity.getAmount());
              balance.setPrecision(entity.getPrecision());
              balance.setActive(entity.isActive());
              return balance;
            });
  }

  @Override
  public Long lastedId() {
    return snapshotRepositoryJpa
        .findById(SnapshotType.LAST_BALANCE_ID.getType())
        .map(SnapshotEntity::getValue)
        .map(Long::parseLong)
        .orElse(0L);
  }

  @Override
  public void persistBalances(List<Balance> balances) {
    if (balances == null || balances.isEmpty()) {
      return;
    }
    var entityManager = EntityManagerContextHolder.CONTEXT.get();
    entityManager
        .createNativeQuery(
            """
                CREATE TEMPORARY TABLE temp_balances(
                id          BIGINT PRIMARY KEY,
                amount      BIGINT  NOT NULL DEFAULT 0,
                precision   INT     NOT NULL DEFAULT 2,
                active      BOOLEAN NOT NULL DEFAULT FALSE
                )
                """)
        .executeUpdate();

    var values =
        balances.stream()
            .map(
                balance ->
                    String.format(
                        "(%s,%s,%s,%s)",
                        balance.getId(),
                        balance.getAmount(),
                        balance.getPrecision(),
                        balance.isActive()))
            .collect(Collectors.joining(","));
    var insertTempBalance = String.format("INSERT INTO temp_balances VALUES %s;", values);
    entityManager.createNativeQuery(insertTempBalance).executeUpdate();

    entityManager
        .createNativeQuery(
            """
                  INSERT INTO balances (id, amount, precision, active)
                  SELECT id, amount, precision, active FROM temp_balances
                  ON CONFLICT (id)
                  DO UPDATE SET
                      amount = EXCLUDED.amount,
                      precision = EXCLUDED.precision,
                      active = EXCLUDED.active;
                """)
        .executeUpdate();
    entityManager.createNativeQuery("DROP TABLE IF EXISTS temp_balances CASCADE;").executeUpdate();
  }

  @Override
  public void persistLastId(Long id) {
    var entityManager = EntityManagerContextHolder.CONTEXT.get();
    entityManager
        .createQuery("UPDATE SnapshotEntity s SET s.value = :value WHERE s.id = :id")
        .setParameter("value", id.toString())
        .setParameter("id", SnapshotType.LAST_BALANCE_ID.getType())
        .executeUpdate();
  }
}
