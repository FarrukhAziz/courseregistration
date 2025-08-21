package com.mycompany.courseregistrationsystem.repository;

import com.mycompany.courseregistrationsystem.controller.JpaUtil;
import com.mycompany.courseregistrationsystem.model.Course;
import com.mycompany.courseregistrationsystem.model.Student;
import org.junit.*;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class StudentRepositoryIT {

  @SuppressWarnings("resource")
@ClassRule
  public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
      .withDatabaseName("coursereg_test")
      .withUsername("testuser")
      .withPassword("testpass");

  private static EntityManagerFactory emf;
  private StudentRepository students;
  private CourseRepository courses;

  @BeforeClass
  public static void initEmf() {
    Map<String, String> overrides = new HashMap<String, String>();
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
    students = new StudentRepository();
    courses = new CourseRepository();
    clearAll();
  }

  @After
  public void tearDown() {
    clearAll();
  }

  private void clearAll() {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    tx.begin();
    em.createQuery("delete from Student").executeUpdate();
    em.createQuery("delete from Course").executeUpdate();
    tx.commit();
    em.close();
  }

  @Test
  public void save_newStudent_persistsCorrectly() {
    Student s = newStudent("1234567", "Alice Smith", "alice@example.com");
    s = students.save(s);
    assertNotNull(s.getId());
    Optional<Student> found = students.findById(s.getId());
    assertTrue(found.isPresent());
    assertEquals("Alice Smith", found.get().getFullName());
  }

  @Test
  public void save_multipleStudents_and_findAllWithCourses_returnsCorrectly() {
    Course c1 = courses.save(newCourse("CS101", "Intro CS", 6, 30));
    Course c2 = courses.save(newCourse("MATH101", "Math 1", 6, 50));

    Student s1 = newStudent("1234568", "Bob Brown", "bob@example.com");
    s1.getCourses().add(c1);
    students.save(s1);

    Student s2 = newStudent("1234569", "Charlie Davis", "charlie@example.com");
    s2.getCourses().add(c2);
    students.save(s2);

    List<Student> all = students.findAllWithCourses();
    assertEquals(2, all.size());
    assertEquals(1, all.get(0).getCourses().size());
    assertEquals(1, all.get(1).getCourses().size());
  }

  @Test
  public void findByMatricola_whenFound_returnsStudent() {
    Student s = newStudent("1234570", "David Evans", "david@example.com");
    students.save(s);
    Optional<Student> found = students.findByMatricola("1234570");
    assertTrue(found.isPresent());
    assertEquals("David Evans", found.get().getFullName());
  }

  @Test
  public void findByMatricola_whenNotFound_returnsEmpty() {
    Optional<Student> found = students.findByMatricola("9999999");
    assertFalse(found.isPresent());
  }

  @Test
  public void updateStudent_existing_changesFieldsAndCourses() {
    Course c1 = courses.save(newCourse("PHYS101", "Physics 1", 6, 40));
    Course c2 = courses.save(newCourse("CHEM101", "Chemistry 1", 6, 40));

    Student s = newStudent("1234571", "Eve Foster", "eve@example.com");
    s.getCourses().add(c1);
    s = students.save(s);

    s = students.updateStudent(s.getId(), "1234571", "Eve F.", "eve2@example.com", c2.getId());

    Optional<Student> found = students.findByIdWithCourses(s.getId());
    assertTrue(found.isPresent());
    assertEquals("Eve F.", found.get().getFullName());
    assertEquals("eve2@example.com", found.get().getEmail());
    assertEquals(1, found.get().getCourses().size());
    assertEquals("CHEM101", found.get().getCourses().iterator().next().getCode());
  }

  @Test(expected = IllegalArgumentException.class)
  public void updateStudent_nonExisting_throwsException() {
    students.updateStudent(999L, "1234999", "Nobody", "none@example.com", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void updateStudent_duplicateMatricola_throwsException() {
    students.save(newStudent("1234572", "Frank Green", "frank@example.com"));
    Student s2 = students.save(newStudent("1234573", "Grace Hill", "grace@example.com"));
    students.updateStudent(s2.getId(), "1234572", "Grace Updated", "grace2@example.com", null);
  }

  @Test
  public void deleteById_existing_removesStudent() {
    Student s = students.save(newStudent("1234574", "Henry Ives", "henry@example.com"));
    assertEquals(1, students.findAll().size());
    students.deleteById(s.getId());
    assertTrue(students.findAll().isEmpty());
  }

  @Test
  public void deleteById_nonExisting_noError() {
    students.deleteById(999999L);
    assertTrue(students.findAll().isEmpty());
  }

  @Test
  public void save_duplicateMatricola_violatesUniqueConstraint() {
    String dup = "1234575";
    students.save(newStudent(dup, "Ivy Jack", "ivy@example.com"));
    try {
      students.save(newStudent(dup, "Ivy Duplicate", "ivy2@example.com"));
      fail("Expected PersistenceException");
    } catch (PersistenceException e) {
      // OK
    }
  }

  @Test
  public void save_managedStudent_mergesCorrectly() {
    Student s = students.save(newStudent("1234576", "John King", "john@example.com"));
    s.setFullName("John K.");
    Student merged = students.save(s);
    Optional<Student> found = students.findById(merged.getId());
    assertTrue(found.isPresent());
    assertEquals("John K.", found.get().getFullName());
  }

  private Student newStudent(String matricola, String fullName, String email) {
    Student s = new Student();
    s.setMatricola(matricola);
    s.setFullName(fullName);
    s.setEmail(email);
    return s;
  }

  private Course newCourse(String code, String title, int cfu, int maxSeats) {
    Course c = new Course();
    c.setCode(code);
    c.setTitle(title);
    c.setCfu(cfu);
    c.setMaxSeats(maxSeats);
    return c;
  }
}
