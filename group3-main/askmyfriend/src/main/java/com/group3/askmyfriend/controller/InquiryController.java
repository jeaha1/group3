// InquiryController.java
package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.entity.InquiryEntity;
import com.group3.askmyfriend.service.InquiryService;
import com.group3.askmyfriend.service.CustomUserDetailsService.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Controller
@RequestMapping("/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    // 문의 작성 폼
    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("inquiry", new InquiryEntity());
        return "inquiry/form";  // templates/inquiry/form.html
    }

    // 문의 제출 처리
    @PostMapping("/form")
    public String submitForm(@ModelAttribute InquiryEntity inquiry,
                             @AuthenticationPrincipal CustomUser user) {
        inquiry.setUserId(user.getId());
        inquiryService.submitInquiry(inquiry);
        return "redirect:/inquiry/list";
    }

    // 사용자 문의 목록 보기
    @GetMapping("/list")
    public String listMyInquiries(Model model,
                                  @AuthenticationPrincipal CustomUser user) {
        model.addAttribute("inquiries", inquiryService.getUserInquiries(user.getId()));
        return "inquiry/list";  // templates/inquiry/list.html
    }
}
