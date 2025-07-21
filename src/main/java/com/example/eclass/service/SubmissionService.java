package com.example.eclass.service;

import com.example.eclass.entity.Assignment;
import com.example.eclass.entity.Submission;
import com.example.eclass.entity.User;
import com.example.eclass.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubmissionService {
    
    @Autowired
    private SubmissionRepository submissionRepository;
    
    public List<Submission> findAll() {
        return submissionRepository.findAll();
    }
    
    public Optional<Submission> findById(Long id) {
        return submissionRepository.findById(id);
    }
    
    public List<Submission> findByStudent(User student) {
        return submissionRepository.findByStudentOrderBySubmittedDateDesc(student);
    }
    
    public List<Submission> findByAssignment(Assignment assignment) {
        return submissionRepository.findByAssignmentOrderBySubmittedDateDesc(assignment);
    }
    
    public Optional<Submission> findByAssignmentAndStudent(Assignment assignment, User student) {
        return submissionRepository.findByAssignmentAndStudent(assignment, student);
    }
    
    public List<Submission> findByTeacher(User teacher) {
        return submissionRepository.findByTeacher(teacher);
    }
    
    public List<Submission> findUngradedByTeacher(User teacher) {
        return submissionRepository.findUngraded(teacher);
    }
    
    public Submission save(Submission submission) {
        if (submission.getSubmittedDate() == null) {
            submission.setSubmittedDate(LocalDateTime.now());
        }
        return submissionRepository.save(submission);
    }
    
    public Submission submitAssignment(Assignment assignment, User student, String content) {
        // 檢查是否已經提交過
        Optional<Submission> existingSubmission = findByAssignmentAndStudent(assignment, student);
        if (existingSubmission.isPresent()) {
            throw new RuntimeException("您已經提交過這個作業了");
        }
        
        Submission submission = new Submission(content, assignment, student);
        return save(submission);
    }
    
    public Submission updateSubmission(Assignment assignment, User student, String content) {
        Optional<Submission> existingSubmission = findByAssignmentAndStudent(assignment, student);
        if (existingSubmission.isPresent()) {
            Submission submission = existingSubmission.get();
            submission.setContent(content);
            submission.setSubmittedDate(LocalDateTime.now());
            return save(submission);
        } else {
            return submitAssignment(assignment, student, content);
        }
    }
    
    public Submission gradeSubmission(Long submissionId, Integer grade, String feedback) {
        Optional<Submission> submissionOpt = findById(submissionId);
        if (submissionOpt.isPresent()) {
            Submission submission = submissionOpt.get();
            submission.setGrade(grade);
            submission.setFeedback(feedback);
            return save(submission);
        }
        throw new RuntimeException("找不到指定的提交記錄");
    }
    
    public void delete(Submission submission) {
        submissionRepository.delete(submission);
    }
    
    public void deleteById(Long id) {
        submissionRepository.deleteById(id);
    }
    
    public boolean hasSubmitted(Assignment assignment, User student) {
        return submissionRepository.existsByAssignmentAndStudent(assignment, student);
    }
    
    public long countByAssignment(Assignment assignment) {
        return submissionRepository.countByAssignment(assignment);
    }
    
    public long countByStudent(User student) {
        return submissionRepository.findByStudent(student).size();
    }
    
    public long countUngradedByTeacher(User teacher) {
        return submissionRepository.findUngraded(teacher).size();
    }
}