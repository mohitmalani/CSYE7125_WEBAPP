package com.csye7125.webapp.webapp.models.reminders;

import com.csye7125.webapp.webapp.models.tasks.Task;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "Reminders")
public class Reminder {
    public OffsetDateTime getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(OffsetDateTime reminderDate) {
        this.reminderDate = reminderDate;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue
    private Long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private OffsetDateTime reminderDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private OffsetDateTime createdAt;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private OffsetDateTime updatedAt;
    @ManyToOne
    @JoinColumn(
            name = "taskId",
            referencedColumnName = "id"
    )
    private Task task;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }


    private UUID userId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
