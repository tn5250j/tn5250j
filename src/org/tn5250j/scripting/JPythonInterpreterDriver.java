
/**
 * JPythonInterpreterDriver.java
 *
 *
 * Created: Wed Dec 23 16:03:41 1998
 *
 * @author
 * @version
 */

package org.tn5250j.scripting;


import java.io.*;

import org.python.util.PythonInterpreter;
import org.python.core.*;
import org.tn5250j.Session;
import javax.swing.SwingUtilities;

public class JPythonInterpreterDriver implements InterpreterDriver {

   private static JPythonInterpreterDriver _instance;

   private PythonInterpreter _interpreter = new PythonInterpreter();

   static {
      _instance = new JPythonInterpreterDriver();
      InterpreterDriverManager.registerDriver(_instance);
   }

   public void executeScript(Session session, String script)
         throws InterpreterDriver.InterpreterException {
      try {
         session.setMacroRunning(true);
         _interpreter.set("_session",session);
         _interpreter.exec(script);
         session.setMacroRunning(false);
      }
      catch (PyException ex) {
         throw new InterpreterDriver.InterpreterException(ex);
      }
   }

   public void executeScript(String script)
         throws InterpreterDriver.InterpreterException {
      try {
         _interpreter = new PythonInterpreter();
         _interpreter.exec(script);
      }
      catch (PyException ex) {
         throw new InterpreterDriver.InterpreterException(ex);
      }
   }

   public void executeScriptFile(Session session, String scriptFile)
                  throws InterpreterDriver.InterpreterException {

      try {
         final Session s1 = session;
         final String s2 = scriptFile;

         s1.setMacroRunning(true);
         Runnable interpretIt = new Runnable() {
            public void run() {
               _interpreter = new PythonInterpreter();
               _interpreter.set("_session",s1);
               _interpreter.execfile(s2);
            }

           };

         // lets start interpreting it.
         Thread interpThread = new Thread(interpretIt);
         interpThread.setDaemon(true);
         interpThread.start();

      }
      catch (PyException ex) {
         throw new InterpreterDriver.InterpreterException(ex);
      }
      catch (Exception ex2) {
         throw new InterpreterDriver.InterpreterException(ex2);
      }
   }

   public void executeScriptFile(String scriptFile)
                  throws InterpreterDriver.InterpreterException {

      try {
         _interpreter.execfile(scriptFile);
      }
      catch (PyException ex) {
         throw new InterpreterDriver.InterpreterException(ex);
      }
   }

    public String[] getSupportedExtensions() {
   return new String[]{"py"};
    }

    public String[] getSupportedLanguages() {
   return new String[]{"Python", "JPython"};
    }

    public static void main(String[] args) {
   try {
       _instance.executeScript("print \"Hello\"");
       _instance.executeScriptFile("test.py");
   } catch (Exception ex) {
       System.out.println(ex);
   }
    }
}
