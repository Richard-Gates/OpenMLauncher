package cn.goindog.OpenMLauncher.events.OAuthEvents;

import java.util.EventObject;

public class OAuthFinishEvent extends EventObject {
    private static final long serialVersionUID = 6496098798146410884L;

    private String type = "";

    public OAuthFinishEvent(Object source, String type) {
        super(source);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
