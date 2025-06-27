package com.group3.askmyfriend.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.group3.askmyfriend.dto.PostDto;
import com.group3.askmyfriend.entity.PostEntity;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.service.PostService;
import com.group3.askmyfriend.service.UserService;

@Controller
public class MainController {

    @Autowired
    private PostService postService;
    @Autowired
    private UserService userService;

    // localhost:8080/index로 접속 시 index.html 보여줌
    @GetMapping("/index")
    public String index(Principal principal, Model model) {
    	List<PostDto> posts = postService.findAllPostDtos(Sort.by(Sort.Direction.DESC, "createdAt"));
        System.out.println(principal.getName());
        UserEntity user = userService.findByLoginId(principal.getName()).orElse(null);
        System.out.println(user.getEmail());
        model.addAttribute("posts", posts);
        model.addAttribute("user", user);
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