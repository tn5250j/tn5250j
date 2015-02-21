
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

import java.io.File;

import javax.swing.JOptionPane;

import org.python.core.PyException;
import org.python.util.PythonInterpreter;
import org.tn5250j.GlobalConfigure;
import org.tn5250j.SessionPanel;

public class JPythonInterpreterDriver implements InterpreterDriver {

   private static JPythonInterpreterDriver _instance;

   private PythonInterpreter _interpreter;

   static {

      // the inizialization is being done in the startup program
      //      Properties props = new Properties();
      //      props.setProperty("python.path", ".;jt400.jar");
      //      PythonInterpreter.initialize(System.getProperties(), props,
      //                        new String[] {""});
      System.setProperty("python.cachedir", 
      System.getProperty("user.home") + File.separator + GlobalConfigure.TN5250J_FOLDER + 
      File.separator);
      
      try {
        _instance = new JPythonInterpreterDriver();
      }
      catch (Exception ex) {
          
          
      }
      InterpreterDriverManager.registerDriver(_instance);
   }

   JPythonInterpreterDriver () {

      try {
        _interpreter = new PythonInterpreter();
      }
      catch (Exception ex) {
        
      }
       
   }
   
   public void executeScript(SessionPanel session, String script)
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

   public void executeScriptFile(SessionPanel session, String scriptFile)
                  throws InterpreterDriver.InterpreterException {

      try {
         final SessionPanel s1 = session;
         final String s2 = scriptFile;

         s1.setMacroRunning(true);
         Runnable interpretIt = new Runnable() {
            public void run() {
//               PySystemState.initialize(System.getProperties(),null, new String[] {""},this.getClass().getClassLoader());

               _interpreter = new PythonInterpreter();
               _interpreter.set("_session",s1);
               try {
                  _interpreter.execfile(s2);
               }
               catch (org.python.core.PySyntaxError pse) {
                  JOptionPane.showMessageDialog(s1,pse,"Error in script " + s2,JOptionPane.ERROR_MESSAGE);
               }
               catch (org.python.core.PyException pse) {
                  JOptionPane.showMessageDialog(s1,pse,"Error in script " + s2,JOptionPane.ERROR_MESSAGE);
               }
               finally {
                  s1.setMacroRunning(false);
               }
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
