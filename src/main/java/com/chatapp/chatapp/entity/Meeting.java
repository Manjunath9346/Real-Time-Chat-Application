package com.chatapp.chatapp.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Long createdBy;

    /*
     * NULL = personal meeting
     * value = group meeting
     */
    private Long groupId;

    @Column(nullable = false, unique = true, length = 8)
    private String meetingCode;

    @Column(nullable = false, length = 20)
    private String status = "UPCOMING";

    @Column(nullable = false)
    private Integer durationMinutes = 60;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    

    public Meeting() {
    }

    @PrePersist
    public void beforeCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = "UPCOMING";
        }

        if (durationMinutes == null || durationMinutes <= 0) {
            durationMinutes = 60;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getMeetingCode() {
        return meetingCode;
    }

    public void setMeetingCode(String meetingCode) {
        this.meetingCode = meetingCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}