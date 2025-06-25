package com.group3.askmyfriend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    // localhost:8080/index로 접속 시 index.html 보여줌
    @GetMapping("/index")
    public String index() {
        return "index"; // templates/index.html
    }

    // localhost:8080 접속 시에도 /index로 리다이렉트
    @GetMapping("/")
    public String homeRedirect() {
        return "redirect:/index";
    }
    @GetMapping("/friends")
    public String friendsPage() {
        return "friends"; // 확장자 생략 → templates/friends.html 사용
    }
}
