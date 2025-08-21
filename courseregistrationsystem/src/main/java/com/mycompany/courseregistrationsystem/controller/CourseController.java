package com.mycompany.courseregistrationsystem.controller;

import com.mycompany.courseregistrationsystem.model.Course;
import com.mycompany.courseregistrationsystem.repository.CourseRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

public class CourseController {

  private final CourseRepository repo;

  public CourseController() {
    this.repo = new CourseRepository();
  }

  public CourseController(CourseRepository repo) {
    this.repo = repo;
  }

  public List<Course> loadAll() {
    return repo.findAll();
  }

  public Course add(String code, String title, int cfu, int maxSeats) {
    if (isBlank(code) || isBlank(title)) throw new IllegalArgumentException("Code and Title are required.");
    if (cfu <= 0 || maxSeats <= 0) throw new IllegalArgumentException("CFU and Max Seats must be positive.");

    Optional<Course> existing = repo.findByCode(code.trim());
    if (existing.isPresent()) throw new IllegalArgumentException("Course code already exists: " + code);

    Course c = new Course();
    c.setCode(code.trim());
    c.setTitle(title.trim());
    c.setCfu(cfu);
    c.setMaxSeats(maxSeats);
    return repo.save(c);
  }

  public Course update(Long id, String code, String title, int cfu, int maxSeats) {
    if (id == null) throw new IllegalArgumentException("Course id is required.");
    if (isBlank(code) || isBlank(title)) throw new IllegalArgumentException("Code and Title are required.");
    if (cfu <= 0 || maxSeats <= 0) throw new IllegalArgumentException("CFU and Max Seats must be positive.");

    EntityManager em = JpaUtil.emf().createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();
      Course entity = em.find(Course.class, id);
      if (entity == null) throw new IllegalArgumentException("Course not found with id: " + id);

      if (!entity.getCode().equals(code.trim())) {
        Optional<Course> dup = repo.findByCode(code.trim());
        if (dup.isPresent() && !dup.get().getId().equals(id))
          throw new IllegalArgumentException("Course code already exists: " + code);
      }

      entity.setCode(code.trim());
      entity.setTitle(title.trim());
      entity.setCfu(cfu);
      entity.setMaxSeats(maxSeats);

      Course merged = em.merge(entity);
      tx.commit();
      return merged;
    } finally {
      if (tx.isActive()) tx.rollback();
      em.close();
    }
  }

  public void delete(Long id) {
    if (id == null) throw new IllegalArgumentException("Course id is required.");
    repo.deleteById(id);
  }

  public int enrolledCount(Long courseId) {
    if (courseId == null) return 0;
    EntityManager em = JpaUtil.emf().createEntityManager();
    try {
      Integer count = em.createQuery(
          "select size(c.students) from Course c where c.id = :id", Integer.class)
          .setParameter("id", courseId)
          .getSingleResult();
      return count == null ? 0 : count;
    } finally {
      em.close();
    }
  }

  private boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }
}
