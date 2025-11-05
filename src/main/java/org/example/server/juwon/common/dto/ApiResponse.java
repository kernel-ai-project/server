package org.example.server.juwon.common.dto;

import lombok.Getter;

// 모든 성공 응답은 이 객체로 감싸서 "success: true"를 보장합니다.
@Getter
public class ApiResponse<T> {

    private final boolean success;
    private final T result;

    public ApiResponse(boolean success, T result) {
        this.success = success;
        this.result = result;
    }

    // 성공 응답을 생성하는 정적 메서드
    public static <T> ApiResponse<T> success(T result) {
        return new ApiResponse<>(true, result);
    }
}