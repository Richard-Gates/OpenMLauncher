package cn.goindog.OpenMLauncher.events.OAuthEvents;

import java.io.IOException;
import java.util.EventListener;

public interface OAuthFinishEventListener extends EventListener {
    public void OAuthFinishEvent(OAuthFinishEventObject event) throws IOException;
}
