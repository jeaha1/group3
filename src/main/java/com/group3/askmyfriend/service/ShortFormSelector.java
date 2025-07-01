// src/main/java/com/group3/askmyfriend/service/ShortFormSelector.java
package com.group3.askmyfriend.service;

import com.group3.askmyfriend.entity.PostEntity;
import java.util.List;

public interface ShortFormSelector {
    /** 후보 리스트에서 다음에 보여줄 한 건을 선택 */
    PostEntity selectNext(List<PostEntity> candidates);
}
