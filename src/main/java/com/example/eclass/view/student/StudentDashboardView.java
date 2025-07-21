package com.example.eclass.view.student;

import com.example.eclass.entity.Assignment;
import com.example.eclass.entity.Submission;
import com.example.eclass.security.SecurityUtils;
import com.example.eclass.service.AssignmentService;
import com.example.eclass.service.SubmissionService;
import com.example.eclass.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
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
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Route(value = "student", layout = MainLayout.class)
@PageTitle("學生儀表板")
@RolesAllowed("STUDENT")
public class StudentDashboardView extends VerticalLayout {
    
    @Autowired
    private AssignmentService assignmentService;
    
    @Autowired
    private SubmissionService submissionService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    private Grid<Assignment> assignmentGrid;
    private Grid<Submission> submissionGrid;
    
    public StudentDashboardView(AssignmentService assignmentService,
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
        H2 title = new H2("學生儀表板");
        
        var currentUser = securityUtils.getAuthenticatedUser().orElse(null);
        if (currentUser != null) {
            // 統計信息
            long totalAssignments = assignmentService.countAll();
            long mySubmissions = submissionService.countByStudent(currentUser);
            long pendingAssignments = totalAssignments - mySubmissions;
            
            HorizontalLayout stats = new HorizontalLayout();
            stats.add(
                createStatCard("總作業數", String.valueOf(totalAssignments), VaadinIcon.TASKS),
                createStatCard("已提交", String.valueOf(mySubmissions), VaadinIcon.CHECK_CIRCLE),
                createStatCard("待提交", String.valueOf(pendingAssignments), VaadinIcon.CLOCK)
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
        H3 sectionTitle = new H3("所有作業");
        
        assignmentGrid = new Grid<>(Assignment.class, false);
        assignmentGrid.addColumn(Assignment::getTitle).setHeader("作業標題").setSortable(true);
        assignmentGrid.addColumn(assignment -> assignment.getTeacher().getFullName())
            .setHeader("老師").setSortable(true);
        assignmentGrid.addColumn(assignment -> 
            assignment.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        ).setHeader("截止時間").setSortable(true);
        assignmentGrid.addColumn(new ComponentRenderer<>(assignment -> {
            var currentUser = securityUtils.getAuthenticatedUser().orElse(null);
            if (currentUser != null) {
                boolean hasSubmitted = submissionService.hasSubmitted(assignment, currentUser);
                Span status = new Span(hasSubmitted ? "已提交" : "未提交");
                status.getElement().getThemeList().add(
                    hasSubmitted ? "badge success" : "badge error"
                );
                return status;
            }
            return new Span("未知");
        })).setHeader("提交狀態");
        assignmentGrid.addColumn(new ComponentRenderer<>(assignment -> {
            Span status = new Span(assignment.isOverdue() ? "已過期" : "進行中");
            status.getElement().getThemeList().add(
                assignment.isOverdue() ? "badge error" : "badge success"
            );
            return status;
        })).setHeader("作業狀態");
        
        assignmentGrid.addComponentColumn(this::createAssignmentActionButtons)
            .setHeader("操作").setFlexGrow(0);
        
        assignmentGrid.setHeight("300px");
        
        add(sectionTitle, assignmentGrid);
    }
    
    private void createSubmissionSection() {
        H3 sectionTitle = new H3("我的提交記錄");
        
        submissionGrid = new Grid<>(Submission.class, false);
        submissionGrid.addColumn(submission -> submission.getAssignment().getTitle())
            .setHeader("作業標題").setSortable(true);
        submissionGrid.addColumn(submission -> 
            submission.getSubmittedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        ).setHeader("提交時間").setSortable(true);
        submissionGrid.addColumn(new ComponentRenderer<>(submission -> {
            Span status = new Span(submission.isLate() ? "遲交" : "準時");
            status.getElement().getThemeList().add(
                submission.isLate() ? "badge error" : "badge success"
            );
            return status;
        })).setHeader("提交狀態");
        submissionGrid.addColumn(submission -> 
            submission.getGrade() != null ? submission.getGrade().toString() : "未評分"
        ).setHeader("分數").setSortable(true);
        submissionGrid.addColumn(submission -> 
            submission.getFeedback() != null && !submission.getFeedback().isEmpty() ? "有評語" : "無評語"
        ).setHeader("評語");
        
        submissionGrid.addComponentColumn(this::createSubmissionActionButtons)
            .setHeader("操作").setFlexGrow(0);
        
        submissionGrid.setHeight("300px");
        
        add(sectionTitle, submissionGrid);
    }
    
    private HorizontalLayout createAssignmentActionButtons(Assignment assignment) {
        Button viewButton = new Button("查看", VaadinIcon.EYE.create());
        viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        viewButton.addClickListener(e -> viewAssignmentDetails(assignment));
        
        var currentUser = securityUtils.getAuthenticatedUser().orElse(null);
        if (currentUser != null) {
            boolean hasSubmitted = submissionService.hasSubmitted(assignment, currentUser);
            
            Button submitButton = new Button(
                hasSubmitted ? "重新提交" : "提交作業", 
                VaadinIcon.UPLOAD.create()
            );
            submitButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            submitButton.addClickListener(e -> openSubmissionDialog(assignment));
            
            // 如果作業已過期且未提交，禁用提交按鈕
            if (assignment.isOverdue() && !hasSubmitted) {
                submitButton.setEnabled(false);
                submitButton.setText("已過期");
            }
            
            return new HorizontalLayout(viewButton, submitButton);
        }
        
        return new HorizontalLayout(viewButton);
    }
    
    private HorizontalLayout createSubmissionActionButtons(Submission submission) {
        Button viewButton = new Button("查看詳情", VaadinIcon.EYE.create());
        viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        viewButton.addClickListener(e -> viewSubmissionDetails(submission));
        
        return new HorizontalLayout(viewButton);
    }
    
    private void viewAssignmentDetails(Assignment assignment) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("作業詳情");
        dialog.setWidth("600px");
        
        VerticalLayout layout = new VerticalLayout();
        
        layout.add(new H3(assignment.getTitle()));
        layout.add(new Span("老師: " + assignment.getTeacher().getFullName()));
        layout.add(new Span("發布時間: " + assignment.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        layout.add(new Span("截止時間: " + assignment.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        
        if (assignment.getDescription() != null && !assignment.getDescription().isEmpty()) {
            TextArea descriptionArea = new TextArea("作業描述");
            descriptionArea.setValue(assignment.getDescription());
            descriptionArea.setReadOnly(true);
            descriptionArea.setHeight("200px");
            descriptionArea.setWidthFull();
            layout.add(descriptionArea);
        }
        
        // 檢查是否已提交
        var currentUser = securityUtils.getAuthenticatedUser().orElse(null);
        if (currentUser != null) {
            Optional<Submission> submission = submissionService.findByAssignmentAndStudent(assignment, currentUser);
            if (submission.isPresent()) {
                Span submittedInfo = new Span("✓ 您已於 " + 
                    submission.get().getSubmittedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + 
                    " 提交此作業");
                submittedInfo.getStyle().set("color", "var(--lumo-success-color)");
                layout.add(submittedInfo);
                
                if (submission.get().getGrade() != null) {
                    Span gradeInfo = new Span("分數: " + submission.get().getGrade());
                    gradeInfo.getStyle().set("font-weight", "bold");
                    layout.add(gradeInfo);
                }
            }
        }
        
        Button closeButton = new Button("關閉", e -> dialog.close());
        dialog.getFooter().add(closeButton);
        dialog.add(layout);
        dialog.open();
    }
    
    private void openSubmissionDialog(Assignment assignment) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("提交作業: " + assignment.getTitle());
        dialog.setWidth("600px");
        
        VerticalLayout layout = new VerticalLayout();
        
        // 顯示作業信息
        layout.add(new Span("截止時間: " + assignment.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        
        if (assignment.isOverdue()) {
            Span overdueWarning = new Span("⚠️ 此作業已過期");
            overdueWarning.getStyle().set("color", "var(--lumo-error-color)");
            layout.add(overdueWarning);
        }
        
        TextArea contentArea = new TextArea("作業內容");
        contentArea.setPlaceholder("請在此輸入您的作業內容...");
        contentArea.setHeight("300px");
        contentArea.setWidthFull();
        
        // 如果已經提交過，顯示之前的內容
        var currentUser = securityUtils.getAuthenticatedUser().orElse(null);
        if (currentUser != null) {
            Optional<Submission> existingSubmission = submissionService.findByAssignmentAndStudent(assignment, currentUser);
            if (existingSubmission.isPresent()) {
                contentArea.setValue(existingSubmission.get().getContent() != null ? existingSubmission.get().getContent() : "");
                Span resubmitInfo = new Span("您之前已提交過此作業，重新提交將覆蓋之前的內容。");
                resubmitInfo.getStyle().set("color", "var(--lumo-warning-color)");
                layout.add(resubmitInfo);
            }
        }
        
        layout.add(contentArea);
        
        Button submitButton = new Button("提交", e -> {
            if (contentArea.getValue().trim().isEmpty()) {
                Notification.show("請輸入作業內容")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            
            try {
                if (currentUser != null) {
                    submissionService.updateSubmission(assignment, currentUser, contentArea.getValue());
                    refreshData();
                    dialog.close();
                    Notification.show("作業提交成功")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
            } catch (Exception ex) {
                Notification.show("提交失敗: " + ex.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button cancelButton = new Button("取消", e -> dialog.close());
        
        dialog.getFooter().add(cancelButton, submitButton);
        dialog.add(layout);
        dialog.open();
    }
    
    private void viewSubmissionDetails(Submission submission) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("提交詳情");
        dialog.setWidth("600px");
        
        VerticalLayout layout = new VerticalLayout();
        
        layout.add(new H3(submission.getAssignment().getTitle()));
        layout.add(new Span("提交時間: " + submission.getSubmittedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        layout.add(new Span("狀態: " + (submission.isLate() ? "遲交" : "準時")));
        
        if (submission.getGrade() != null) {
            Span gradeSpan = new Span("分數: " + submission.getGrade());
            gradeSpan.getStyle().set("font-weight", "bold").set("font-size", "1.2em");
            layout.add(gradeSpan);
        } else {
            layout.add(new Span("分數: 未評分"));
        }
        
        if (submission.getFeedback() != null && !submission.getFeedback().isEmpty()) {
            TextArea feedbackArea = new TextArea("老師評語");
            feedbackArea.setValue(submission.getFeedback());
            feedbackArea.setReadOnly(true);
            feedbackArea.setHeight("100px");
            feedbackArea.setWidthFull();
            layout.add(feedbackArea);
        }
        
        TextArea contentArea = new TextArea("我的提交內容");
        contentArea.setValue(submission.getContent() != null ? submission.getContent() : "");
        contentArea.setReadOnly(true);
        contentArea.setHeight("200px");
        contentArea.setWidthFull();
        layout.add(contentArea);
        
        Button closeButton = new Button("關閉", e -> dialog.close());
        dialog.getFooter().add(closeButton);
        dialog.add(layout);
        dialog.open();
    }
    
    private void refreshData() {
        List<Assignment> assignments = assignmentService.findAll();
        assignmentGrid.setItems(assignments);
        
        var currentUser = securityUtils.getAuthenticatedUser().orElse(null);
        if (currentUser != null) {
            List<Submission> submissions = submissionService.findByStudent(currentUser);
            submissionGrid.setItems(submissions);
        }
    }
}