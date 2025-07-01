package com.group3.askmyfriend.service;

import com.group3.askmyfriend.dto.NotificationDto;
import com.group3.askmyfriend.entity.Notification;
import com.group3.askmyfriend.entity.NotificationType;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.repository.NotificationRepository;
import com.group3.askmyfriend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 알림 생성
     */
    @Transactional
    public void createNotification(Long receiverId,
                                   Long senderId,
                                   NotificationType type,
                                   Long targetId,
                                   String message,
                                   String url) {
        UserEntity receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("수신자 없음: " + receiverId));
        UserEntity sender = senderId != null
                ? userRepository.findById(senderId)
                    .orElseThrow(() -> new IllegalArgumentException("발신자 없음: " + senderId))
                : null;

        Notification noti = Notification.builder()
                .receiver(receiver)
                .sender(sender)
                .type(type)
                .targetId(targetId)
                .message(message)
                .url(url)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();

        notificationRepository.save(noti);
    }

    /**
     * 유저별 알림 목록 조회 (Entity 반환 - Thymeleaf용)
     */
    @Transactional(readOnly = true)
    public List<Notification> getNotifications(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + userId));
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(user);
    }

    /**
     * 유저별 알림 목록 조회 (DTO 반환 - API용)
     */
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationDtos(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + userId));
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(user)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 읽지 않은 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + userId));
        return notificationRepository.findByReceiverAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + userId));
        return notificationRepository.countByReceiverAndIsReadFalse(user);
    }

    /**
     * 단일 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림 없음: " + notificationId));
        
        if (!notification.isRead()) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    /**
     * 모든 알림 읽음 처리
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + userId));
        
        List<Notification> unreadNotifications = notificationRepository.findByReceiverAndIsReadFalse(user);
        if (!unreadNotifications.isEmpty()) {
            unreadNotifications.forEach(n -> n.setIsRead(true));
            notificationRepository.saveAll(unreadNotifications);
        }
    }

    /**
     * 알림 삭제
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new IllegalArgumentException("알림 없음: " + notificationId);
        }
        notificationRepository.deleteById(notificationId);
    }

    /**
     * 사용자의 모든 알림 삭제
     */
    @Transactional
    public void deleteAllNotifications(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + userId));
        notificationRepository.deleteByReceiver(user);
    }

    /**
     * 읽은 알림만 삭제
     */
    @Transactional
    public void deleteReadNotifications(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + userId));
        notificationRepository.deleteByReceiverAndIsReadTrue(user);
    }

    /**
     * Entity를 DTO로 변환하는 헬퍼 메서드
     */
    private NotificationDto convertToDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .url(notification.getUrl())
                .createdAt(notification.getCreatedAt())
                .isRead(notification.isRead())
                .targetId(notification.getTargetId())
                .receiverId(notification.getReceiver().getUserId())
                .receiverNickname(notification.getReceiver().getNickname())
                .senderId(notification.getSender() != null ? notification.getSender().getUserId() : null)
                .senderNickname(notification.getSender() != null ? notification.getSender().getNickname() : null)
                .senderProfileImg(notification.getSender() != null ? notification.getSender().getProfileImg() : null)
                .build();
    }

    /**
     * 중복 알림 방지를 위한 체크
     */
    @Transactional(readOnly = true)
    public boolean isDuplicateNotification(Long receiverId, Long senderId, NotificationType type, Long targetId) {
        UserEntity receiver = userRepository.findById(receiverId).orElse(null);
        UserEntity sender = senderId != null ? userRepository.findById(senderId).orElse(null) : null;
        
        if (receiver == null) return false;
        
        return notificationRepository.findByReceiverAndSenderAndTypeAndTargetId(receiver, sender, type, targetId)
                .isPresent();
    }

    /**
     * 중복되지 않는 알림만 생성
     */
    @Transactional
    public void createNotificationIfNotExists(Long receiverId,
                                              Long senderId,
                                              NotificationType type,
                                              Long targetId,
                                              String message,
                                              String url) {
        if (!isDuplicateNotification(receiverId, senderId, type, targetId)) {
            createNotification(receiverId, senderId, type, targetId, message, url);
        }
    }
}
