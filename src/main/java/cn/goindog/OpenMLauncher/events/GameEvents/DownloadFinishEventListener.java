package cn.goindog.OpenMLauncher.events.GameEvents;

import java.util.EventListener;

public interface DownloadFinishEventListener extends EventListener {

    void DownloadFinishEvent(DownloadFinishEvent event);
}
