package com.mycompany.courseregistrationsystem.repository;

import com.mycompany.courseregistrationsystem.controller.JpaUtil;
import com.mycompany.courseregistrationsystem.model.Course;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class CourseRepository {

  public Course save(Course c) {
    EntityManager em = JpaUtil.emf().createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();

      if (c.getId() == null) {

        TypedQuery<Long> q = em.createQuery(
            "select count(x) from Course x where x.code = :code", Long.class);
        q.setParameter("code", c.getCode());
        if (q.getSingleResult() > 0L) {
          throw new javax.persistence.PersistenceException("Duplicate course code: " + c.getCode());
        }
        em.persist(c);
      } else {
        c = em.merge(c);
      }

      em.flush();
      tx.commit();
      return c;
    } finally {
      if (tx.isActive()) tx.rollback();
      em.close();
    }
  }

  public List<Course> findAll() {
    EntityManager em = JpaUtil.emf().createEntityManager();
    try {
      return em.createQuery("from Course c order by c.id", Course.class).getResultList();
    } finally {
      em.close();
    }
  }

  public Optional<Course> findByCode(String code) {
    EntityManager em = JpaUtil.emf().createEntityManager();
    try {
      return em.createQuery("from Course c where c.code = :code", Course.class)
          .setParameter("code", code)
          .getResultStream()
          .findFirst();
    } finally {
      em.close();
    }
  }

  public void deleteById(Long id) {
    EntityManager em = JpaUtil.emf().createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();
      Course c = em.find(Course.class, id);
      if (c != null) {
        em.remove(c);
      }
      tx.commit();
    } finally {
      if (tx.isActive()) tx.rollback();
      em.close();
    }
  }

  public Optional<Course> findById(Long id) {
    EntityManager em = JpaUtil.emf().createEntityManager();
    try {
      return Optional.ofNullable(em.find(Course.class, id));
    } finally {
      em.close();
    }
  }
}
