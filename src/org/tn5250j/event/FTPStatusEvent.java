package org.tn5250j.event;

import java.util.EventObject;

public class FTPStatusEvent extends EventObject {

   public FTPStatusEvent(Object obj){
      super(obj);

   }

   public FTPStatusEvent(Object obj, String s) {
      super(obj);
      message = s;
   }

   public String getMessage() {
      return message;
   }

   public void setMessage(String s) {
      message = s;
   }

   public int getFileLength() {

      return fileLength;
   }
   public void setFileLength(int len) {

      fileLength = len;
   }

   public int getCurrentRecord() {

      return currentRecord;
   }

   public void setCurrentRecord(int current) {

      currentRecord = current;
   }
   private String message;
   private int fileLength;
   private int currentRecord;
}
