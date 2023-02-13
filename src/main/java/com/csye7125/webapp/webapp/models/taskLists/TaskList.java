package com.csye7125.webapp.webapp.models.taskLists;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "Lists")
public class TaskList {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue
    private Long id;
    private String name;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date createdAt;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date updatedAt;

    public TaskList(String name) {
        this.name = name;
    }

    public TaskList() {

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    private UUID userId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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
}
