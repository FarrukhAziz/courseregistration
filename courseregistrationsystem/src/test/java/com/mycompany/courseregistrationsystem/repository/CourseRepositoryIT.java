
package com.mycompany.courseregistrationsystem.repository;

import com.mycompany.courseregistrationsystem.controller.JpaUtil;
import com.mycompany.courseregistrationsystem.model.Course;
import org.junit.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class CourseRepositoryIT {

    private static EntityManagerFactory emf;

    private CourseRepository repo;

    @BeforeClass
    public static void initEmf() {

        emf = JpaUtil.emf();
        assertNotNull("EntityManagerFactory should be initialized", emf);

        EntityManager em = emf.createEntityManager();
        try {
            Object url = em.getEntityManagerFactory().getProperties().get("hibernate.connection.url");
            System.out.println(">>> TEST JDBC URL: " + url);
        } finally {
            em.close();
        }
    }

    @AfterClass
    public static void closeEmf() {
        if (emf != null) emf.close();
    }

    @Before
    public void setUp() {
        repo = new CourseRepository();
    }

    // --------------------- tests ---------------------

    @Test
    public void findAll_whenEmpty_returnsEmptyList() {
        List<Course> all = repo.findAll();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    public void save_and_findAll_whenNotEmpty_returnsSavedCoursesInOrder() {
        Course c1 = newCourse("CS" + System.nanoTime(), "Intro to CS", 6, 30);
        Course c2 = newCourse("SE" + System.nanoTime(), "Software Eng", 9, 50);

        c1 = repo.save(c1);
        c2 = repo.save(c2);

        List<Course> all = repo.findAll();
        assertEquals(2, all.size());
        assertEquals(c1.getId(), all.get(0).getId());
        assertEquals(c2.getId(), all.get(1).getId());
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

        repo.deleteById(999_999L);
        assertTrue(repo.findAll().isEmpty());
    }

    @Test(expected = PersistenceException.class)
    public void save_duplicateCode_violatesUniqueConstraint() {
        String code = "UNIQ" + System.nanoTime();
        repo.save(newCourse(code, "Course A", 6, 30));

        repo.save(newCourse(code, "Course B", 9, 40));
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
