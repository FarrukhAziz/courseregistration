package com.mycompany.courseregistrationsystem.controller;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA utility with a rebuildable EntityManagerFactory.
 *
 * - PU name is configurable via system property "persistence.unit"
 *   (defaults to "crsPU"). You can also set env var PERSISTENCE_UNIT.
 * - Reads standard Hibernate/JPA overrides from System properties.
 * - Tests can call JpaUtil.rebuild(overrides) to point JPA at Testcontainers/H2.
 */
public final class JpaUtil {

  private static final String DEFAULT_PU = "crsPU";
  private static EntityManagerFactory EMF;

  private JpaUtil() { }


  private static String puName() {
    String sys = System.getProperty("persistence.unit");
    if (sys != null && !sys.trim().isEmpty()) return sys.trim();
    String env = System.getenv("PERSISTENCE_UNIT");
    if (env != null && !env.trim().isEmpty()) return env.trim();
    return DEFAULT_PU;
  }


  public static synchronized EntityManagerFactory emf() {
    if (EMF == null || !EMF.isOpen()) {
      EMF = Persistence.createEntityManagerFactory(puName(), defaultProps());
    }
    return EMF;
  }

  /**
   * 
   * Any previous EMF is closed first.
   */
  public static synchronized void rebuild(Map<String, String> overrides) {
    if (EMF != null && EMF.isOpen()) {
      EMF.close();
    }
    Map<String, String> props = defaultProps();
    if (overrides != null && !overrides.isEmpty()) {
      props.putAll(overrides);
    }
    EMF = Persistence.createEntityManagerFactory(puName(), props);
  }

  /** optional helper */
  public static synchronized void close() {
    if (EMF != null && EMF.isOpen()) {
      EMF.close();
    }
  }


  private static Map<String, String> defaultProps() {
    Map<String, String> p = new HashMap<>();

    // Hibernate-style overrides
    putIfPresent(p, "hibernate.connection.url",         System.getProperty("hibernate.connection.url"));
    putIfPresent(p, "hibernate.connection.username",    System.getProperty("hibernate.connection.username"));
    putIfPresent(p, "hibernate.connection.password",    System.getProperty("hibernate.connection.password"));
    putIfPresent(p, "hibernate.connection.driver_class",System.getProperty("hibernate.connection.driver_class"));
    putIfPresent(p, "hibernate.hbm2ddl.auto",           System.getProperty("hibernate.hbm2ddl.auto"));
    putIfPresent(p, "hibernate.dialect",                System.getProperty("hibernate.dialect"));
    putIfPresent(p, "hibernate.show_sql",               System.getProperty("hibernate.show_sql"));
    putIfPresent(p, "hibernate.format_sql",             System.getProperty("hibernate.format_sql"));

    // JPA-style overrides - optional
    putIfPresent(p, "javax.persistence.jdbc.driver",    System.getProperty("javax.persistence.jdbc.driver"));
    putIfPresent(p, "javax.persistence.jdbc.url",       System.getProperty("javax.persistence.jdbc.url"));
    putIfPresent(p, "javax.persistence.jdbc.user",      System.getProperty("javax.persistence.jdbc.user"));
    putIfPresent(p, "javax.persistence.jdbc.password",  System.getProperty("javax.persistence.jdbc.password"));

    return p;
  }

  private static void putIfPresent(Map<String, String> map, String key, String value) {
    if (value != null && value.trim().length() > 0) {
      map.put(key, value.trim());
    }
  }
}
