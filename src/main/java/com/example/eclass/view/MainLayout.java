package com.example.eclass.view;

import com.example.eclass.entity.Role;
import com.example.eclass.security.SecurityUtils;
import com.example.eclass.view.admin.AdminDashboardView;
import com.example.eclass.view.student.StudentDashboardView;
import com.example.eclass.view.teacher.TeacherDashboardView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;

public class MainLayout extends AppLayout {
    
    @Autowired
    private SecurityUtils securityUtils;
    
    public MainLayout(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
        createHeader();
        createDrawer();
    }
    
    private void createHeader() {
        H1 logo = new H1("E-Class 系統");
        logo.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.Margin.MEDIUM
        );
        
        // 用戶信息和登出按鈕
        String userName = securityUtils.getAuthenticatedUser()
            .map(user -> user.getFullName() + " (" + user.getRole().getDisplayName() + ")")
            .orElse("未知用戶");
        
        Span userInfo = new Span(userName);
        userInfo.addClassNames(LumoUtility.FontSize.SMALL);
        
        Button logoutButton = new Button("登出", new Icon(VaadinIcon.SIGN_OUT));
        logoutButton.addClickListener(e -> securityUtils.logout());
        
        HorizontalLayout userLayout = new HorizontalLayout(userInfo, logoutButton);
        userLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        userLayout.addClassNames(LumoUtility.Gap.MEDIUM);
        
        HorizontalLayout header = new HorizontalLayout(
            new DrawerToggle(),
            logo
        );
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames(
            LumoUtility.Padding.Vertical.NONE,
            LumoUtility.Padding.Horizontal.MEDIUM
        );
        
        // 將用戶信息添加到header右側
        header.add(userLayout);
        
        addToNavbar(header);
    }
    
    private void createDrawer() {
        SideNav nav = new SideNav();
        
        if (securityUtils.hasRole(Role.ADMIN)) {
            nav.addItem(new SideNavItem("管理員儀表板", AdminDashboardView.class, VaadinIcon.DASHBOARD.create()));
        } else if (securityUtils.hasRole(Role.TEACHER)) {
            nav.addItem(new SideNavItem("老師儀表板", TeacherDashboardView.class, VaadinIcon.DASHBOARD.create()));
        } else if (securityUtils.hasRole(Role.STUDENT)) {
            nav.addItem(new SideNavItem("學生儀表板", StudentDashboardView.class, VaadinIcon.DASHBOARD.create()));
        }
        
        VerticalLayout drawerContent = new VerticalLayout(nav);
        drawerContent.setSizeFull();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);
        
        addToDrawer(drawerContent);
    }
}