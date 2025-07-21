package com.example.eclass.security;

import com.example.eclass.entity.Role;
import com.example.eclass.entity.User;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtils {
    
    @Autowired
    private AuthenticationContext authenticationContext;
    
    public Optional<User> getAuthenticatedUser() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .map(userDetails -> {
                    if (userDetails instanceof CustomUserDetailsService.CustomUserPrincipal) {
                        return ((CustomUserDetailsService.CustomUserPrincipal) userDetails).getUser();
                    }
                    return null;
                });
    }
    
    public boolean isUserLoggedIn() {
        return authenticationContext.isAuthenticated();
    }
    
    public boolean hasRole(Role role) {
        return getAuthenticatedUser()
                .map(user -> user.getRole() == role)
                .orElse(false);
    }
    
    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }
    
    public boolean isTeacher() {
        return hasRole(Role.TEACHER);
    }
    
    public boolean isStudent() {
        return hasRole(Role.STUDENT);
    }
    
    public void logout() {
        authenticationContext.logout();
    }
}