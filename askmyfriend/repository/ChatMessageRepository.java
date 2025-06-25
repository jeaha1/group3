package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.ChatMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    // 특정 채팅방의 메시지들을 최신순으로 페이징 조회
    Page<ChatMessageEntity> findByChatRoomRoomIdAndIsDeletedFalseOrderBySentAtDesc(Long roomId, Pageable pageable);

    // 특정 채팅방의 최근 메시지 조회
    @Query("SELECT cm FROM ChatMessageEntity cm " +
           "WHERE cm.chatRoom.roomId = :roomId AND cm.isDeleted = false " +
           "ORDER BY cm.sentAt DESC")
    List<ChatMessageEntity> findRecentMessagesByRoomId(@Param("roomId") Long roomId, Pageable pageable);

    // 특정 시간 이후의 안읽은 메시지 수 조회
    @Query("SELECT COUNT(cm) FROM ChatMessageEntity cm " +
           "WHERE cm.chatRoom.roomId = :roomId AND cm.sentAt > :lastReadAt AND cm.isDeleted = false " +
           "AND cm.sender.userId != :userId")
    Long countUnreadMessages(@Param("roomId") Long roomId, 
                            @Param("lastReadAt") LocalDateTime lastReadAt, 
                            @Param("userId") Long userId);
    //박준형 채팅 로그 메시지 보기 추가
    List<ChatMessageEntity> findAllByIsDeletedFalseOrderBySentAtDesc();

}