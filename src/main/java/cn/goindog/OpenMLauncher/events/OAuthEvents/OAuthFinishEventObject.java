package cn.goindog.OpenMLauncher.events.OAuthEvents;

import javax.xml.stream.events.Comment;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashSet;

public class OAuthFinishEventObject extends EventObject {
    private static final long serialVersionUID = 6496098798146410884L;


    public OAuthFinishEventObject(Object source) {
        super(source);
    }
}
