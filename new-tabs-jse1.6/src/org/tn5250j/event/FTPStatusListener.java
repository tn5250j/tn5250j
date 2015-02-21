package org.tn5250j.event;

import java.util.EventListener;

public interface FTPStatusListener extends EventListener {

    public abstract void statusReceived(FTPStatusEvent statusevent);
    public abstract void commandStatusReceived(FTPStatusEvent statusevent);
    public abstract void fileInfoReceived(FTPStatusEvent statusevent);

}
