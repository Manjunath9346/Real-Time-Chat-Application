package com.chatapp.chatapp.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "contacts",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "contact_user_id"}
                )
        }
)
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "contact_user_id", nullable = false)
    private Long contactUserId;

    private String nickname;

    @Column(nullable = false)
    private boolean blocked = false;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Contact() {
    }

    public Contact(
            Long userId,
            Long contactUserId,
            String nickname
    ) {
        this.userId = userId;
        this.contactUserId = contactUserId;
        this.nickname = nickname;
        this.blocked = false;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void beforeCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = "PENDING";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getContactUserId() {
        return contactUserId;
    }

    public void setContactUserId(Long contactUserId) {
        this.contactUserId = contactUserId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}