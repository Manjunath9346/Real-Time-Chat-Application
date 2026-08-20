package com.chatapp.chatapp.service;


import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import com.chatapp.chatapp.entity.ChatGroup;
import com.chatapp.chatapp.entity.User;
import com.chatapp.chatapp.repository.ChatGroupRepository;
import com.chatapp.chatapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupService {

    private final ChatGroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupService(
            ChatGroupRepository groupRepository,
            UserRepository userRepository) {

        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // CREATE GROUP
    // =========================================================

    @Transactional
    public ChatGroup createGroup(
            String name,
            Long creatorId) {

        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Group name is required");
        }

        User creator =
                userRepository.findById(creatorId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Creator user not found"
                                ));

        ChatGroup group =
                new ChatGroup(
                        name.trim(),
                        creatorId
                );

        // Creator automatically becomes a member
        group.getMembers().add(creator);

        return groupRepository.save(group);
    }


    // =========================================================
    // GET GROUPS FOR USER
    // =========================================================

    @Transactional(readOnly = true)
    public List<ChatGroup> getUserGroups(Long userId) {

        return groupRepository.findByMembers_Id(userId);
    }


    // =========================================================
    // ADD MEMBER
    // ONLY CREATOR CAN ADD MEMBERS
    // =========================================================

    @Transactional
    public ChatGroup addMember(
            Long groupId,
            Long userId,
            Long requesterId) {

        ChatGroup group =
                groupRepository.findById(groupId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Group not found"
                                ));

        // Only creator can add members
        if (!group.getCreatorId().equals(requesterId)) {

            throw new RuntimeException(
                    "Only the group creator can add members"
            );
        }

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                ));

        // Already a member
        if (group.getMembers().contains(user)) {

            throw new RuntimeException(
                    "User is already a member of this group"
            );
        }

        group.getMembers().add(user);

        return groupRepository.save(group);
    }


    // =========================================================
    // REMOVE MEMBER
    // ONLY CREATOR CAN REMOVE MEMBERS
    // =========================================================

    @Transactional
    public ChatGroup removeMember(
            Long groupId,
            Long userId,
            Long requesterId) {

        ChatGroup group =
                groupRepository.findById(groupId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Group not found"
                                ));

        // Only creator can remove members
        if (!group.getCreatorId().equals(requesterId)) {

            throw new RuntimeException(
                    "Only the group creator can remove members"
            );
        }

        // Creator cannot remove themselves
        if (group.getCreatorId().equals(userId)) {

            throw new RuntimeException(
                    "The group creator cannot be removed"
            );
        }

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                ));

        group.getMembers().remove(user);

        return groupRepository.save(group);
    }


    // =========================================================
    // DELETE GROUP
    // ONLY CREATOR CAN DELETE
    // =========================================================

    @Transactional
    public void deleteGroup(
            Long groupId,
            Long requesterId) {

        ChatGroup group =
                groupRepository.findById(groupId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Group not found"
                                ));

        // SECURITY CHECK
        if (!group.getCreatorId().equals(requesterId)) {

            throw new RuntimeException(
                    "Only the group creator can delete the group"
            );
        }

        /*
         * Remove members first.
         * This prevents orphan rows in group_members.
         */
        group.getMembers().clear();

        groupRepository.save(group);

        groupRepository.delete(group);
    }


    // =========================================================
    // LEAVE GROUP
    // CREATOR CANNOT LEAVE
    // =========================================================

    @Transactional
    public void leaveGroup(
            Long groupId,
            Long userId) {

        ChatGroup group =
                groupRepository.findById(groupId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Group not found"
                                ));

        if (group.getCreatorId().equals(userId)) {

            throw new RuntimeException(
                    "Group creator cannot leave the group. " +
                    "Delete the group instead."
            );
        }

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                ));

        group.getMembers().remove(user);

        groupRepository.save(group);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getGroupInfo(Long groupId) {

        ChatGroup group =
                groupRepository.findById(groupId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Group not found"
                                ));

        List<Map<String, Object>> members =
                group.getMembers()
                        .stream()
                        .map(user -> {

                            Map<String, Object> member =
                                    new HashMap<>();

                            member.put("id", user.getId());
                            member.put(
                                    "username",
                                    user.getUsername()
                            );
                            member.put(
                                    "status",
                                    user.getStatus()
                            );
                            member.put(
                                    "admin",
                                    group.getCreatorId()
                                            .equals(user.getId())
                            );

                            return member;

                        })
                        .collect(Collectors.toList());

        Map<String, Object> result =
                new HashMap<>();

        result.put("id", group.getId());
        result.put("name", group.getName());
        result.put(
                "creatorId",
                group.getCreatorId()
        );
        result.put("members", members);

        return result;
    }
}