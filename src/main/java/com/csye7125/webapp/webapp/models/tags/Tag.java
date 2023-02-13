package com.csye7125.webapp.webapp.models.tags;

import com.csye7125.webapp.webapp.models.tasks.Task;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="Tags")
public class Tag {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @Column(unique=true)
    private String name;
    private Date createdAt;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date updatedAt;

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

    @ManyToMany
    @JoinColumn(
            name = "taskId",
            referencedColumnName = "id"
    )
    private List<Task> tasks;
}
