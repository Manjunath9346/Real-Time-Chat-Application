package com.chatapp.chatapp.repository;

import com.chatapp.chatapp.entity.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {

    List<ChatGroup> findByMembers_Id(Long userId);
}