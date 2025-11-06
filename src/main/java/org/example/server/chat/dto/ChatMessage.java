package org.example.server.chat.dto;


import lombok.*;


// 히스토리 관리를 위해서 저장할 메세지의 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String role;
    private String content;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class HistoryMessageDTO{
        private String content;
        private Boolean isUser;
    }
}
