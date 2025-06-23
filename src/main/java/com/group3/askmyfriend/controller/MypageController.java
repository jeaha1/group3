package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.dto.MypageDto;
import com.group3.askmyfriend.service.MypageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/mypage")
public class MypageController {

    @Autowired
    private MypageService mypageService;

    // 마이페이지 GET 요청
    @GetMapping
    public String mypage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        MypageDto dto = mypageService.getMypageInfo(principal.getName());
        model.addAttribute("user", dto);
        return "mypage";
    }

    // 프로필(이미지, 닉네임, 자기소개, 공개범위) 수정 POST 요청
    @PostMapping("/updateProfile")
    public String updateProfile(
            @RequestParam(value = "backgroundImg", required = false) MultipartFile backgroundImg,
            @RequestParam(value = "profileImg", required = false) MultipartFile profileImg,
            @RequestParam("username") String nickname,
            @RequestParam("bio") String bio,
            @RequestParam(value = "privacy", required = false) String privacy,
            Principal principal)
    {
    	System.out.println("profileImg: " + (profileImg == null ? "null" : profileImg.getOriginalFilename()));
    	System.out.println("backgroundImg: " + (backgroundImg == null ? "null" : backgroundImg.getOriginalFilename()));


        if (principal == null) {
            return "redirect:/auth/login";
        }
     
        
        mypageService.updateProfile(principal.getName(), backgroundImg, profileImg, nickname, bio, privacy);
        return "redirect:/mypage";
        
    }
    
}

