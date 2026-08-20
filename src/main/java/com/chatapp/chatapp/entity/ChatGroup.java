package com.chatapp.chatapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chat_groups")
public class ChatGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long creatorId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private Set<User> members = new HashSet<>();

    public ChatGroup() {
    }

    public ChatGroup(String name, Long creatorId) {
        this.name = name;
        this.creatorId = creatorId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public Set<User> getMembers() {
        return members;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public void setMembers(Set<User> members) {
        this.members = members;
    }
}