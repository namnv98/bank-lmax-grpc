package com.namnv;

import jakarta.persistence.EntityManager;

public class EntityManagerContextHolder {
  public static ThreadLocal<EntityManager> CONTEXT = new ThreadLocal<>();
}
