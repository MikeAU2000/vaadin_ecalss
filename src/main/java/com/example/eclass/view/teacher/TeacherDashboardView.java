package com.example.eclass.view.teacher;

import com.example.eclass.entity.Assignment;
import com.example.eclass.entity.Submission;
import com.example.eclass.security.SecurityUtils;
import com.example.eclass.service.AssignmentService;
import com.example.eclass.service.SubmissionService;
import com.example.eclass.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "teacher", layout = MainLayout.class)
@PageTitle("老師儀表板")
@RolesAllowed("TEACHER")
public class TeacherDashboardView extends VerticalLayout {
    
    @Autowired
    private AssignmentService assignmentService;
    
    @Autowired
    private SubmissionService submissionService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    private Grid<Assignment> assignmentGrid;
    private Grid<Submission> submissionGrid;
    
    public TeacherDashboardView(AssignmentService assignmentService, 
                               SubmissionService submissionService,
                               SecurityUtils securityUtils) {
        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
        this.securityUtils = securityUtils;
        
        setSizeFull();
        
        createHeader();
        createAssignmentSection();
        createSubmissionSection();
        
        refreshData();
    }
    
    private void createHeader() {
        H2 title = new H2("老師儀表板");
        
        var currentUser = securityUtils.getAuthenticatedUser().orElse(null);
        if (currentUser != null) {
            // 統計信息
            long totalAssignments = assignmentService.countByTeacher(currentUser);
            long ungradedSubmissions = submissionService.countUngradedByTeacher(currentUser);
            
            HorizontalLayout stats = new HorizontalLayout();
            stats.add(
                createStatCard("我的作業", String.valueOf(totalAssignments), VaadinIcon.TASKS),
                createStatCard("待評分", String.valueOf(ungradedSubmissions), VaadinIcon.CLOCK)
            );
            
            add(title, stats);
        } else {
            add(title);
        }
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
    
    private void createAssignmentSection() {
        H3 sectionTitle = new H3("我的作業");
        
        Button addAssignmentButton = new Button("新增作業", VaadinIcon.PLUS.create());
        addAssignmentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addAssignmentButton.addClickListener(e -> openAssignmentDialog(null));
        
        HorizontalLayout header = new HorizontalLayout(sectionTitle, addAssignmentButton);
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setWidthFull();
        
        assignmentGrid = new Grid<>(Assignment.class, false);
        assignmentGrid.addColumn(Assignment::getTitle).setHeader("標題").setSortable(true);
        assignmentGrid.addColumn(assignment -> 
            assignment.getDescription() != null && assignment.getDescription().length() > 50 ?
            assignment.getDescription().substring(0, 50) + "..." :
            assignment.getDescription()
        ).setHeader("描述");
        assignmentGrid.addColumn(assignment -> 
            assignment.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        ).setHeader("截止時間").setSortable(true);
        assignmentGrid.addColumn(assignment -> 
            submissionService.countByAssignment(assignment)
        ).setHeader("提交數量");
        assignmentGrid.addColumn(new ComponentRenderer<>(assignment -> {
            Span status = new Span(assignment.isOverdue() ? "已過期" : "進行中");
            status.getElement().getThemeList().add(
                assignment.isOverdue() ? "badge error" : "badge success"
            );
            return status;
        })).setHeader("狀態");
        
        assignmentGrid.addComponentColumn(this::createAssignmentActionButtons)
            .setHeader("操作").setFlexGrow(0);
        
        assignmentGrid.setHeight("300px");
        
        add(header, assignmentGrid);
    }
    
    private void createSubmissionSection() {
        H3 sectionTitle = new H3("學生提交");
        
        submissionGrid = new Grid<>(Submission.class, false);
        submissionGrid.addColumn(submission -> submission.getAssignment().getTitle())
            .setHeader("作業標題").setSortable(true);
        submissionGrid.addColumn(submission -> submission.getStudent().getFullName())
            .setHeader("學生姓名").setSortable(true);
        submissionGrid.addColumn(submission -> 
            submission.getSubmittedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        ).setHeader("提交時間").setSortable(true);
        submissionGrid.addColumn(new ComponentRenderer<>(submission -> {
            Span status = new Span(submission.isLate() ? "遲交" : "準時");
            status.getElement().getThemeList().add(
                submission.isLate() ? "badge error" : "badge success"
            );
            return status;
        })).setHeader("狀態");
        submissionGrid.addColumn(submission -> 
            submission.getGrade() != null ? submission.getGrade().toString() : "未評分"
        ).setHeader("分數");
        
        submissionGrid.addComponentColumn(this::createSubmissionActionButtons)
            .setHeader("操作").setFlexGrow(0);
        
        submissionGrid.setHeight("300px");
        
        add(sectionTitle, submissionGrid);
    }
    
    private HorizontalLayout createAssignmentActionButtons(Assignment assignment) {
        Button viewButton = new Button(VaadinIcon.EYE.create());
        viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        viewButton.addClickListener(e -> viewAssignmentSubmissions(assignment));
        
        Button editButton = new Button(VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.addClickListener(e -> openAssignmentDialog(assignment));
        
        Button deleteButton = new Button(VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> confirmDeleteAssignment(assignment));
        
        return new HorizontalLayout(viewButton, editButton, deleteButton);
    }
    
    private HorizontalLayout createSubmissionActionButtons(Submission submission) {
        Button gradeButton = new Button(submission.getGrade() != null ? "重新評分" : "評分");
        gradeButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        gradeButton.addClickListener(e -> openGradeDialog(submission));
        
        return new HorizontalLayout(gradeButton);
    }
    
    private void openAssignmentDialog(Assignment assignment) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(assignment == null ? "新增作業" : "編輯作業");
        dialog.setWidth("600px");
        
        FormLayout formLayout = new FormLayout();
        
        TextField titleField = new TextField("作業標題");
        TextArea descriptionField = new TextArea("作業描述");
        descriptionField.setHeight("150px");
        DateTimePicker dueDateField = new DateTimePicker("截止時間");
        
        formLayout.add(titleField, descriptionField, dueDateField);
        
        Binder<Assignment> binder = new Binder<>(Assignment.class);
        binder.forField(titleField)
            .asRequired("作業標題不能為空")
            .bind(Assignment::getTitle, Assignment::setTitle);
        binder.forField(descriptionField)
            .bind(Assignment::getDescription, Assignment::setDescription);
        binder.forField(dueDateField)
            .asRequired("截止時間不能為空")
            .bind(Assignment::getDueDate, Assignment::setDueDate);
        
        Assignment editAssignment = assignment != null ? assignment : new Assignment();
        binder.readBean(editAssignment);
        
        Button saveButton = new Button("保存", e -> {
            try {
                binder.writeBean(editAssignment);
                
                var currentUser = securityUtils.getAuthenticatedUser().orElse(null);
                if (currentUser != null) {
                    editAssignment.setTeacher(currentUser);
                    assignmentService.save(editAssignment);
                    
                    refreshData();
                    dialog.close();
                    Notification.show(assignment == null ? "作業創建成功" : "作業更新成功")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
                
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
    
    private void openGradeDialog(Submission submission) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("評分作業");
        dialog.setWidth("500px");
        
        VerticalLayout layout = new VerticalLayout();
        
        // 顯示學生信息和作業內容
        layout.add(new Span("學生: " + submission.getStudent().getFullName()));
        layout.add(new Span("作業: " + submission.getAssignment().getTitle()));
        layout.add(new Span("提交時間: " + submission.getSubmittedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        
        TextArea contentArea = new TextArea("學生提交內容");
        contentArea.setValue(submission.getContent() != null ? submission.getContent() : "");
        contentArea.setReadOnly(true);
        contentArea.setHeight("200px");
        contentArea.setWidthFull();
        
        TextField gradeField = new TextField("分數 (0-100)");
        if (submission.getGrade() != null) {
            gradeField.setValue(submission.getGrade().toString());
        }
        
        TextArea feedbackField = new TextArea("評語");
        if (submission.getFeedback() != null) {
            feedbackField.setValue(submission.getFeedback());
        }
        feedbackField.setHeight("100px");
        
        layout.add(contentArea, gradeField, feedbackField);
        
        Button saveButton = new Button("保存評分", e -> {
            try {
                Integer grade = null;
                if (!gradeField.getValue().isEmpty()) {
                    grade = Integer.parseInt(gradeField.getValue());
                    if (grade < 0 || grade > 100) {
                        throw new NumberFormatException("分數必須在0-100之間");
                    }
                }
                
                submissionService.gradeSubmission(
                    submission.getId(),
                    grade,
                    feedbackField.getValue()
                );
                
                refreshData();
                dialog.close();
                Notification.show("評分保存成功")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    
            } catch (NumberFormatException ex) {
                Notification.show("請輸入有效的分數 (0-100)")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                Notification.show("保存失敗: " + ex.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button cancelButton = new Button("取消", e -> dialog.close());
        
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.add(layout);
        dialog.open();
    }
    
    private void viewAssignmentSubmissions(Assignment assignment) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("作業提交情況: " + assignment.getTitle());
        dialog.setWidth("800px");
        dialog.setHeight("600px");
        
        Grid<Submission> submissionGrid = new Grid<>(Submission.class, false);
        submissionGrid.addColumn(submission -> submission.getStudent().getFullName())
            .setHeader("學生姓名");
        submissionGrid.addColumn(submission -> 
            submission.getSubmittedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        ).setHeader("提交時間");
        submissionGrid.addColumn(submission -> 
            submission.getGrade() != null ? submission.getGrade().toString() : "未評分"
        ).setHeader("分數");
        submissionGrid.addColumn(new ComponentRenderer<>(submission -> {
            Span status = new Span(submission.isLate() ? "遲交" : "準時");
            status.getElement().getThemeList().add(
                submission.isLate() ? "badge error" : "badge success"
            );
            return status;
        })).setHeader("狀態");
        
        List<Submission> submissions = submissionService.findByAssignment(assignment);
        submissionGrid.setItems(submissions);
        submissionGrid.setSizeFull();
        
        Button closeButton = new Button("關閉", e -> dialog.close());
        dialog.getFooter().add(closeButton);
        dialog.add(submissionGrid);
        dialog.open();
    }
    
    private void confirmDeleteAssignment(Assignment assignment) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("確認刪除");
        dialog.setText("確定要刪除作業 \"" + assignment.getTitle() + "\" 嗎？此操作將同時刪除所有相關的學生提交。");
        dialog.setCancelable(true);
        dialog.setConfirmText("刪除");
        dialog.setConfirmButtonTheme("error primary");
        
        dialog.addConfirmListener(e -> {
            try {
                assignmentService.delete(assignment);
                refreshData();
                Notification.show("作業已刪除")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("刪除失敗: " + ex.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        
        dialog.open();
    }
    
    private void refreshData() {
        var currentUser = securityUtils.getAuthenticatedUser().orElse(null);
        if (currentUser != null) {
            List<Assignment> assignments = assignmentService.findByTeacher(currentUser);
            assignmentGrid.setItems(assignments);
            
            List<Submission> submissions = submissionService.findByTeacher(currentUser);
            submissionGrid.setItems(submissions);
        }
    }
}