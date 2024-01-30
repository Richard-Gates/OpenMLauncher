package cn.goindog.OpenMLauncher.events.OAuthEvents;

import java.io.IOException;
import java.util.EventListener;

public interface OAuthFinishEventListener extends EventListener {
    void OAuthFinishEvent(OAuthFinishEvent event);
}
