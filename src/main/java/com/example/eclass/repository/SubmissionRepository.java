package com.example.eclass.repository;

import com.example.eclass.entity.Assignment;
import com.example.eclass.entity.Submission;
import com.example.eclass.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    
    List<Submission> findByStudent(User student);
    
    List<Submission> findByAssignment(Assignment assignment);
    
    Optional<Submission> findByAssignmentAndStudent(Assignment assignment, User student);
    
    List<Submission> findByStudentOrderBySubmittedDateDesc(User student);
    
    List<Submission> findByAssignmentOrderBySubmittedDateDesc(Assignment assignment);
    
    boolean existsByAssignmentAndStudent(Assignment assignment, User student);
    
    @Query("SELECT s FROM Submission s WHERE s.assignment.teacher = :teacher ORDER BY s.submittedDate DESC")
    List<Submission> findByTeacher(@Param("teacher") User teacher);
    
    @Query("SELECT s FROM Submission s WHERE s.grade IS NULL AND s.assignment.teacher = :teacher")
    List<Submission> findUngraded(@Param("teacher") User teacher);
    
    @Query("SELECT COUNT(s) FROM Submission s WHERE s.assignment = :assignment")
    long countByAssignment(@Param("assignment") Assignment assignment);
}