package cn.goindog.OpenMLauncher.exceptions;

public class CommonException extends Exception {
    private String message;
    public CommonException(String message) {
        super(message);
        this.message = message;
    }
}
