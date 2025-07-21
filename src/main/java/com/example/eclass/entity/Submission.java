package com.example.eclass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
public class Submission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    private LocalDateTime submittedDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @Column
    private Integer grade;
    
    @Column(columnDefinition = "TEXT")
    private String feedback;
    
    // 構造函數
    public Submission() {
        this.submittedDate = LocalDateTime.now();
    }
    
    public Submission(String content, Assignment assignment, User student) {
        this();
        this.content = content;
        this.assignment = assignment;
        this.student = student;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getSubmittedDate() {
        return submittedDate;
    }
    
    public void setSubmittedDate(LocalDateTime submittedDate) {
        this.submittedDate = submittedDate;
    }
    
    public Assignment getAssignment() {
        return assignment;
    }
    
    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }
    
    public User getStudent() {
        return student;
    }
    
    public void setStudent(User student) {
        this.student = student;
    }
    
    public Integer getGrade() {
        return grade;
    }
    
    public void setGrade(Integer grade) {
        this.grade = grade;
    }
    
    public String getFeedback() {
        return feedback;
    }
    
    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
    
    public boolean isLate() {
        return submittedDate.isAfter(assignment.getDueDate());
    }
    
    @Override
    public String toString() {
        return "Submission{" +
                "id=" + id +
                ", submittedDate=" + submittedDate +
                ", student=" + (student != null ? student.getFullName() : "null") +
                ", assignment=" + (assignment != null ? assignment.getTitle() : "null") +
                ", grade=" + grade +
                '}';
    }
}