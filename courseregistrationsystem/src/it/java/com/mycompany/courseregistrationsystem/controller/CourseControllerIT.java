package com.mycompany.courseregistrationsystem.controller;

import com.mycompany.courseregistrationsystem.model.Course;
import com.mycompany.courseregistrationsystem.repository.CourseRepository;
import org.junit.*;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class CourseControllerIT {

  @SuppressWarnings("resource")
@ClassRule
  public static PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:15")
          .withDatabaseName("course_testdb")
          .withUsername("user")
          .withPassword("pass");

  private static EntityManagerFactory emf;

  private CourseController courseController;
  private CourseRepository courseRepository;

  @BeforeClass
  public static void startContainer() {
    Map<String,String> props = new HashMap<>();
    props.put("hibernate.connection.url", POSTGRES.getJdbcUrl());
    props.put("hibernate.connection.username", POSTGRES.getUsername());
    props.put("hibernate.connection.password", POSTGRES.getPassword());
    props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
    props.put("hibernate.hbm2ddl.auto", "create-drop");
    props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL95Dialect");
    JpaUtil.rebuild(props);
    emf = JpaUtil.emf();
    assertNotNull(emf);
  }

  @AfterClass
  public static void stopContainer() {
    if (emf != null) emf.close();
  }

  @Before
  public void setUp() {
    courseRepository = new CourseRepository();
    courseController = new CourseController();
    clearDb();
  }

  @After
  public void tearDown() {
    clearDb();
  }

  private void clearDb() {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();
      em.createNativeQuery("DELETE FROM enrollments").executeUpdate();
      em.createQuery("DELETE FROM Course").executeUpdate();
      em.createQuery("DELETE FROM Student").executeUpdate();
      tx.commit();
    } finally {
      if (tx.isActive()) tx.rollback();
      em.close();
    }
  }

  @Test
  public void testAddCourse() {
    Course c = courseController.add("CS101", "Intro to CS", 6, 30);
    assertNotNull(c.getId());
    Optional<Course> found = courseRepository.findById(c.getId());
    assertTrue(found.isPresent());
    assertEquals("CS101", found.get().getCode());
    assertEquals("Intro to CS", found.get().getTitle());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddCourse_duplicateCode_throws() {
    courseController.add("CS101", "Course 1", 6, 30);
    courseController.add("CS101", "Course 2", 6, 30);
  }

  @Test
  public void testLoadAllCourses() {
    courseController.add("CS101", "Intro to CS", 6, 30);
    courseController.add("MATH101", "Math 1", 6, 50);
    List<Course> courses = courseController.loadAll();
    assertEquals(2, courses.size());
  }

  @Test
  public void testUpdateCourse() {
    Course c = courseController.add("CS101", "Intro to CS", 6, 30);
    Course updated = courseController.update(c.getId(), "CS101", "Intro CS Updated", 7, 35);
    assertEquals("Intro CS Updated", updated.getTitle());
    assertEquals(7, updated.getCfu());
    assertEquals(35, updated.getMaxSeats());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateCourse_nonExisting_throws() {
    courseController.update(999L, "CS999", "Nonexistent", 6, 20);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateCourse_duplicateCode_throws() {
    courseController.add("CS101", "Intro to CS", 6, 30);
    Course c2 = courseController.add("MATH101", "Math 1", 6, 50);
    courseController.update(c2.getId(), "CS101", "Math 1 Updated", 6, 50);
  }

  @Test
  public void testDeleteCourse() {
    Course c = courseController.add("CS101", "Intro to CS", 6, 30);
    assertEquals(1, courseController.loadAll().size());
    courseController.delete(c.getId());
    assertTrue(courseController.loadAll().isEmpty());
  }

  @Test
  public void testEnrolledCount_emptyCourse_returnsZero() {
    Course c = courseController.add("CS101", "Intro to CS", 6, 30);
    int count = courseController.enrolledCount(c.getId());
    assertEquals(0, count);
  }

  @Test
  public void testEnrolledCount_nullId_returnsZero() {
    assertEquals(0, courseController.enrolledCount(null));
  }
}
