package com.example.eclass.repository;

import com.example.eclass.entity.Assignment;
import com.example.eclass.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    

    
    @Query("SELECT a FROM Assignment a JOIN FETCH a.teacher ORDER BY a.createdDate DESC")
    List<Assignment> findAllByOrderByCreatedDateDesc();
    
    @Query("SELECT a FROM Assignment a JOIN FETCH a.teacher WHERE a.dueDate < :dateTime ORDER BY a.dueDate DESC")
    List<Assignment> findByDueDateBefore(@Param("dateTime") LocalDateTime dateTime);
    
    @Query("SELECT a FROM Assignment a JOIN FETCH a.teacher WHERE a.dueDate > :dateTime ORDER BY a.dueDate ASC")
    List<Assignment> findByDueDateAfter(@Param("dateTime") LocalDateTime dateTime);
    
    @Query("SELECT a FROM Assignment a JOIN FETCH a.teacher WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY a.createdDate DESC")
    List<Assignment> findByTitleContainingIgnoreCase(@Param("title") String title);
    
    @Query("SELECT a FROM Assignment a JOIN FETCH a.teacher WHERE a.dueDate > :now ORDER BY a.dueDate ASC")
    List<Assignment> findUpcomingAssignments(@Param("now") LocalDateTime now);
    
    @Query("SELECT a FROM Assignment a JOIN FETCH a.teacher WHERE a.dueDate < :now ORDER BY a.dueDate DESC")
    List<Assignment> findOverdueAssignments(@Param("now") LocalDateTime now);
    
    @Query("SELECT a FROM Assignment a JOIN FETCH a.teacher WHERE a.teacher = :teacher ORDER BY a.createdDate DESC")
    List<Assignment> findByTeacherOrderByCreatedDateDesc(@Param("teacher") User teacher);
    
    @Query("SELECT a FROM Assignment a JOIN FETCH a.teacher WHERE a.teacher = :teacher")
    List<Assignment> findByTeacher(@Param("teacher") User teacher);
}