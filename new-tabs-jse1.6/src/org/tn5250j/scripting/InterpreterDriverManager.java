// InterpreterDriverManager.java
package org.tn5250j.scripting;

import java.util.HashMap;
import java.util.Map;

import org.tn5250j.SessionPanel;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;
/**
 * Class for managing interpreter drivers.
 * This manager is responsible for keeping track of loaded
 * driver. Interpreter drivers are required to register instance
 * of themselves with this manager when they are loaded.
 */
public class InterpreterDriverManager {
	
	private static final transient TN5250jLogger LOG = TN5250jLogFactory.getLogger(InterpreterDriverManager.class);
	
   private static Map<String, InterpreterDriver> _extensionDriverMap = new HashMap<String, InterpreterDriver>();
   private static Map<String, InterpreterDriver> _languageDriverMap = new HashMap<String, InterpreterDriver>();

   private static final char EXTENSION_SEPARATOR = '.';

   /**
    * Private constructor
    * There is no need for instantiating this class as all methods
    * are private. This private constructor is to disallow creating
    * instances of this class.
    */
   private InterpreterDriverManager() {
   }

   /**
    * Register a driver.
    * Interpreter drivers call this method when they are loaded.
    * @param driver the driver to be registered
    */
   public static void registerDriver(InterpreterDriver driver) {
      String[] extensions = driver.getSupportedExtensions();
      for (int size = extensions.length, i = 0; i < size; i++) {
         _extensionDriverMap.put(extensions[i], driver);
      }
      String[] languages = driver.getSupportedLanguages();
      for (int size = languages.length, i = 0; i < size; i++) {
         _languageDriverMap.put(languages[i], driver);
      }
   }

   /**
    * Execute a script string
    * Execute the string supplied according to the langauge specified
    * @param script script to be executed
    * @param language language for interpreting the script string
    */
   public static void executeScript(SessionPanel session, String script, String language)
                        throws InterpreterDriver.InterpreterException {
      InterpreterDriver driver
                  = _languageDriverMap.get(language);
      if (driver == null) {
    	  LOG.warn("No driver installed to handle language "
                      + language);
            return;
      }

      driver.executeScript(session,script);
   }

    /**
     * Exceute a script file.
     * The interpreter driver supporting the language for this file
     * is deduced from file name extension
     * @param scriptFile file name containing script
     */
   public static void executeScriptFile(SessionPanel session,String scriptFile)
                     throws InterpreterDriver.InterpreterException {
      String extension
             = scriptFile.substring(scriptFile
                  .lastIndexOf(EXTENSION_SEPARATOR)+1);
      InterpreterDriver driver
         = _extensionDriverMap.get(extension);
      if (driver == null) {
    	  LOG.warn("No driver installed to handle extension "
                               + extension);
         return;
      }
      driver.executeScriptFile(session,scriptFile);
   }

    /**
     * Exceute a script file.
     * The interpreter driver supporting the language for this file
     * is deduced from file name extension
     * @param scriptFile file name containing script
     */
   public static void executeScriptFile(String scriptFile)
                     throws InterpreterDriver.InterpreterException {
      String extension
             = scriptFile.substring(scriptFile
                  .lastIndexOf(EXTENSION_SEPARATOR)+1);
      InterpreterDriver driver
         = _extensionDriverMap.get(extension);
      if (driver == null) {
    	  LOG.warn("No driver installed to handle extension "
                               + extension);
         return;
      }
      driver.executeScriptFile(scriptFile);
   }


    /**
     * Check if there is a driver that supports the language.
     * @param scriptFile file name containing script
     */
   public static boolean isScriptSupported(String scriptFile) {
      String extension
             = scriptFile.substring(scriptFile
                  .lastIndexOf(EXTENSION_SEPARATOR)+1);

      return _extensionDriverMap.containsKey(extension);
   }

}


