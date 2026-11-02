package com.withyourhand.ymy.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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


    // JSON能正常转换成请求对象，
    // 但对象不符合 @NotBlank, @Size 等规则时，处理这个异常。
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalidRequestBody(
        MethodArgumentNotValidException exception
    ) {
        //取得第一个字段校验错误。
        //当前请求对象只有content一个字段
        FieldError fieldError =
            exception.getBindingResult().getFieldError();

        //getDefaultMessage, 可以取得我门注解中的message
        String message = fieldError == null ? "请求参数不合法" : fieldError.getDefaultMessage();
        return new ApiError("INVALID_ARGUMENT",message);
    }

    // 请求正文无法读取时，处理这个异常
    // 例如： JSON缺少括号，请求正文为空等
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError unreadableRequestBody() {
        return new ApiError(
            "INVALID_ARGUMENT",
            "请求体不能为空，且必须是可读取的JSON对象。"
        );
    }

    public record ApiError(String code, String message) {}
}
