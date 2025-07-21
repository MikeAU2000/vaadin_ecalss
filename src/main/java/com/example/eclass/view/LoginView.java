package com.example.eclass.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("登錄 | E-Class 系統")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    
    private final LoginForm login = new LoginForm();
    
    public LoginView() {
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        
        login.setAction("login");
        
        // 創建標題和說明
        H1 title = new H1("E-Class 電子課堂系統");
        title.getStyle().set("color", "var(--lumo-primary-color)");
        
        Paragraph description = new Paragraph("請使用您的賬戶登錄系統");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        // 測試賬戶信息
        Paragraph testAccounts = new Paragraph(
            "測試賬戶:\n" +
            "管理員: admin / admin123\n" +
            "老師: teacher1 / teacher123\n" +
            "學生: student1 / student123"
        );
        testAccounts.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "var(--lumo-space-m)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("font-size", "var(--lumo-font-size-s)")
            .set("white-space", "pre-line");
        
        VerticalLayout loginLayout = new VerticalLayout();
        loginLayout.setAlignItems(Alignment.CENTER);
        loginLayout.setMaxWidth("400px");
        loginLayout.add(title, description, login, testAccounts);
        
        add(loginLayout);
    }
    
    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // 檢查是否有登錄錯誤
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
        }
    }
}