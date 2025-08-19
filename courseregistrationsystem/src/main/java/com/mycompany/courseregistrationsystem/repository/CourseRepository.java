package com.mycompany.courseregistrationsystem.repository;

import com.mycompany.courseregistrationsystem.controller.JpaUtil;
import com.mycompany.courseregistrationsystem.model.Course;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

public class CourseRepository {

  public Course save(Course c) {
    EntityManager em = JpaUtil.emf().createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();
      if (c.getId() == null) {
        em.persist(c);
        em.flush();
      } else {
        c = em.merge(c);
        em.flush(); 
      }
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

      return em.createQuery("from Course c ORDER BY c.id", Course.class).getResultList();
    } finally {
      em.close();
    }
  }

  public Optional<Course> findByCode(String code) {
    EntityManager em = JpaUtil.emf().createEntityManager();
    try {
      List<Course> list = em.createQuery(
          "from Course c where c.code = :code", Course.class)
          .setParameter("code", code)
          .getResultList();
      return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
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
        em.flush();
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
	        Course course = em.find(Course.class, id);
	        return course != null ? Optional.of(course) : Optional.empty();
	    } finally {
	        em.close();
	    }
	}

}
