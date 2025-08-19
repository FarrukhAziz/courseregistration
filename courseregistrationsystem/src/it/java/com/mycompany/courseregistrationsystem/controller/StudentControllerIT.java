package com.mycompany.courseregistrationsystem.controller;

import com.mycompany.courseregistrationsystem.model.Student;
import com.mycompany.courseregistrationsystem.repository.StudentRepository;
import org.junit.*;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class StudentControllerIT {

    @SuppressWarnings("resource")
	private static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("student_testdb")
                    .withUsername("testuser")
                    .withPassword("testpass");

    private StudentController controller;
    private StudentRepository repo;

    @BeforeClass
    public static void startContainer() {
        POSTGRES_CONTAINER.start();

        System.setProperty("db.url", POSTGRES_CONTAINER.getJdbcUrl());
        System.setProperty("db.user", POSTGRES_CONTAINER.getUsername());
        System.setProperty("db.password", POSTGRES_CONTAINER.getPassword());
    }

    @AfterClass
    public static void stopContainer() {
        POSTGRES_CONTAINER.stop();
    }

    @Before
    public void setUp() {
        repo = new StudentRepository();
        controller = new StudentController();

        // Clean up table before each test
        EntityManager em = JpaUtil.emf().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("DELETE FROM Student").executeUpdate();
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
            em.close();
        }
    }

    @Test
    public void testAddStudent() {
        Student s = controller.add("M12345", "John Doe", "john@example.com");
        assertNotNull(s.getId());
        assertEquals("M12345", s.getMatricola());
        assertEquals("John Doe", s.getFullName());
        assertEquals("john@example.com", s.getEmail());
    }

    @Test
    public void testLoadAllStudents() {
        controller.add("M123", "Alice", "alice@example.com");
        controller.add("M124", "Bob", "bob@example.com");

        List<Student> students = controller.loadAll();
        assertEquals(2, students.size());
    }

    @Test
    public void testUpdateStudent() {
        Student s = controller.add("M200", "Charlie", "charlie@example.com");

        Student updated = controller.update(s.getId(), "M200", "Charlie Brown", "charlie.b@example.com");
        assertEquals("Charlie Brown", updated.getFullName());
        assertEquals("charlie.b@example.com", updated.getEmail());

        Optional<Student> fetched = repo.findById(s.getId());
        assertTrue(fetched.isPresent());
        assertEquals("Charlie Brown", fetched.get().getFullName());
    }

    @Test
    public void testDeleteStudent() {
        Student s = controller.add("M300", "David", "david@example.com");
        Long id = s.getId();

        controller.delete(id);

        Optional<Student> fetched = repo.findById(id);
        assertFalse(fetched.isPresent());
    }
}
