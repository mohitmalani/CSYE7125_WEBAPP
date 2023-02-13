package com.csye7125.webapp.webapp.models.attachments;

import com.csye7125.webapp.webapp.models.tasks.Task;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

//@Entity
//@Table(name = "Attachments")
public class Attachment {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date dateAttached;
    private String name;
    private double sizeMegaBytes;

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
        dateAttached = new Date();
    }
}
