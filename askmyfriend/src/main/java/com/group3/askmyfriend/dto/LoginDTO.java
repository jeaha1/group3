package com.group3.askmyfriend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class LoginDTO {
    private String loginId;
    private String password;
}