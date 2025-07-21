package com.example.eclass.config;

import com.example.eclass.entity.Assignment;
import com.example.eclass.entity.Role;
import com.example.eclass.entity.User;
import com.example.eclass.service.AssignmentService;
import com.example.eclass.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AssignmentService assignmentService;
    
    @Override
    public void run(String... args) throws Exception {
        // 創建默認管理員賬戶
        if (!userService.existsByUsername("admin")) {
            userService.createUser("admin", "admin123", "系統管理員", "admin@eclass.com", Role.ADMIN);
            System.out.println("默認管理員賬戶已創建: admin/admin123");
        }
        
        // 創建測試老師賬戶
        if (!userService.existsByUsername("teacher1")) {
            userService.createUser("teacher1", "teacher123", "張老師", "teacher1@eclass.com", Role.TEACHER);
            System.out.println("測試老師賬戶已創建: teacher1/teacher123");
        }
        
        // 創建測試學生賬戶
        if (!userService.existsByUsername("student1")) {
            userService.createUser("student1", "student123", "李小明", "student1@eclass.com", Role.STUDENT);
            System.out.println("測試學生賬戶已創建: student1/student123");
        }
        
        if (!userService.existsByUsername("student2")) {
            userService.createUser("student2", "student123", "王小華", "student2@eclass.com", Role.STUDENT);
            System.out.println("測試學生賬戶已創建: student2/student123");
        }
        
        // 創建測試作業
        User teacher = userService.findByUsername("teacher1").orElse(null);
        if (teacher != null && assignmentService.findByTeacher(teacher).isEmpty()) {
            assignmentService.createAssignment(
                "數學作業 - 第一章",
                "請完成教科書第一章的所有練習題，並寫出詳細的解題過程。",
                LocalDateTime.now().plusDays(7),
                teacher
            );
            
            assignmentService.createAssignment(
                "英語作文",
                "寫一篇關於'我的夢想'的英語作文，不少於200字。",
                LocalDateTime.now().plusDays(5),
                teacher
            );
            
            System.out.println("測試作業已創建");
        }
    }
}