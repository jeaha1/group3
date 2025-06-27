package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/likes")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping("/{postId}")
    public ResponseEntity<Integer> toggleLike(@PathVariable Long postId,
                                              @RequestParam String userEmail) {
        int likeCount = likeService.toggleLike(postId, userEmail);
        return ResponseEntity.ok(likeCount);
    }
}
