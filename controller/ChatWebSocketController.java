package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.dto.ChatMessageDTO;
import com.group3.askmyfriend.service.ChatService;
import com.group3.askmyfriend.repository.UserRepository;
import com.group3.askmyfriend.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.util.HashMap;

@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 메시지 전송 처리
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO messageDTO, 
                           SimpMessageHeaderAccessor headerAccessor,
                           Principal principal) {
        try {
            // 현재 사용자 정보 가져오기
            Long senderId = getSenderIdFromPrincipal(principal);
            
            // 메시지 저장
            ChatMessageDTO savedMessage = chatService.sendMessage(
                messageDTO.getRoomId(), 
                senderId, 
                messageDTO.getContent()
            );
            
            // 채팅방의 모든 참여자에게 메시지 전송
            messagingTemplate.convertAndSend(
                "/topic/room/" + messageDTO.getRoomId(), 
                savedMessage
            );
            
        } catch (Exception e) {
            // 에러 메시지를 발신자에게만 전송
            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/errors",
                "메시지 전송 실패: " + e.getMessage()
            );
        }
    }

    /**
     * 타이핑 상태 전송
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload Map<String, Object> typingData, Principal principal) {
        Long roomId = ((Number) typingData.get("roomId")).longValue();
        Boolean isTyping = (Boolean) typingData.get("isTyping");
        String senderNickname = (String) typingData.get("senderNickname");
        
        // 본인 제외하고 채팅방 참여자들에게 타이핑 상태 전송
        Map<String, Object> response = new HashMap<>();
        response.put("senderNickname", senderNickname);
        response.put("isTyping", isTyping);
        
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/typing", response);
    }

    /**
     * Principal에서 사용자 ID 추출
     */
    private Long getSenderIdFromPrincipal(Principal principal) {
        if (principal != null) {
            String loginId = principal.getName();
            return userRepository.findByLoginId(loginId)
                .map(UserEntity::getUserId)
                .orElse(null);
        }
        return null;
    }
}