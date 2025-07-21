package com.example.eclass.service;

import com.example.eclass.entity.Role;
import com.example.eclass.entity.User;
import com.example.eclass.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }
    
    public List<User> findTeachers() {
        return userRepository.findByRoleAndEnabledTrue(Role.TEACHER);
    }
    
    public List<User> findStudents() {
        return userRepository.findByRoleAndEnabledTrue(Role.STUDENT);
    }
    
    public User save(User user) {
        if (user.getId() == null) {
            // 新用戶，加密密碼
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // 更新用戶，檢查密碼是否需要重新加密
            User existingUser = userRepository.findById(user.getId()).orElse(null);
            if (existingUser != null && !existingUser.getPassword().equals(user.getPassword())) {
                // 密碼已更改，需要重新加密
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }
        return userRepository.save(user);
    }
    
    public User createUser(String username, String password, String fullName, String email, Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用戶名已存在: " + username);
        }
        if (email != null && userRepository.existsByEmail(email)) {
            throw new RuntimeException("電子郵件已存在: " + email);
        }
        
        User user = new User(username, password, fullName, email, role);
        return save(user);
    }
    
    public void delete(User user) {
        userRepository.delete(user);
    }
    
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public List<User> searchByName(String name) {
        return userRepository.findByFullNameContainingIgnoreCase(name);
    }
    
    public List<User> searchByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username);
    }
    
    public void toggleUserStatus(User user) {
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }
    
    public long countByRole(Role role) {
        return userRepository.findByRole(role).size();
    }
}