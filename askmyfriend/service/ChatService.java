package com.group3.askmyfriend.service;

import com.group3.askmyfriend.dto.ChatMessageDTO;
import com.group3.askmyfriend.dto.ChatRoomDTO;
import com.group3.askmyfriend.entity.*;
import com.group3.askmyfriend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 사용자의 모든 채팅방 목록 조회
     */
    public List<ChatRoomDTO> getUserChatRooms(Long userId) {
        List<ChatRoomEntity> rooms = chatRoomRepository.findByUserIdOrderByLastMessageAt(userId);
        
        return rooms.stream().map(room -> {
            ChatRoomDTO dto = new ChatRoomDTO(room);
            
            // 마지막 메시지 설정
            List<ChatMessageEntity> recentMessages = chatMessageRepository
                .findRecentMessagesByRoomId(room.getRoomId(), PageRequest.of(0, 1));
            if (!recentMessages.isEmpty()) {
                dto.setLastMessage(new ChatMessageDTO(recentMessages.get(0)));
            }
            
            // 안읽은 메시지 수 설정
            Optional<ChatParticipantEntity> participant = chatParticipantRepository
                .findByChatRoomRoomIdAndUserUserIdAndIsActiveTrue(room.getRoomId(), userId);
            if (participant.isPresent()) {
                Long unreadCount = chatMessageRepository.countUnreadMessages(
                    room.getRoomId(), 
                    participant.get().getLastReadAt(), 
                    userId
                );
                dto.setUnreadCount(unreadCount);
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 1:1 채팅방 생성 또는 기존 방 반환
     */
    public ChatRoomDTO createOrGetPrivateRoom(Long user1Id, Long user2Id) {
        // 기존 1:1 채팅방 확인
        Optional<ChatRoomEntity> existingRoom = chatRoomRepository
            .findPrivateRoomBetweenUsers(user1Id, user2Id);
        
        if (existingRoom.isPresent()) {
            return new ChatRoomDTO(existingRoom.get());
        }
        
        // 새 1:1 채팅방 생성
        ChatRoomEntity newRoom = ChatRoomEntity.createPrivateRoom();
        chatRoomRepository.save(newRoom);
        
        // 참여자 추가
        UserEntity user1 = userRepository.findById(user1Id)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + user1Id));
        UserEntity user2 = userRepository.findById(user2Id)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + user2Id));
        
        addParticipantToRoom(newRoom, user1);
        addParticipantToRoom(newRoom, user2);
        
        return new ChatRoomDTO(newRoom);
    }

    /**
     * 그룹 채팅방 생성
     */
    public ChatRoomDTO createGroupRoom(String roomName, List<Long> participantIds, Long creatorId) {
        ChatRoomEntity newRoom = ChatRoomEntity.createGroupRoom(roomName);
        chatRoomRepository.save(newRoom);
        
        // 방장 먼저 추가
        UserEntity creator = userRepository.findById(creatorId)
            .orElseThrow(() -> new RuntimeException("방장을 찾을 수 없습니다: " + creatorId));
        addParticipantToRoom(newRoom, creator);
        
        // 다른 참여자들 추가
        for (Long participantId : participantIds) {
            if (!participantId.equals(creatorId)) {
                UserEntity participant = userRepository.findById(participantId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + participantId));
                addParticipantToRoom(newRoom, participant);
            }
        }
        
        return new ChatRoomDTO(newRoom);
    }

    /**
     * 채팅방에 참여자 추가
     */
    private void addParticipantToRoom(ChatRoomEntity room, UserEntity user) {
        ChatParticipantEntity participant = new ChatParticipantEntity();
        participant.setChatRoom(room);
        participant.setUser(user);
        chatParticipantRepository.save(participant);
    }

    /**
     * 메시지 전송
     */
    public ChatMessageDTO sendMessage(Long roomId, Long senderId, String content) {
        ChatRoomEntity room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다: " + roomId));
            
        UserEntity sender = userRepository.findById(senderId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + senderId));
        
        // 사용자가 해당 방의 참여자인지 확인
        boolean isParticipant = chatParticipantRepository
            .findByChatRoomRoomIdAndUserUserIdAndIsActiveTrue(roomId, senderId)
            .isPresent();
            
        if (!isParticipant) {
            throw new RuntimeException("채팅방 참여자가 아닙니다.");
        }
        
        // 메시지 저장
        ChatMessageEntity message = new ChatMessageEntity();
        message.setChatRoom(room);
        message.setSender(sender);
        message.setContent(content);
        message.setMessageType(ChatMessageEntity.MessageType.TEXT);
        
        chatMessageRepository.save(message);
        
        // 채팅방 마지막 메시지 시간 업데이트
        room.setLastMessageAt(LocalDateTime.now());
        chatRoomRepository.save(room);
        
        return new ChatMessageDTO(message);
    }

    /**
     * 채팅방 메시지 목록 조회 (페이징)
     */
    public Page<ChatMessageDTO> getRoomMessages(Long roomId, int page, int size, Long userId) {
        // 사용자가 해당 방의 참여자인지 확인
        boolean isParticipant = chatParticipantRepository
            .findByChatRoomRoomIdAndUserUserIdAndIsActiveTrue(roomId, userId)
            .isPresent();
            
        if (!isParticipant) {
            throw new RuntimeException("채팅방 참여자가 아닙니다.");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessageEntity> messages = chatMessageRepository
            .findByChatRoomRoomIdAndIsDeletedFalseOrderBySentAtDesc(roomId, pageable);
            
        return messages.map(ChatMessageDTO::new);
    }

    /**
     * 메시지 읽음 처리
     */
    public void markAsRead(Long roomId, Long userId) {
        Optional<ChatParticipantEntity> participant = chatParticipantRepository
            .findByChatRoomRoomIdAndUserUserIdAndIsActiveTrue(roomId, userId);
            
        if (participant.isPresent()) {
            participant.get().setLastReadAt(LocalDateTime.now());
            chatParticipantRepository.save(participant.get());
        }
    }

    /**
     * 채팅방 나가기
     */
    public void leaveRoom(Long roomId, Long userId) {
        Optional<ChatParticipantEntity> participant = chatParticipantRepository
            .findByChatRoomRoomIdAndUserUserIdAndIsActiveTrue(roomId, userId);
            
        if (participant.isPresent()) {
            participant.get().setIsActive(false);
            chatParticipantRepository.save(participant.get());
            
            // 1:1 채팅방이고 상대방도 나간 경우 방 비활성화
            Long activeParticipants = chatParticipantRepository
                .countActiveParticipantsByRoomId(roomId);
                
            if (activeParticipants == 0) {
                ChatRoomEntity room = chatRoomRepository.findById(roomId).orElse(null);
                if (room != null) {
                    room.setIsActive(false);
                    chatRoomRepository.save(room);
                }
            }
        }
    }

    /**
     * 사용자 검색 (채팅 상대 찾기용)
     */
    public List<UserEntity> searchUsers(String keyword, Long currentUserId) {
        // 현재 사용자 제외하고 닉네임으로 검색
        return userRepository.findAll().stream()
            .filter(user -> !user.getUserId().equals(currentUserId))
            .filter(user -> user.getNickname().toLowerCase().contains(keyword.toLowerCase()))
            .filter(user -> "ACTIVE".equals(user.getStatus()))
            .limit(10)
            .collect(Collectors.toList());
    }

    /**
     * 파일 업로드 처리
     */
    public Map<String, Object> uploadFile(MultipartFile file, Long roomId, Long userId, String fileType) throws IOException {
        // 파일 유효성 검사
        validateFile(file, fileType);
        
        // 사용자가 해당 방의 참여자인지 확인
        boolean isParticipant = chatParticipantRepository
            .findByChatRoomRoomIdAndUserUserIdAndIsActiveTrue(roomId, userId)
            .isPresent();
            
        if (!isParticipant) {
            throw new RuntimeException("채팅방 참여자가 아닙니다.");
        }
        
        // 파일 저장
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String savedFileName = UUID.randomUUID().toString() + fileExtension;
        
        Path uploadDir = Paths.get("src/main/resources/static/uploads/chat");
        Files.createDirectories(uploadDir);
        
        Path filePath = uploadDir.resolve(savedFileName);
        Files.copy(file.getInputStream(), filePath);
        
        // 메시지 생성
        ChatRoomEntity room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
        UserEntity sender = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        ChatMessageEntity message = new ChatMessageEntity();
        message.setChatRoom(room);
        message.setSender(sender);
        message.setContent(originalFileName); // 원본 파일명을 content로 설정
        message.setMessageType("IMAGE".equals(fileType) ? 
            ChatMessageEntity.MessageType.IMAGE : ChatMessageEntity.MessageType.FILE);
        message.setFileName(originalFileName);
        message.setFilePath("/uploads/chat/" + savedFileName);
        message.setFileSize(file.getSize());
        
        chatMessageRepository.save(message);
        
        // 채팅방 마지막 메시지 시간 업데이트
        room.setLastMessageAt(LocalDateTime.now());
        chatRoomRepository.save(room);
        
        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", new ChatMessageDTO(message));
        response.put("filePath", message.getFilePath());
        
        return response;
    }
    
    /**
     * 파일 유효성 검사
     */
    private void validateFile(MultipartFile file, String fileType) {
        if (file.isEmpty()) {
            throw new RuntimeException("파일이 비어있습니다.");
        }
        
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new RuntimeException("파일명이 유효하지 않습니다.");
        }
        
        String extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
        long fileSize = file.getSize();
        
        if ("IMAGE".equals(fileType)) {
            // 이미지 파일 검증
            Set<String> allowedImageTypes = Set.of("jpg", "jpeg", "png", "gif", "webp");
            if (!allowedImageTypes.contains(extension)) {
                throw new RuntimeException("지원하지 않는 이미지 형식입니다. (jpg, jpeg, png, gif, webp만 지원)");
            }
            if (fileSize > 10 * 1024 * 1024) { // 10MB
                throw new RuntimeException("이미지 파일 크기는 10MB를 초과할 수 없습니다.");
            }
        } else {
            // 일반 파일 검증
            if (fileSize > 50 * 1024 * 1024) { // 50MB
                throw new RuntimeException("파일 크기는 50MB를 초과할 수 없습니다.");
            }
        }
    }
}