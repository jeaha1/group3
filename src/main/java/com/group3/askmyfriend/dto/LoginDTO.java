package com.group3.askmyfriend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class LoginDTO {
    private String username;
    private String password;
}