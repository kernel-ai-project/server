package org.example.server.chat.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {

    private boolean success;

    private Result<T> result;

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, Result.success(message));
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, Result.success(message, data));
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, Result.error(message));
    }
}


