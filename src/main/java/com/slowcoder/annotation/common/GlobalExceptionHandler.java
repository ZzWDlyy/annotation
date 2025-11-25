package com.slowcoder.annotation.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1. 处理 @Valid 参数校验失败的异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder sb = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append(fieldError.getDefaultMessage()).append("; ");
        }
        String msg = sb.toString();
        logger.warn("参数校验失败: {}", msg);
        return Result.validateFailed(msg);
    }

    // 2. 处理业务逻辑异常 (比如我们在 Service 里抛出的 IllegalArgumentException)
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("业务异常: {}", e.getMessage());
        return Result.failed(e.getMessage());
    }

    // 3. 处理其他未知异常 (兜底)
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        logger.error("系统内部异常", e);
        return Result.failed("系统繁忙，请稍后重试");
    }

}
