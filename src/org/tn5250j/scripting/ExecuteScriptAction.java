/**
 * ExecuteScriptAction.java
 *
 *
 * Created: Wed Dec 23 15:22:01 1998
 *
 * @author
 * @version
 */

package org.tn5250j.scripting;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class ExecuteScriptAction extends AbstractAction {
   String _scriptFile;
   public ExecuteScriptAction(String name, String scriptFile) {
      super(name);
      _scriptFile = scriptFile;
   }

   public void actionPerformed(ActionEvent e) {
      System.out.println("Invoking " + _scriptFile);

      try {
         InterpreterDriverManager.executeScriptFile(_scriptFile);
      }
      catch (InterpreterDriver.InterpreterException ex) {
         System.out.println(ex);
         ex.printStackTrace();
      }
   }
}
