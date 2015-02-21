// InterpreterDriver.java
package org.tn5250j.scripting;

import org.tn5250j.SessionPanel;

/**
 * Driver interface for scripting interpreter.
 * Each language supported must implement this interface.
 * The implementation fo this interface will typically delegate
 * the work to the underlying third-party interpreter.
 * The implementing class must create an instance of itself and
 * register it with InterpreterDriverManager when it is loaded.
 * @author Ramnivas Laddad
 */
public interface InterpreterDriver  {
   /**
    * Execute a script string.
    * @param script a string to be interpreted
    * @exception throw a InterpreterDriver.InterpreterException
    *            which wraps the exception throw by underlying
    *            interpreter
    */
   public void executeScript(SessionPanel session,String script)
            throws InterpreterDriver.InterpreterException;

   /**
    * Execute a script file.
    * @param script a name of file to be interpreted
    * @exception throw a InterpreterDriver.InterpreterException
    *            which wraps the exception throw by underlying
    *            interpreter
    */
   public void executeScriptFile(SessionPanel session, String scriptFile)
            throws InterpreterDriver.InterpreterException;

   /**
    * Execute a script file.
    * @param script a name of file to be interpreted
    * @exception throw a InterpreterDriver.InterpreterException
    *            which wraps the exception throw by underlying
    *            interpreter
    */
   public void executeScriptFile(String scriptFile)
            throws InterpreterDriver.InterpreterException;

   /**
    * Get the extension for supported extensions by this driver
    * @return Array of string containing extension supported
    */
   public String[] getSupportedExtensions();

   /**
    * Get the langauges for supported extensions by this driver
    * @return Array of string containing languages supported
    */
   public String[] getSupportedLanguages();

   /**
   * Nested class for wrapping the exception throw by underlying
   * interpreter while executing scripts
   */
   public static class InterpreterException extends Exception {
      private static final long serialVersionUID = 1L;
	private Exception _underlyingException;

   /**
    * Construct a wrapper exception for given undelying exception.
    * @param ex the underlying exception thrown by the interpreter
    */
   public InterpreterException(Exception ex) {
       _underlyingException = ex;
   }

   /**
    * Get a string representation for this object
    * @return string representing the object
    */
   public String toString() {
       return "InterpreterException: underlying exception: "
      + _underlyingException;
   }
    }
}
