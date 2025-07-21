package com.example.eclass.view.admin;

import com.example.eclass.entity.Role;
import com.example.eclass.entity.User;
import com.example.eclass.service.UserService;
import com.example.eclass.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "admin", layout = MainLayout.class)
@PageTitle("管理員儀表板")
@RolesAllowed("ADMIN")
public class AdminDashboardView extends VerticalLayout {
    
    @Autowired
    private UserService userService;
    
    private Grid<User> grid;
    private TextField searchField;
    private ComboBox<Role> roleFilter;
    
    public AdminDashboardView(UserService userService) {
        this.userService = userService;
        setSizeFull();
        
        createHeader();
        createToolbar();
        createGrid();
        
        refreshGrid();
    }
    
    private void createHeader() {
        H2 title = new H2("管理員儀表板");
        
        // 統計信息
        long totalUsers = userService.findAll().size();
        long teachers = userService.countByRole(Role.TEACHER);
        long students = userService.countByRole(Role.STUDENT);
        
        HorizontalLayout stats = new HorizontalLayout();
        stats.add(
            createStatCard("總用戶數", String.valueOf(totalUsers), VaadinIcon.USERS),
            createStatCard("老師數量", String.valueOf(teachers), VaadinIcon.SPECIALIST),
            createStatCard("學生數量", String.valueOf(students), VaadinIcon.ACADEMY_CAP)
        );
        
        add(title, stats);
    }
    
    private VerticalLayout createStatCard(String title, String value, VaadinIcon icon) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("stat-card");
        card.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("padding", "var(--lumo-space-m)")
            .set("text-align", "center")
            .set("min-width", "150px");
        
        Icon cardIcon = icon.create();
        cardIcon.setSize("2em");
        cardIcon.getStyle().set("color", "var(--lumo-primary-color)");
        
        H3 cardValue = new H3(value);
        cardValue.getStyle().set("margin", "0");
        
        Span cardTitle = new Span(title);
        cardTitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        card.add(cardIcon, cardValue, cardTitle);
        card.setAlignItems(Alignment.CENTER);
        
        return card;
    }
    
    private void createToolbar() {
        searchField = new TextField();
        searchField.setPlaceholder("搜索用戶...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addValueChangeListener(e -> refreshGrid());
        
        roleFilter = new ComboBox<>("角色篩選");
        roleFilter.setItems(Role.values());
        roleFilter.setItemLabelGenerator(Role::getDisplayName);
        roleFilter.addValueChangeListener(e -> refreshGrid());
        
        Button addUserButton = new Button("新增用戶", VaadinIcon.PLUS.create());
        addUserButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addUserButton.addClickListener(e -> openUserDialog(null));
        
        Button refreshButton = new Button("刷新", VaadinIcon.REFRESH.create());
        refreshButton.addClickListener(e -> refreshGrid());
        
        HorizontalLayout toolbar = new HorizontalLayout(
            searchField, roleFilter, addUserButton, refreshButton
        );
        toolbar.setAlignItems(Alignment.END);
        
        add(toolbar);
    }
    
    private void createGrid() {
        grid = new Grid<>(User.class, false);
        grid.addColumn(User::getId).setHeader("ID").setWidth("80px").setFlexGrow(0);
        grid.addColumn(User::getUsername).setHeader("用戶名").setSortable(true);
        grid.addColumn(User::getFullName).setHeader("姓名").setSortable(true);
        grid.addColumn(User::getEmail).setHeader("電子郵件");
        grid.addColumn(user -> user.getRole().getDisplayName()).setHeader("角色").setSortable(true);
        grid.addColumn(user -> user.isEnabled() ? "啟用" : "禁用")
            .setHeader("狀態")
            .setSortable(true);
        
        grid.addComponentColumn(this::createActionButtons).setHeader("操作").setFlexGrow(0);
        
        grid.setSizeFull();
        add(grid);
    }
    
    private HorizontalLayout createActionButtons(User user) {
        Button editButton = new Button(VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.addClickListener(e -> openUserDialog(user));
        
        Button toggleButton = new Button(user.isEnabled() ? VaadinIcon.EYE_SLASH.create() : VaadinIcon.EYE.create());
        toggleButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        toggleButton.addClickListener(e -> toggleUserStatus(user));
        
        Button deleteButton = new Button(VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> confirmDelete(user));
        
        return new HorizontalLayout(editButton, toggleButton, deleteButton);
    }
    
    private void openUserDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(user == null ? "新增用戶" : "編輯用戶");
        dialog.setWidth("500px");
        
        FormLayout formLayout = new FormLayout();
        
        TextField usernameField = new TextField("用戶名");
        PasswordField passwordField = new PasswordField("密碼");
        TextField fullNameField = new TextField("姓名");
        EmailField emailField = new EmailField("電子郵件");
        ComboBox<Role> roleComboBox = new ComboBox<>("角色");
        roleComboBox.setItems(Role.values());
        roleComboBox.setItemLabelGenerator(Role::getDisplayName);
        
        formLayout.add(usernameField, passwordField, fullNameField, emailField, roleComboBox);
        
        Binder<User> binder = new Binder<>(User.class);
        binder.forField(usernameField)
            .asRequired("用戶名不能為空")
            .bind(User::getUsername, User::setUsername);
        binder.forField(passwordField)
            .asRequired("密碼不能為空")
            .bind(User::getPassword, User::setPassword);
        binder.forField(fullNameField)
            .asRequired("姓名不能為空")
            .bind(User::getFullName, User::setFullName);
        binder.forField(emailField)
            .bind(User::getEmail, User::setEmail);
        binder.forField(roleComboBox)
            .asRequired("請選擇角色")
            .bind(User::getRole, User::setRole);
        
        User editUser = user != null ? user : new User();
        binder.readBean(editUser);
        
        if (user != null) {
            passwordField.setHelperText("留空則不修改密碼");
            passwordField.setRequired(false);
        }
        
        Button saveButton = new Button("保存", e -> {
            try {
                binder.writeBean(editUser);
                
                if (user == null) {
                    // 新增用戶
                    userService.createUser(
                        editUser.getUsername(),
                        editUser.getPassword(),
                        editUser.getFullName(),
                        editUser.getEmail(),
                        editUser.getRole()
                    );
                } else {
                    // 更新用戶
                    if (passwordField.getValue().isEmpty()) {
                        editUser.setPassword(user.getPassword()); // 保持原密碼
                    }
                    userService.save(editUser);
                }
                
                refreshGrid();
                dialog.close();
                Notification.show(user == null ? "用戶創建成功" : "用戶更新成功")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    
            } catch (ValidationException ex) {
                Notification.show("請檢查輸入的數據")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                Notification.show("操作失敗: " + ex.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button cancelButton = new Button("取消", e -> dialog.close());
        
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.add(formLayout);
        dialog.open();
    }
    
    private void toggleUserStatus(User user) {
        userService.toggleUserStatus(user);
        refreshGrid();
        Notification.show("用戶狀態已更新")
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    
    private void confirmDelete(User user) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("確認刪除");
        dialog.setText("確定要刪除用戶 \"" + user.getFullName() + "\" 嗎？此操作無法撤銷。");
        dialog.setCancelable(true);
        dialog.setConfirmText("刪除");
        dialog.setConfirmButtonTheme("error primary");
        
        dialog.addConfirmListener(e -> {
            try {
                userService.delete(user);
                refreshGrid();
                Notification.show("用戶已刪除")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("刪除失敗: " + ex.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        
        dialog.open();
    }
    
    private void refreshGrid() {
        String searchTerm = searchField.getValue();
        Role selectedRole = roleFilter.getValue();
        
        var users = userService.findAll().stream()
            .filter(user -> {
                boolean matchesSearch = searchTerm == null || searchTerm.isEmpty() ||
                    user.getFullName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    user.getUsername().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchTerm.toLowerCase()));
                
                boolean matchesRole = selectedRole == null || user.getRole() == selectedRole;
                
                return matchesSearch && matchesRole;
            })
            .toList();
        
        grid.setItems(users);
    }
}