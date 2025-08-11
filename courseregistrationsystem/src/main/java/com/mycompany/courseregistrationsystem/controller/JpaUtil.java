package com.mycompany.courseregistrationsystem.controller;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public final class JpaUtil {
  private static final EntityManagerFactory EMF =
      Persistence.createEntityManagerFactory("crsPU");
  private JpaUtil() {}
  public static EntityManagerFactory emf() { return EMF; }
}
