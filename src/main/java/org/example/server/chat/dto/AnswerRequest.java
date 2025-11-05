package org.example.server.chat.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnswerRequest {
    private String question;
    private List<ChatMessage> history;
    private String summary;

    public AnswerRequest(String question) {
        this.question = question;
        this.history = null;
        this.summary = null;
    }
}
