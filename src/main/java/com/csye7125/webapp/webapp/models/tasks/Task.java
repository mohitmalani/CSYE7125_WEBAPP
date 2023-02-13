package com.csye7125.webapp.webapp.models.tasks;

import com.csye7125.webapp.webapp.models.comments.Comment;
import com.csye7125.webapp.webapp.models.reminders.Reminder;
import com.csye7125.webapp.webapp.models.tags.Tag;
import com.csye7125.webapp.webapp.models.taskLists.TaskList;
//import org.springframework.data.elasticsearch.annotations.Document;
//import org.springframework.data.elasticsearch.annotations.Field;
//import org.springframework.data.elasticsearch.annotations.FieldType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="tasks")
@Document(indexName = "taskIndex")
public class Task {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue
    private Long id;

    public Task() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    public TaskState getTaskState() {
        return taskState;
    }

    public void setTaskState(TaskState taskState) {
        this.taskState = taskState;
    }

    private String summary;

    private TaskState taskState = TaskState.TODO;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public OffsetDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(OffsetDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public TaskList getTaskList() {
        return taskList;
    }

    public void setTaskList(TaskList taskList) {
        this.taskList = taskList;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Reminder> getReminders() {
        return reminders;
    }

    public void setReminders(List<Reminder> reminders) {
        this.reminders = reminders;
    }

    @Column(nullable = false)
    private String task;

    @Column(nullable = false)
    @Field(type = FieldType.Date, includeInParent = true)
    private OffsetDateTime dueDate;

    @Column(nullable = false)
    @Field(type = FieldType.Nested, includeInParent = true)
    private TaskPriority priority;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date createdAt;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date updatedAt;

    @ManyToOne
    @JoinColumn(
            name = "listId",
            referencedColumnName = "id"
    )
    @Field(type = FieldType.Nested, includeInParent = true)
    private TaskList taskList;

//    @OneToMany(fetch = FetchType.LAZY, mappedBy = "task")
//    private List<Attachment> attachments;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "tasks")
    @Field(type = FieldType.Nested, includeInParent = true)
    private List<Tag> tags;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "task")
    @Field(type = FieldType.Nested, includeInParent = true)
    private List<Comment> comments;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "task")
    @Field(type = FieldType.Nested, includeInParent = true)
    private List<Reminder> reminders;


    private UUID userId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
