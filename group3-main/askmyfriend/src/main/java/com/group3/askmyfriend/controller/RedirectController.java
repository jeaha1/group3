package com.group3.askmyfriend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RedirectController {

    // /profile 요청 시 /mypage로 리다이렉트
    @GetMapping("/profile")
    public String redirectToMypage() {
        return "redirect:/mypage";
    }
}
