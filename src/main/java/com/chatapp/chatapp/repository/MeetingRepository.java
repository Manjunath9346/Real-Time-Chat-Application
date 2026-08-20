package com.chatapp.chatapp.repository;

import com.chatapp.chatapp.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MeetingRepository
        extends JpaRepository<Meeting, Long> {

    List<Meeting> findByCreatedByOrderByDateTimeAsc(
            Long createdBy
    );

    List<Meeting> findByGroupIdOrderByDateTimeAsc(
            Long groupId
    );

    List<Meeting> findByGroupIdInOrderByDateTimeAsc(
            List<Long> groupIds
    );

    Optional<Meeting> findByMeetingCode(
            String meetingCode
    );

    @Query("""
            SELECT m
            FROM Meeting m
            WHERE m.createdBy = :userId
               OR m.groupId IN :groupIds
            ORDER BY m.dateTime ASC
            """)
    List<Meeting> findUserMeetings(
            Long userId,
            List<Long> groupIds
    );
}