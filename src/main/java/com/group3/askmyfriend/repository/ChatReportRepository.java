// src/main/java/com/group3/askmyfriend/repository/ChatReportRepository.java
package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.ChatReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatReportRepository extends JpaRepository<ChatReport, Long> {
}