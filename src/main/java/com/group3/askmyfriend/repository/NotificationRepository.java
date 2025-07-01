package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.Notification;
import com.group3.askmyfriend.entity.NotificationType;
import com.group3.askmyfriend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // 기본 조회 메서드들
    List<Notification> findByReceiverOrderByCreatedAtDesc(UserEntity receiver);
    
    List<Notification> findByReceiverAndIsReadFalseOrderByCreatedAtDesc(UserEntity receiver);
    
    List<Notification> findByReceiverAndIsReadTrueOrderByCreatedAtDesc(UserEntity receiver);
    
    // 개수 조회
    long countByReceiverAndIsReadFalse(UserEntity receiver);
    
    long countByReceiver(UserEntity receiver);
    
    // 타입별 조회
    List<Notification> findByReceiverAndTypeOrderByCreatedAtDesc(UserEntity receiver, NotificationType type);
    
    List<Notification> findByReceiverAndTypeAndIsReadFalseOrderByCreatedAtDesc(UserEntity receiver, NotificationType type);
    
    // 발신자별 조회
    List<Notification> findByReceiverAndSenderOrderByCreatedAtDesc(UserEntity receiver, UserEntity sender);
    
    // 특정 기간 조회
    List<Notification> findByReceiverAndCreatedAtBetweenOrderByCreatedAtDesc(
        UserEntity receiver, LocalDateTime start, LocalDateTime end);
    
    // 읽지 않은 알림 목록
    List<Notification> findByReceiverAndIsReadFalse(UserEntity receiver);
    
    // 특정 대상에 대한 알림 조회
    List<Notification> findByReceiverAndTargetIdOrderByCreatedAtDesc(UserEntity receiver, Long targetId);
    
    // 복합 조건 조회
    List<Notification> findByReceiverAndTypeAndTargetIdOrderByCreatedAtDesc(
        UserEntity receiver, NotificationType type, Long targetId);
    
    // 커스텀 쿼리들
    @Query("SELECT n FROM Notification n WHERE n.receiver = :receiver AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotifications(@Param("receiver") UserEntity receiver);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiver = :receiver AND n.isRead = false")
    long countUnreadNotifications(@Param("receiver") UserEntity receiver);
    
    @Query("SELECT n FROM Notification n WHERE n.receiver = :receiver AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("receiver") UserEntity receiver, @Param("since") LocalDateTime since);
    
    // 페이징을 위한 메서드
    @Query("SELECT n FROM Notification n WHERE n.receiver = :receiver ORDER BY n.createdAt DESC LIMIT :limit OFFSET :offset")
    List<Notification> findNotificationsWithPaging(
        @Param("receiver") UserEntity receiver, 
        @Param("limit") int limit, 
        @Param("offset") int offset);
    
    // 중복 알림 방지를 위한 조회
    Optional<Notification> findByReceiverAndSenderAndTypeAndTargetId(
        UserEntity receiver, UserEntity sender, NotificationType type, Long targetId);
    
    // 오래된 알림 삭제를 위한 조회
    List<Notification> findByCreatedAtBefore(LocalDateTime cutoffDate);
    
    // 특정 사용자의 모든 알림 삭제
    void deleteByReceiver(UserEntity receiver);
    
    // 특정 발신자의 모든 알림 삭제
    void deleteBySender(UserEntity sender);
    
    // 읽은 알림만 삭제
    void deleteByReceiverAndIsReadTrue(UserEntity receiver);
}
