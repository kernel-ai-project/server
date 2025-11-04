package org.example.server.chat.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Result<T> {
    private String message;
    private T data;

    public static <T> Result<T> success(String message) {
        return Result.<T>builder()
                .message(message)
                .build();
    }

    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
                .message(message)
                .data(data)
                .build();
    }

    public static <T> Result<T> error(String message) {
        return Result.<T>builder()
                .message(message)
                .build();
    }
}
