package org.example.server.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class ChatResponse {

    @Getter
    @Setter
    @Builder
    public static class SaveChatQuestionAndAnswerDTO{
        Long chatQuestionId;
        Long chatAnswerId;
        Long chatRoomId;
    }

}
