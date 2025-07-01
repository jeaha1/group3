package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.dto.ChatLogDto;
import com.group3.askmyfriend.entity.AdminEntity;
import com.group3.askmyfriend.entity.InquiryEntity;
import com.group3.askmyfriend.entity.ReportEntity;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.service.AdminService;
import com.group3.askmyfriend.service.ChatLogService;
import com.group3.askmyfriend.service.InquiryService;
import com.group3.askmyfriend.service.ReportService;
import com.group3.askmyfriend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ChatLogService chatLogService;
    private final InquiryService inquiryService;
    private final UserService userService;
    private final ReportService reportService; // 신고 서비스 추가

    // 관리자 로그인 페이지
    @GetMapping("/admin-login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "로그인에 실패했습니다.");
        }
        return "admin_login";
    }

    // 로그인 처리
    @PostMapping("/admin-login")
    public String handleLogin(@RequestParam("admin_id") String adminId,
                              @RequestParam("admin_pw") String password,
                              Model model) {
        try {
            AdminEntity admin = adminService.authenticate(adminId, password);
            return "redirect:/admin/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin_login";
        }
    }

    // 관리자 대시보드
    @GetMapping("/admin/dashboard")
    public String showDashboard() {
        return "admin/dashboard";
    }

    // 회원 목록 보기
    @GetMapping("/admin/members")
    public String showMemberList(Model model) {
        List<UserEntity> users = adminService.getAllUsers();
        model.addAttribute("userList", users);
        return "admin/members";
    }

    // 채팅 로그 보기
    @GetMapping("/admin/chatlog")
    public String showChatLog(Model model) {
        List<ChatLogDto> chatList = chatLogService.getAllLogs();
        model.addAttribute("chatList", chatList);
        return "admin/chatlog";
    }

    // 1:1 문의 목록 보기
    @GetMapping("/admin/inquiries")
    public String showInquiries(Model model) {
        List<InquiryEntity> inquiries = inquiryService.getAllInquiries();
        model.addAttribute("inquiries", inquiries);
        return "admin/inquiries";
    }

    // 1:1 문의 답변 처리
    @PostMapping("/admin/inquiries/{id}/reply")
    public String replyToInquiry(@PathVariable Long id, @RequestParam String reply) {
        inquiryService.replyToInquiry(id, reply);
        return "redirect:/admin/inquiries";
    }

    // 회원 상태 변경 처리
    @PostMapping("/admin/members/status")
    public String updateUserStatus(@RequestParam Long userId,
                                   @RequestParam String status) {
        userService.updateStatus(userId, status);
        return "redirect:/admin/members";
    }

    // 신고 목록 보기 (추가된 메서드)
    @GetMapping("/admin/reports")
    public String showReportList(Model model) {
        List<ReportEntity> reports = reportService.findAll();
        model.addAttribute("reports", reports);
        return "admin/reports";
    }
}
