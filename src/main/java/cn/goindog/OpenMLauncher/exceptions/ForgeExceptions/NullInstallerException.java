package cn.goindog.OpenMLauncher.exceptions.ForgeExceptions;

import cn.goindog.OpenMLauncher.exceptions.CommonException;

public class NullInstallerException extends CommonException {
    private String message;
    public NullInstallerException(String message) {
        super(message);
        this.message = message;
    }
}
