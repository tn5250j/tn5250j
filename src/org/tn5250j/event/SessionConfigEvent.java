package org.tn5250j.event;

import java.util.EventObject;

public class SessionConfigEvent extends EventObject {

   public SessionConfigEvent(Object obj){
      super(obj);

   }

   public SessionConfigEvent(Object obj, String s) {
      super(obj);
      message = s;
   }

   public String getMessage() {
      return message;
   }

   public void setMessage(String s) {
      message = s;
   }

   public int getState() {

      return state;
   }

   public void setState(int s) {

      state = s;
   }

   private String message;
   private int state;
}
