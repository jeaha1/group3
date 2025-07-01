package com.group3.askmyfriend.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GroupChatRequestDTO {
    private String roomName;
    private List<Long> participantIds;
}