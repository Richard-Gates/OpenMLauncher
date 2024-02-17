package cn.goindog.OpenMLauncher.exceptions.GameExceptions;

import cn.goindog.OpenMLauncher.exceptions.CommonException;

public class UnknownGameVersionException extends CommonException {
    private String message;
    public UnknownGameVersionException(String message) {
        super(message);
        this.message = message;
    }
}
