package cn.goindog.OpenMLauncher.events.GameEvents;

import java.util.EventObject;

public class DownloadFinishEvent extends EventObject {
    private static final long serialVersionUID = 6496098798146410884L;

    private String type = "";

    public DownloadFinishEvent(Object source, String type) {
        super(source);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
