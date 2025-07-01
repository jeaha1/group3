package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/{postId}")
    public String createComment(@PathVariable Long postId,
                                 @RequestParam String content,
                                 @RequestParam String authorEmail) {
        commentService.addComment(postId, content, authorEmail);
        return "redirect:/posts"; // 댓글 작성 후 메인(index.html)으로 리다이렉트
    }
}
