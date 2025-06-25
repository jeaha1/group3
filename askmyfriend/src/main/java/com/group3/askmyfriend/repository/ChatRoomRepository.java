package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.ChatRoomEntity;
import com.group3.askmyfriend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {

    // 사용자가 참여중인 모든 채팅방 조회
    @Query("SELECT DISTINCT cr FROM ChatRoomEntity cr " +
           "JOIN cr.participants cp " +
           "WHERE cp.user.userId = :userId AND cp.isActive = true AND cr.isActive = true " +
           "ORDER BY cr.lastMessageAt DESC")
    List<ChatRoomEntity> findByUserIdOrderByLastMessageAt(@Param("userId") Long userId);

    // 두 사용자 간의 1:1 채팅방 찾기
    @Query("SELECT cr FROM ChatRoomEntity cr " +
           "WHERE cr.roomType = 'PRIVATE' AND cr.isActive = true " +
           "AND cr.roomId IN (" +
           "    SELECT cp1.chatRoom.roomId FROM ChatParticipantEntity cp1 " +
           "    WHERE cp1.user.userId = :user1Id AND cp1.isActive = true" +
           ") " +
           "AND cr.roomId IN (" +
           "    SELECT cp2.chatRoom.roomId FROM ChatParticipantEntity cp2 " +
           "    WHERE cp2.user.userId = :user2Id AND cp2.isActive = true" +
           ")")
    Optional<ChatRoomEntity> findPrivateRoomBetweenUsers(@Param("user1Id") Long user1Id, 
                                                        @Param("user2Id") Long user2Id);

    // 채팅방 이름으로 그룹 채팅방 검색
    List<ChatRoomEntity> findByRoomNameContainingAndRoomTypeAndIsActiveTrue(String roomName, ChatRoomEntity.RoomType roomType);
}