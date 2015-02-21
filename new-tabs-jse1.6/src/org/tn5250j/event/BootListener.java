package org.tn5250j.event;

import java.util.EventListener;

public interface BootListener extends EventListener {

    public abstract void bootOptionsReceived(BootEvent bootevent);

}
