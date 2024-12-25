package com.namnv.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "balances")
public class BalanceEntity {

  @Id private Long id;

  private long amount;

  @Column(name = "precision")
  private int precision;

  private boolean active;
}
