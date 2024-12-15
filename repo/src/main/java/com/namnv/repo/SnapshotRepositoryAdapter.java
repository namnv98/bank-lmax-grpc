package com.namnv.repo;

import com.namnv.EntityManagerContextHolder;
import com.namnv.entities.SnapshotEntity;
import com.namnv.entities.SnapshotType;
import com.namnv.repo.offset.SnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SnapshotRepositoryAdapter implements SnapshotRepository {

  private final SnapshotRepositoryJpa snapshotRepositoryJpa;

  @Override
  public Long getLastOffset() {
    return snapshotRepositoryJpa
        .findById(SnapshotType.LAST_KAFKA_OFFSET.getType())
        .map(SnapshotEntity::getValue)
        .map(Long::parseLong)
        .orElse(-1L);
  }

  @Override
  public void persistLastOffset(long offset) {
    var entityManager = EntityManagerContextHolder.CONTEXT.get();
    entityManager
        .createQuery("update SnapshotEntity s set s.value = :value where s.id = :id")
        .setParameter("value", String.valueOf(offset))
        .setParameter("id", SnapshotType.LAST_KAFKA_OFFSET.getType())
        .executeUpdate();
  }
}
