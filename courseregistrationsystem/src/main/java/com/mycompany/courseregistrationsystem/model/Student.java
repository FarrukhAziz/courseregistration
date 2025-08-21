package com.mycompany.courseregistrationsystem.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students",
       uniqueConstraints = @UniqueConstraint(name = "uk_student_matricola", columnNames = "matricola"))
public class Student {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 32, unique = true)
  private String matricola;

  @Column(nullable = false, length = 100)
  private String fullName;

  @Column(nullable = false, length = 120)
  private String email;

  @ManyToMany
  @JoinTable(name = "enrollments",
      joinColumns = @JoinColumn(name = "student_id"),
      inverseJoinColumns = @JoinColumn(name = "course_id"))
  private Set<Course> courses = new HashSet<Course>();

  public Student() {}

  public Long getId() { return id; }
  public String getMatricola() { return matricola; }
  public void setMatricola(String matricola) { this.matricola = matricola; }
  public String getFullName() { return fullName; }
  public void setFullName(String fullName) { this.fullName = fullName; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public Set<Course> getCourses() { return courses; }
}
