package com.mycompany.courseregistrationsystem.repository;

import com.mycompany.courseregistrationsystem.controller.JpaUtil;
import com.mycompany.courseregistrationsystem.model.Course;
import org.junit.*;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class CourseRepositoryIT {

  @SuppressWarnings("resource")
@ClassRule
  public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
      .withDatabaseName("coursereg_test")
      .withUsername("testuser")
      .withPassword("testpass");

  private static EntityManagerFactory emf;
  private CourseRepository repo;

  @BeforeClass
  public static void initEmf() {
    Map<String, String> overrides = new HashMap<>();
    overrides.put("hibernate.connection.url", postgres.getJdbcUrl());
    overrides.put("hibernate.connection.username", postgres.getUsername());
    overrides.put("hibernate.connection.password", postgres.getPassword());
    overrides.put("hibernate.connection.driver_class", "org.postgresql.Driver");
    overrides.put("hibernate.hbm2ddl.auto", "create-drop");
    overrides.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL95Dialect");
    JpaUtil.rebuild(overrides);
    emf = JpaUtil.emf();
    assertNotNull(emf);
  }

  @AfterClass
  public static void closeEmf() {
    if (emf != null) emf.close();
  }

  @Before
  public void setUp() {
    // Clean DB before each test to avoid cross-test interference
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    tx.begin();
    em.createQuery("delete from Course").executeUpdate();
    tx.commit();
    em.close();

    repo = new CourseRepository();
  }

  @Test
  public void findAll_whenEmpty_returnsEmptyList() {
    List<Course> all = repo.findAll();
    assertNotNull(all);
    assertTrue(all.isEmpty());
  }

  @Test
  public void save_and_findAll_whenNotEmpty_returnsSavedCourses() {
    final Course saved1 = repo.save(newCourse("CS" + System.nanoTime(), "Intro to CS", 6, 30));
    final Course saved2 = repo.save(newCourse("SE" + System.nanoTime(), "Software Eng", 9, 50));

    List<Course> all = repo.findAll();
    assertTrue(all.stream().anyMatch(c -> c.getCode().equals(saved1.getCode())));
    assertTrue(all.stream().anyMatch(c -> c.getCode().equals(saved2.getCode())));
  }

  @Test
  public void findByCode_whenFound_returnsCourse() {
    String code = "ENG" + System.nanoTime();
    Course c = repo.save(newCourse(code, "English 1", 6, 40));

    Optional<Course> found = repo.findByCode(code);
    assertTrue(found.isPresent());
    assertEquals(c.getId(), found.get().getId());
  }

  @Test
  public void findByCode_whenNotFound_returnsEmpty() {
    Optional<Course> found = repo.findByCode("NOPE" + System.nanoTime());
    assertFalse(found.isPresent());
  }

  @Test
  public void update_existingCourse_changesPersistedValues() {
    Course c = repo.save(newCourse("DB" + System.nanoTime(), "Databases", 6, 60));
    c.setTitle("Databases (Updated)");
    c.setCfu(8);
    c.setMaxSeats(45);
    Course updated = repo.save(c);

    Optional<Course> reloaded = repo.findByCode(updated.getCode());
    assertTrue(reloaded.isPresent());
    assertEquals("Databases (Updated)", reloaded.get().getTitle());
    assertEquals(8, reloaded.get().getCfu());
    assertEquals(45, reloaded.get().getMaxSeats());
  }

  @Test
  public void deleteById_existing_removesCourse() {
    Course c = repo.save(newCourse("AI" + System.nanoTime(), "AI", 6, 25));
    assertEquals(1, repo.findAll().size());
    repo.deleteById(c.getId());
    assertTrue(repo.findAll().isEmpty());
    assertFalse(repo.findByCode(c.getCode()).isPresent());
  }

  @Test
  public void deleteById_nonExisting_noErrorAndNoChange() {
    int before = repo.findAll().size();
    try {
      repo.deleteById(999_999L);
    } catch (Exception e) {
      fail("Should not throw when deleting non-existing ID, but got: " + e);
    }
    int after = repo.findAll().size();
    assertEquals(before, after);
  }

  @Test
  public void save_duplicateCode_violatesUniqueConstraint() {
    String code = "UNIQ" + System.nanoTime();
    repo.save(newCourse(code, "Course A", 6, 30));
    try {
      repo.save(newCourse(code, "Course B", 9, 40));
      fail("Expected unique constraint violation");
    } catch (Exception e) {
      assertNotNull(e);
    }
  }

  private static Course newCourse(String code, String title, int cfu, int maxSeats) {
    Course c = new Course();
    c.setCode(code);
    c.setTitle(title);
    c.setCfu(cfu);
    c.setMaxSeats(maxSeats);
    return c;
  }
}
