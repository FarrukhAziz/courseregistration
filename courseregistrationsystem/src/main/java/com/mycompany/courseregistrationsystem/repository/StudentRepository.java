package com.mycompany.courseregistrationsystem.repository;

import com.mycompany.courseregistrationsystem.controller.JpaUtil;
import com.mycompany.courseregistrationsystem.model.Student;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

public class StudentRepository {

  public Student save(Student s) {
    EntityManager em = JpaUtil.emf().createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();
      if (s.getId() == null) em.persist(s);
      else s = em.merge(s);
      tx.commit();
      return s;
    } finally {
      if (tx.isActive()) tx.rollback();
      em.close();
    }
  }

  public List<Student> findAll() {
    EntityManager em = JpaUtil.emf().createEntityManager();
    try { return em.createQuery("from Student", Student.class).getResultList(); }
    finally { em.close(); }
  }

  public Optional<Student> findById(Long id) {
    EntityManager em = JpaUtil.emf().createEntityManager();
    try { return Optional.ofNullable(em.find(Student.class, id)); }
    finally { em.close(); }
  }

  public Optional<Student> findByMatricola(String m) {
    EntityManager em = JpaUtil.emf().createEntityManager();
    try {
      List<Student> list = em.createQuery(
          "from Student s where s.matricola = :m", Student.class)
          .setParameter("m", m).getResultList();
      return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    } finally { em.close(); }
  }

  public void deleteById(Long id) {
    EntityManager em = JpaUtil.emf().createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();
      Student s = em.find(Student.class, id);
      if (s != null) em.remove(s);
      tx.commit();
    } finally {
      if (tx.isActive()) tx.rollback();
      em.close();
    }
  }
}
