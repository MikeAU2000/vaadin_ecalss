package com.example.eclass.config;

import com.example.eclass.security.CustomUserDetailsService;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 允許訪問H2控制台
        http.authorizeHttpRequests(auth -> 
            auth.requestMatchers(
                AntPathRequestMatcher.antMatcher("/h2-console/**"),
                AntPathRequestMatcher.antMatcher("/VAADIN/**"),
                AntPathRequestMatcher.antMatcher("/vaadinServlet/**")
            ).permitAll()
        );
        
        // 禁用H2控制台的CSRF和frame選項
        http.csrf(csrf -> csrf
            .ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**"))
        );
        
        http.headers(headers -> headers
            .frameOptions().sameOrigin()
        );
        
        super.configure(http);
        
        // 設置登錄頁面
        setLoginView(http, "/login");
    }
}