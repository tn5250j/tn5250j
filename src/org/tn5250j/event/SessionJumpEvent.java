package org.tn5250j.event;

import java.util.EventObject;

public class SessionJumpEvent extends EventObject {

   public SessionJumpEvent(Object obj){
      super(obj);
   }

   public SessionJumpEvent(Object obj, String s) {
      super(obj);
      message = s;
   }

   public String getMessage() {
      return message;
   }

   public void setMessage(String s) {
      message = s;
   }

   public int getJumpDirection() {

      return dir;
   }

   public void setJumpDirection(int d) {

      dir = d;
   }

   private String message;
   private int dir;
}
