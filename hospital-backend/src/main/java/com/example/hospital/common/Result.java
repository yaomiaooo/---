package com.example.hospital.common;

// import lombok.Data; // 删除这行，不依赖 Lombok 了

public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    // --- 下面是手动添加的 Getter 和 Setter 方法 (这是解决报错的关键) ---

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    // --- 静态构建方法保持不变 ---

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.code = 500;
        r.message = msg;
        return r;
    }
    
    public static <T> Result<T> error(int code, String msg) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = msg;
        return r;
    }
    
    public static <T> Result<T> error(int code, String msg, T data) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = msg;
        r.data = data;
        return r;
    }
}