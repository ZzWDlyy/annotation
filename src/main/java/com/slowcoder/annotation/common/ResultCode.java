package com.slowcoder.annotation.common;

public enum ResultCode implements IErrorCode{
    // 成功
    SUCCESS(200, "操作成功"),

    // 失败
    FAILED(500, "操作失败"),

    // 参数校验失败
    VALIDATE_FAILED(400, "参数检验失败"),

    // 未登录或token过期
    UNAUTHORIZED(401, "暂未登录或token已经过期"),

    // 没有相关权限
    FORBIDDEN(403, "没有相关权限"),

    // 资源不存在
    NOT_FOUND(404, "资源不存在"),

    // 请求方法不允许
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),

    // 服务器内部错误
    INTERNAL_SERVER_ERROR(500, "服务器内部错误");
    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
