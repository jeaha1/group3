package com.group3.askmyfriend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EmailVerifyController {

    @GetMapping("/email-verify")
    public String showEmailVerifyPage(@RequestParam(required = false) String redirect, Model model) {
        model.addAttribute("redirect", redirect);
        return "email-verify"; // templates/email-verify.html
    }
}
