package com.csye7125.webapp.webapp.models.tasks;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Document(indexName = "tasksindex")
public class TaskElastic {


    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "TaskElastic{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", summary='" + summary + '\'' +
                ", dueDate=" + dueDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", priority='" + priority + '\'' +
                ", state='" + state + '\'' +
                '}';
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
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

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Id
    private String id;

    @Field(type = FieldType.Text, name = "name")
    private String name;

    @Field(type = FieldType.Text, name = "summary")
    private String summary;

    @Field(type = FieldType.Date, name = "dueDate")
    private Date dueDate;

    @Field(type = FieldType.Date, name = "createdAt")
    private Date createdAt;

    @Field(type = FieldType.Date, name = "updatedAt")
    private Date updatedAt;

    @Field(type = FieldType.Text, name = "priority")
    private String priority;

    @Field(type = FieldType.Text, name = "state")
    private String state;

}
