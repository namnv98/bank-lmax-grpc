package com.namnv;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionManagerAdapter implements TransactionManager {

  private final EntityManagerFactory entityManagerFactory;

  @Override
  public void doInNewTransaction(Runnable runnable) {
    var entityManager = entityManagerFactory.createEntityManager();
    EntityManagerContextHolder.CONTEXT.set(entityManager);
    var transaction = entityManager.getTransaction();
    try {
      transaction.begin();
      runnable.run();
      transaction.commit();
    } catch (Exception e) {
      if (transaction.isActive()) {
        transaction.rollback();
      }
      throw e;
    } finally {
      if (entityManager.isOpen()) {
        entityManager.close();
      }
      EntityManagerContextHolder.CONTEXT.remove();
    }
  }
}
