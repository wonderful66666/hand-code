package com.withyourhand.ymy.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalidParameter() {
        return new ApiError("INVALID_ARGUMENT", "name 必须为 1–50 个字符，且不能全部为空白。");
    }

    public record ApiError(String code, String message) {
        
    }
}
