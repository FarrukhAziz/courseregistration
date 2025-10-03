package com.mycompany.courseregistrationsystem.controller;

import com.mycompany.courseregistrationsystem.model.Course;
import com.mycompany.courseregistrationsystem.model.Student;
import com.mycompany.courseregistrationsystem.repository.CourseRepository;
import com.mycompany.courseregistrationsystem.repository.StudentRepository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class StudentController {

  private final StudentRepository studentRepo;
  private final CourseRepository  courseRepo;

  public StudentController() {
    this(new StudentRepository(), new CourseRepository());
  }

  public StudentController(StudentRepository studentRepo) {
    this(studentRepo, new CourseRepository());
  }

  public StudentController(StudentRepository studentRepo, CourseRepository courseRepo) {
    this.studentRepo = studentRepo;
    this.courseRepo  = courseRepo;
  }

  // ---------- Existing simple CRUD you wrote ----------
  public List<Student> loadAll() {
    return studentRepo.findAll();
  }

  public Student add(String matricola, String fullName, String email) {
    Student s = new Student();
    s.setMatricola(matricola);
    s.setFullName(fullName);
    s.setEmail(email);
    return studentRepo.save(s);
  }

  public Student update(Long id, String matricola, String fullName, String email) {
    Student s = studentRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Student not found"));
    s.setMatricola(matricola);
    s.setFullName(fullName);
    s.setEmail(email);
    return studentRepo.save(s);
  }

  public void delete(Long id) {
    studentRepo.deleteById(id);
  }

  // ---------- Minimal extras that StudentSwingView uses ----------
  public List<Student> findAllWithCourses() {
    return studentRepo.findAllWithCourses();
  }

  public List<Course> loadAllCourses() {
    return courseRepo.findAll();
  }

  public void addStudent(String matricola, String fullName, String email, Long courseId) {
    Student s = new Student();
    s.setMatricola(matricola);
    s.setFullName(fullName);
    s.setEmail(email);

    if (courseId != null) {
      Optional<Course> c = courseRepo.findById(courseId);
      c.ifPresent(course -> s.getCourses().add(course));
    }
    studentRepo.save(s);
  }

  public void updateStudent(Long id, String matricola, String fullName, String email, Long courseId) {
    // Use repository helper if you already have it; this matches your view’s call
    studentRepo.updateStudent(id, matricola, fullName, email, courseId);
  }

  public void deleteStudent(Long id) {
    studentRepo.deleteById(id);
  }

  public int enrolledCount(Long courseId) {
    if (courseId == null) return 0;
    EntityManager em = JpaUtil.emf().createEntityManager();
    try {
      Long cnt = em.createQuery(
          "select count(s) from Course c join c.students s where c.id = :cid", Long.class)
          .setParameter("cid", courseId)
          .getSingleResult();
      return cnt == null ? 0 : cnt.intValue();
    } finally {
      em.close();
    }
  }

  public int findMaxSeats(Long courseId) {
    if (courseId == null) return 0;
    EntityManager em = JpaUtil.emf().createEntityManager();
    try {
      Integer seats = em.createQuery(
          "select c.maxSeats from Course c where c.id = :cid", Integer.class)
          .setParameter("cid", courseId)
          .getSingleResult();
      return seats == null ? 0 : seats;
    } finally {
      em.close();
    }
  }

  public boolean isStudentInCourse(Long studentId, Long courseId) {
    if (studentId == null || courseId == null) return false;
    EntityManager em = JpaUtil.emf().createEntityManager();
    try {
      Long cnt = em.createQuery(
          "select count(s) from Course c join c.students s " +
          "where c.id = :cid and s.id = :sid", Long.class)
          .setParameter("cid", courseId)
          .setParameter("sid", studentId)
          .getSingleResult();
      return cnt != null && cnt > 0;
    } finally {
      em.close();
    }
  }
}
