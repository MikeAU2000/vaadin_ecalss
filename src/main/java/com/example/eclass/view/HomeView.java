package com.example.eclass.view;

import com.example.eclass.entity.Role;
import com.example.eclass.security.SecurityUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

@Route("")
@PermitAll
public class HomeView extends Div implements BeforeEnterObserver {
    
    @Autowired
    private SecurityUtils securityUtils;
    
    public HomeView(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }
    
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!securityUtils.isUserLoggedIn()) {
            // 用戶未登錄，重定向到登錄頁面
            event.rerouteTo("login");
            return;
        }
        
        // 根據用戶角色重定向到相應的儀表板
        var user = securityUtils.getAuthenticatedUser();
        if (user.isPresent()) {
            Role role = user.get().getRole();
            switch (role) {
                case ADMIN:
                    event.rerouteTo("admin");
                    break;
                case TEACHER:
                    event.rerouteTo("teacher");
                    break;
                case STUDENT:
                    event.rerouteTo("student");
                    break;
                default:
                    event.rerouteTo("login");
            }
        } else {
            event.rerouteTo("login");
        }
    }
}