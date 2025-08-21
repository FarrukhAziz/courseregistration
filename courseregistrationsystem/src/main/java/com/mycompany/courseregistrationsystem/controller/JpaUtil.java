package com.mycompany.courseregistrationsystem.controller;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA utility with a rebuildable EntityManagerFactory.
 * - Default PU name: "crsPU" (match your persistence.xml)
 * - Reads standard Hibernate overrides from System properties by default.
 * - Tests can call JpaUtil.rebuild(overrides) to point JPA at Testcontainers.
 */
public final class JpaUtil {

  private static final String PU_NAME = "crsPU";
  private static EntityManagerFactory EMF;

  private JpaUtil() { }

  /** Get (or lazily create) the singleton EMF. */
  public static synchronized EntityManagerFactory emf() {
    if (EMF == null || !EMF.isOpen()) {
      EMF = Persistence.createEntityManagerFactory(PU_NAME, defaultProps());
    }
    return EMF;
  }

  /**
   * Rebuild the EMF with the given overrides (useful in integration tests).
   * Any previous EMF is closed first.
   *
   * Example overrides:
   *  - hibernate.connection.url
   *  - hibernate.connection.username
   *  - hibernate.connection.password
   *  - hibernate.connection.driver_class
   *  - hibernate.hbm2ddl.auto (e.g., create-drop)
   *  - hibernate.dialect
   */
  public static synchronized void rebuild(Map<String, String> overrides) {
    if (EMF != null && EMF.isOpen()) {
      EMF.close();
    }
    Map<String, String> props = defaultProps();
    if (overrides != null && !overrides.isEmpty()) {
      props.putAll(overrides);
    }
    EMF = Persistence.createEntityManagerFactory(PU_NAME, props);
  }

  /** Close the EMF if open (optional helper). */
  public static synchronized void close() {
    if (EMF != null && EMF.isOpen()) {
      EMF.close();
    }
  }

  /** Base properties, reading from System properties so CI/tests can override easily. */
  private static Map<String, String> defaultProps() {
    Map<String, String> p = new HashMap<String, String>();
    putIfPresent(p, "hibernate.connection.url", System.getProperty("hibernate.connection.url"));
    putIfPresent(p, "hibernate.connection.username", System.getProperty("hibernate.connection.username"));
    putIfPresent(p, "hibernate.connection.password", System.getProperty("hibernate.connection.password"));
    putIfPresent(p, "hibernate.connection.driver_class", System.getProperty("hibernate.connection.driver_class"));
    putIfPresent(p, "hibernate.hbm2ddl.auto", System.getProperty("hibernate.hbm2ddl.auto"));
    putIfPresent(p, "hibernate.dialect", System.getProperty("hibernate.dialect"));
    putIfPresent(p, "hibernate.show_sql", System.getProperty("hibernate.show_sql"));
    putIfPresent(p, "hibernate.format_sql", System.getProperty("hibernate.format_sql"));
    return p;
  }

  private static void putIfPresent(Map<String, String> map, String key, String value) {
    if (value != null && value.trim().length() > 0) {
      map.put(key, value);
    }
    // else keep value from persistence.xml/defaults
  }
}
