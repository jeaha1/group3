package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.dto.ChatMessageDTO;
import com.group3.askmyfriend.dto.ChatRoomDTO;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.service.ChatService;
import com.group3.askmyfriend.service.CustomUserDetailsService;
import com.group3.askmyfriend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import com.group3.askmyfriend.dto.GroupChatRequestDTO; // ✅ 이 부분을 추가하세요.

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 채팅 메인 페이지
     */
    @GetMapping
    public String chatPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetailsService.CustomUser user = (CustomUserDetailsService.CustomUser) auth.getPrincipal();
        
        model.addAttribute("currentUser", user.getUsername());
        model.addAttribute("currentUserNickname", user.getNickname());
        
        return "chat/chat-main";
    }

    /**
     * 사용자의 채팅방 목록 조회
     */
    @GetMapping("/api/rooms")
    @ResponseBody
    public ResponseEntity<List<ChatRoomDTO>> getUserChatRooms() {
        Long userId = getCurrentUserId();
        List<ChatRoomDTO> rooms = chatService.getUserChatRooms(userId);
        return ResponseEntity.ok(rooms);
    }

    /**
     * 1:1 채팅방 생성 또는 기존 방 반환
     */
    @PostMapping("/api/rooms/private")
    @ResponseBody
    public ResponseEntity<ChatRoomDTO> createPrivateRoom(@RequestBody Map<String, Long> request) {
        Long currentUserId = getCurrentUserId();
        Long targetUserId = request.get("targetUserId");
        
        ChatRoomDTO room = chatService.createOrGetPrivateRoom(currentUserId, targetUserId);
        return ResponseEntity.ok(room);
    }

    /**
     * 그룹 채팅방 생성
     */
    @PostMapping("/api/rooms/group")
    @ResponseBody
    public ResponseEntity<ChatRoomDTO> createGroupRoom(@RequestBody GroupChatRequestDTO request) {
        Long currentUserId = getCurrentUserId();
        String roomName = request.getRoomName();
        List<Long> participantIds = request.getParticipantIds();
        
        ChatRoomDTO room = chatService.createGroupRoom(roomName, participantIds, currentUserId);
        return ResponseEntity.ok(room);
    }
    /**
     * 채팅방 메시지 목록 조회
     */
    @GetMapping("/api/rooms/{roomId}/messages")
    @ResponseBody
    public ResponseEntity<Page<ChatMessageDTO>> getRoomMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = getCurrentUserId();
        Page<ChatMessageDTO> messages = chatService.getRoomMessages(roomId, page, size, userId);
        return ResponseEntity.ok(messages);
    }

    /**
     * 메시지 읽음 처리
     */
    @PostMapping("/api/rooms/{roomId}/read")
    @ResponseBody
    public ResponseEntity<Void> markAsRead(@PathVariable Long roomId) {
        Long userId = getCurrentUserId();
        chatService.markAsRead(roomId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 채팅방 나가기
     */
    @PostMapping("/api/rooms/{roomId}/leave")
    @ResponseBody
    public ResponseEntity<Void> leaveRoom(@PathVariable Long roomId) {
        Long userId = getCurrentUserId();
        chatService.leaveRoom(roomId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자 검색
     */
    @GetMapping("/api/users/search")
    @ResponseBody
    public ResponseEntity<List<UserEntity>> searchUsers(@RequestParam String keyword) {
        Long currentUserId = getCurrentUserId();
        List<UserEntity> users = chatService.searchUsers(keyword, currentUserId);
        return ResponseEntity.ok(users);
    }

    /**
     * 이미지 파일 업로드
     */
    @PostMapping("/api/upload/image")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("roomId") Long roomId) {
        try {
            Long userId = getCurrentUserId();
            Map<String, Object> response = chatService.uploadFile(file, roomId, userId, "IMAGE");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 파일 업로드
     */
    @PostMapping("/api/upload/file")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("roomId") Long roomId) {
        try {
            Long userId = getCurrentUserId();
            Map<String, Object> response = chatService.uploadFile(file, roomId, userId, "FILE");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 현재 로그인한 사용자 ID 조회
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loginId = auth.getName();
        
        return userRepository.findByLoginId(loginId)
            .map(UserEntity::getUserId)
            .orElseThrow(() -> new RuntimeException("현재 사용자를 찾을 수 없습니다."));
    }
}