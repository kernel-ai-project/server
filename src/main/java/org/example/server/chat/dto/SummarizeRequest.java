package org.example.server.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SummarizeRequest {
    private List<ChatMessage> messages;
    private String previousSummary;

}