package org.example.server.juwon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GreetingResponseDto
{
    private String message;
    private String greeting;
}
