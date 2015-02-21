package org.tn5250j;

import java.net.*;
import java.io.*;
import java.util.Vector;
import org.tn5250j.event.*;

public class BootStrapper extends Thread {

   private Socket socket = null;
   private ServerSocket serverSocket = null;
   boolean listening = true;
   private Vector<BootListener> listeners;
   private BootEvent bootEvent;

   public BootStrapper() {
      super("BootStrapper");
      try {
         serverSocket = new ServerSocket(3036);
      }
      catch (IOException e) {
         System.err.println("Could not listen on port: 3036.");
      }

   }

   public void run() {

      System.out.println("BootStrapper listening");
      while (true) {
         listen();
         getNewSessionOptions();
         System.out.println("got one");
      }

   }

   /**
    * Add a BootListener to the listener list.
    *
    * @param listener  The BootListener to be added
    */
   public synchronized void addBootListener(BootListener listener) {

      if (listeners == null) {
          listeners = new java.util.Vector<BootListener>(3);
      }
      listeners.addElement(listener);

   }

   /**
    * Notify all registered listeners of the BootEvent.
    *
    */
   private void fireBootEvent() {

   	if (listeners != null) {
	      int size = listeners.size();
	      for (int i = 0; i < size; i++) {
	         BootListener target =
                    listeners.elementAt(i);
	         target.bootOptionsReceived(bootEvent);
	      }
   	}
   }

   /**
    * Listen for a connection from another tn5250j session starting.
    */
   private void listen() {

      try {
         socket = serverSocket.accept();
      }
      catch (IOException ioe) {
         System.out.println(this.getName() + ": " + ioe.getMessage());
      }

   }

   /**
    * Retrieve the boot options from the other JVM wanting to start a new
    *    session.
    */
   private void getNewSessionOptions() {

      try {

         BufferedReader in = new BufferedReader(
                              new InputStreamReader(
                              socket.getInputStream()));

         bootEvent = new BootEvent(this,in.readLine());

         System.out.println(bootEvent.getNewSessionOptions());
         fireBootEvent();

         in.close();
         socket.close();

      }
      catch (IOException e) {
         e.printStackTrace();
      }

   }
}
