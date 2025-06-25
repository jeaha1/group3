package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.ChatParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipantEntity, Long> {

    // 특정 채팅방의 활성화된 참여자들 조회
    List<ChatParticipantEntity> findByChatRoomRoomIdAndIsActiveTrue(Long roomId);

    // 특정 사용자의 특정 채팅방 참여 정보 조회
    Optional<ChatParticipantEntity> findByChatRoomRoomIdAndUserUserIdAndIsActiveTrue(Long roomId, Long userId);

    // 특정 채팅방의 참여자 수 조회
    @Query("SELECT COUNT(cp) FROM ChatParticipantEntity cp " +
           "WHERE cp.chatRoom.roomId = :roomId AND cp.isActive = true")
    Long countActiveParticipantsByRoomId(@Param("roomId") Long roomId);
}