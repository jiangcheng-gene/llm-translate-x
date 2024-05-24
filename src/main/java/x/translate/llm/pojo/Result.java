package x.translate.llm.pojo;

import java.io.Serializable;

/**
 * @author jiancheng.gene@proton.me 2024 2024/5/22 10:45
 * @since 1.0.0
 */
public class Result<T> implements Serializable {

    public static final int OK_CODE = 200;
    public static final String OK_MSG = "success";
    public static final int ERR_CODE = 500;
    public static final String ERR_MSG = "error";

    private T data;
    private int code;
    private String msg;

    public Result(T data, int code, String msg) {
        this.data = data;
        this.code = code;
        this.msg = msg;
    }

    public static final Result<Object> OK = new Result<>(null, OK_CODE, OK_MSG);
    public static final Result<Object> ERR = new Result<>(null, ERR_CODE, ERR_MSG);

    public static <T> Result<Object> success() {
        return new Result<>(null, OK_CODE, OK_MSG);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(data, OK_CODE, OK_MSG);
    }

    public static <T> Result<T> success(T data, String message) {
        return new Result<>(data, OK_CODE, message);
    }

    public static <T> Result<T> error(T data) {
        return new Result<>(data, ERR_CODE, ERR_MSG);
    }

    public static <T> Result<T> error(T data, String message) {
        return new Result<>(data, ERR_CODE, message);
    }


    public int getCode() {
        return code;
    }

    public T getData() {
        return data;
    }

    public String getMsg() {
        return msg;
    }

    public boolean isOk() {
        return this.code == 200;
    }
}
