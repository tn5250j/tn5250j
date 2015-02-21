package org.tn5250j.event;

import java.util.EventObject;

public class SessionChangeEvent extends EventObject {

   private static final long serialVersionUID = 1L;
   
   public SessionChangeEvent(Object obj){
      super(obj);

   }

   public SessionChangeEvent(Object obj, String s) {
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
