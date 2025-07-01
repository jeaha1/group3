// src/main/java/com/group3/askmyfriend/service/RandomShortFormSelector.java
package com.group3.askmyfriend.service;

import com.group3.askmyfriend.entity.PostEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;

@Service("randomShortFormSelector")
public class RandomShortFormSelector implements ShortFormSelector {
    private final Random rnd = new Random();

    @Override
    public PostEntity selectNext(List<PostEntity> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        return candidates.get(rnd.nextInt(candidates.size()));
    }
}
