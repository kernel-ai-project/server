package org.example.server.juwon.common.exception;

import jakarta.persistence.EntityNotFoundException;
import org.example.server.juwon.common.dto.ApiResponse;
import org.example.server.juwon.common.exception.ErrorResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 엔티티를 찾지 못한 경우 (404 Not Found)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResult>> handleEntityNotFound(EntityNotFoundException e) {
        ErrorResult errorResult = new ErrorResult(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, errorResult));
    }

    // 기타 모든 서버 내부 오류 (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResult>> handleGlobalException(Exception e) {
        ErrorResult errorResult = new ErrorResult("서버 내부 오류가 발생했습니다: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, errorResult));
    }


}
