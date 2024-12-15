package com.namnv.repo;

import com.namnv.entities.BalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepositoryJpa extends JpaRepository<BalanceEntity, Long> {}
