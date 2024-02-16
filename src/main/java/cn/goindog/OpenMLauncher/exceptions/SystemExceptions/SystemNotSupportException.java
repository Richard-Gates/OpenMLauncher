package cn.goindog.OpenMLauncher.exceptions.SystemExceptions;

import cn.goindog.OpenMLauncher.exceptions.CommonException;

public class SystemNotSupportException extends CommonException {
    private String message;
    public SystemNotSupportException(String message) {
        super(message);
    }
}
