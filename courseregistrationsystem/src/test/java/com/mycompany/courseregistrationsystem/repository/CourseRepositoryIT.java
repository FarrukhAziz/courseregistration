package com.mycompany.courseregistrationsystem.repository;

import com.mycompany.courseregistrationsystem.controller.JpaUtil;
import com.mycompany.courseregistrationsystem.model.Course;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;

import static org.junit.Assert.*;

public class CourseRepositoryIT {

    private static EntityManagerFactory emf;
    private final CourseRepository courses = new CourseRepository();

    @BeforeClass
    public static void init() { emf = JpaUtil.emf(); assertNotNull(emf); }

    @AfterClass
    public static void close() { if (emf != null) emf.close(); }

    @Test
    public void save_find_update_delete_course() {
        Course c = new Course();
        c.setCode("CS101");
        c.setTitle("Intro to CS");
        c.setCfu(6);
        c.setMaxSeats(30);

        c = courses.save(c);
        assertNotNull(c.getId());

        assertTrue(courses.findByCode("CS101").isPresent());

        c.setTitle("Intro to Computer Science");
        c = courses.save(c);
        assertEquals("Intro to Computer Science",
                courses.findByCode("CS101").get().getTitle());

        courses.deleteById(c.getId());
        assertFalse(courses.findByCode("CS101").isPresent());
    }
}
