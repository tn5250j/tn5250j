package org.tn5250j.event;

import java.util.EventObject;

public class BootEvent extends EventObject {

    private static final long serialVersionUID = 1L;
    private String message;
    private String bootOptions;

    public BootEvent(Object obj) {
        super(obj);

    }

    public BootEvent(Object obj, String s) {
        super(obj);
        bootOptions = s;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String s) {
        message = s;
    }

    public String getNewSessionOptions() {

        return bootOptions;
    }

    public void setNewSessionOptions(String s) {

        bootOptions = s;
    }
}
