package com.mycompany.courseregistrationsystem.controller;

import com.mycompany.courseregistrationsystem.model.Student;
import com.mycompany.courseregistrationsystem.repository.StudentRepository;

import java.util.List;

public class StudentController {

  private final StudentRepository repo = new StudentRepository();

  public List<Student> loadAll() {
    return repo.findAll();
  }

  public Student add(String matricola, String fullName, String email) {
    Student s = new Student();
    s.setMatricola(matricola);
    s.setFullName(fullName);
    s.setEmail(email);
    return repo.save(s);
  }

  public Student update(Long id, String matricola, String fullName, String email) {
    Student s = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Student not found"));
    s.setMatricola(matricola);
    s.setFullName(fullName);
    s.setEmail(email);
    return repo.save(s);
  }

  public void delete(Long id) {
    repo.deleteById(id);
  }
}
