package com.example.hospital.common;

/**
 * 响应码定义
 */
public interface ResultCode {
    // 成功
    int SUCCESS = 200;
    
    // 通用错误码
    int ERROR = 500;
    int PARAM_VALID_ERROR = 400;  // 参数验证错误
    int PARAM_TYPE_ERROR = 40001; // 参数类型错误
    int UNAUTHORIZED = 401;       // 未授权
    int FORBIDDEN = 403;          // 权限不足
    int NOT_FOUND = 404;          // 资源不存在
    
    // 业务错误码
    int BUSINESS_ERROR = 1000;    // 业务异常
    int USER_NOT_EXIST = 1001;    // 用户不存在
    int USER_EXIST = 1002;        // 用户已存在
    int USER_PASSWORD_ERROR = 1003; // 密码错误
    int TOKEN_INVALID = 1004;     // Token无效
    int TOKEN_EXPIRED = 1005;     // Token过期
    int APPOINTMENT_FULL = 1006;  // 预约已满
    int APPOINTMENT_EXISTS = 1007; // 已预约
    
    // 内部服务器错误
    int INTERNAL_SERVER_ERROR = 500; // 服务器内部错误
}
