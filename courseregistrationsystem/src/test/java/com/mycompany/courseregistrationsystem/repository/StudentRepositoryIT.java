package com.mycompany.courseregistrationsystem.repository;

import com.mycompany.courseregistrationsystem.controller.JpaUtil;
import com.mycompany.courseregistrationsystem.model.Course;
import com.mycompany.courseregistrationsystem.model.Student;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;
import java.util.List;

import static org.junit.Assert.*;

public class StudentRepositoryIT {

    private static EntityManagerFactory emf;
    private final StudentRepository students = new StudentRepository();
    private final CourseRepository  courses  = new CourseRepository();

    @BeforeClass
    public static void initEmf() {

        emf = JpaUtil.emf();
        assertNotNull(emf);
    }

    @AfterClass
    public static void closeEmf() {
        if (emf != null) emf.close();
    }

    @Test
    public void save_find_update_delete_student() {

        Course c = new Course();
        c.setCode("ENG101");
        c.setTitle("English 1");
        c.setCfu(6);
        c.setMaxSeats(2);
        c = courses.save(c);
        assertNotNull(c.getId());

        Student s = new Student();
        s.setMatricola("S0001");
        s.setFullName("Alice Smith");
        s.setEmail("alice@example.com");
        s.getCourses().add(c);
        s = students.save(s);
        assertNotNull(s.getId());

        List<Student> all = students.findAllWithCourses();
        assertEquals(1, all.size());
        assertEquals("S0001", all.get(0).getMatricola());
        assertEquals(1, all.get(0).getCourses().size());

        s.setFullName("Alice S.");
        s = students.save(s);
        assertEquals("Alice S.", students.findById(s.getId()).get().getFullName());

        students.deleteById(s.getId());
        assertTrue(students.findAll().isEmpty());
    }
}
