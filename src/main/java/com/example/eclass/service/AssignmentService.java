package com.example.eclass.service;

import com.example.eclass.entity.Assignment;
import com.example.eclass.entity.User;
import com.example.eclass.repository.AssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AssignmentService {
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    public List<Assignment> findAll() {
        return assignmentRepository.findAllByOrderByCreatedDateDesc();
    }
    
    public Optional<Assignment> findById(Long id) {
        return assignmentRepository.findById(id);
    }
    
    public List<Assignment> findByTeacher(User teacher) {
        return assignmentRepository.findByTeacherOrderByCreatedDateDesc(teacher);
    }
    
    public Assignment save(Assignment assignment) {
        if (assignment.getCreatedDate() == null) {
            assignment.setCreatedDate(LocalDateTime.now());
        }
        return assignmentRepository.save(assignment);
    }
    
    public Assignment createAssignment(String title, String description, LocalDateTime dueDate, User teacher) {
        Assignment assignment = new Assignment(title, description, dueDate, teacher);
        return save(assignment);
    }
    
    public void delete(Assignment assignment) {
        assignmentRepository.delete(assignment);
    }
    
    public void deleteById(Long id) {
        assignmentRepository.deleteById(id);
    }
    
    public List<Assignment> findUpcomingAssignments() {
        return assignmentRepository.findUpcomingAssignments(LocalDateTime.now());
    }
    
    public List<Assignment> findOverdueAssignments() {
        return assignmentRepository.findOverdueAssignments(LocalDateTime.now());
    }
    
    public List<Assignment> searchByTitle(String title) {
        return assignmentRepository.findByTitleContainingIgnoreCase(title);
    }
    
    public List<Assignment> findByDueDateBefore(LocalDateTime dateTime) {
        return assignmentRepository.findByDueDateBefore(dateTime);
    }
    
    public List<Assignment> findByDueDateAfter(LocalDateTime dateTime) {
        return assignmentRepository.findByDueDateAfter(dateTime);
    }
    
    public long countAll() {
        return assignmentRepository.count();
    }
    
    public long countByTeacher(User teacher) {
        return assignmentRepository.findByTeacher(teacher).size();
    }
    
    public long countOverdue() {
        return assignmentRepository.findOverdueAssignments(LocalDateTime.now()).size();
    }
    
    public long countUpcoming() {
        return assignmentRepository.findUpcomingAssignments(LocalDateTime.now()).size();
    }
}