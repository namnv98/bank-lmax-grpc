package com.namnv.repo;

import com.namnv.entities.SnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SnapshotRepositoryJpa extends JpaRepository<SnapshotEntity, String> {}
