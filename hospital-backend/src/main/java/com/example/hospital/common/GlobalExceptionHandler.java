package com.example.hospital.common;

import org.springframework.http.HttpStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ValidationException;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 全局异常处理器
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException e) {
        // 打印错误日志到控制台，方便调试
        System.out.println("捕获业务异常: " + e.getMessage());
        
        // 返回失败的 Result，状态码设为 500 (或者你定义的其他错误码)
        // 这里的 code 是 JSON 里的 code，HTTP 状态码依然是 200
        return Result.error(500, e.getMessage());
    }


    /**
     * 处理所有不可知的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleException(Exception e) {
        e.printStackTrace();
        return Result.error(ResultCode.INTERNAL_SERVER_ERROR, "服务器内部错误: " + e.getMessage());
    }

    /**
     * 处理请求参数格式错误
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return Result.error(ResultCode.PARAM_VALID_ERROR, "参数验证失败", errors);
    }

    /**
     * 处理请求参数格式错误
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : violations) {
            String field = violation.getPropertyPath().toString();
            errors.put(field, violation.getMessage());
        }
        return Result.error(ResultCode.PARAM_VALID_ERROR, "参数验证失败", errors);
    }

    /**
     * 处理参数绑定错误
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleBindException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return Result.error(ResultCode.PARAM_VALID_ERROR, "参数绑定失败", errors);
    }

    /**
     * 处理参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = String.format("参数'%s'类型不匹配，需要'%s'类型",
                e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "");
        return Result.error(ResultCode.PARAM_TYPE_ERROR, message);
    }

    /**
     * 处理未授权异常
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result handleUnauthorizedException(UnauthorizedException e) {
        return Result.error(ResultCode.UNAUTHORIZED, e.getMessage());
    }

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result handleForbiddenException(ForbiddenException e) {
        return Result.error(ResultCode.FORBIDDEN, e.getMessage());
    }

//    /**
//     * 处理业务异常
//     */
//    @ExceptionHandler(BusinessException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public Result handleBusinessException(BusinessException e) {
//        return Result.error(e.getCode(), e.getMessage());
//    }
}
