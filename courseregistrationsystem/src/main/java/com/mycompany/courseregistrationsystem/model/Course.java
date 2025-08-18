package com.mycompany.courseregistrationsystem.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "courses",
       uniqueConstraints = @UniqueConstraint(name = "uk_course_code", columnNames = "code"))
public class Course {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 32, unique = true)
  private String code;

  @Column(nullable = false, length = 120)
  private String title;

  @Column(nullable = false)
  private int cfu;

  @Column(nullable = false)
  private int maxSeats;

  @ManyToMany(mappedBy = "courses")
  private Set<Student> students = new HashSet<>();

  public Course() {}

  public Long getId() { return id; }
  public String getCode() { return code; }
  public void setCode(String code) { this.code = code; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public int getCfu() { return cfu; }
  public void setCfu(int cfu) { this.cfu = cfu; }
  public int getMaxSeats() { return maxSeats; }
  public void setMaxSeats(int maxSeats) { this.maxSeats = maxSeats; }
  public Set<Student> getStudents() { return students; }
}
