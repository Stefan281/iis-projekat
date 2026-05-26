package com.iis.backend.dto;

import com.iis.backend.model.Notification;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private String text;
    private LocalDateTime createdAt;
    private Long authorId;

    public NotificationResponse() {
    }

    public NotificationResponse(Long id, String text, LocalDateTime createdAt, Long authorId) {
        this.id = id;
        this.text = text;
        this.createdAt = createdAt;
        this.authorId = authorId;
    }

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getText(),
                n.getCreatedAt(),
                n.getAuthor() != null ? n.getAuthor().getId() : null
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
}
