package com.mycompany.courseregistrationsystem.controller;

import static org.junit.Assert.*;

import com.mycompany.courseregistrationsystem.model.Course;
import com.mycompany.courseregistrationsystem.repository.CourseRepository;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Optional;

public class CourseControllerIT {

    @SuppressWarnings("resource")
	private static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("course_testdb")
                    .withUsername("user")
                    .withPassword("pass");

    private CourseController courseController;
    private CourseRepository courseRepository;

    @BeforeClass
    public static void startContainer() {
        POSTGRES_CONTAINER.start();
        System.setProperty("DB_URL", POSTGRES_CONTAINER.getJdbcUrl());
        System.setProperty("DB_USER", POSTGRES_CONTAINER.getUsername());
        System.setProperty("DB_PASS", POSTGRES_CONTAINER.getPassword());
    }

    @AfterClass
    public static void stopContainer() {
        POSTGRES_CONTAINER.stop();
    }

    @Before
    public void setUp() {
        courseRepository = new CourseRepository();
        courseController = new CourseController();

        // Clear DB before each test
        courseRepository.findAll().forEach(c -> courseRepository.deleteById(c.getId()));
    }

    @After
    public void tearDown() {
        // Clear DB after each test
        courseRepository.findAll().forEach(c -> courseRepository.deleteById(c.getId()));
    }

    // ---------------- TESTS ----------------

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
