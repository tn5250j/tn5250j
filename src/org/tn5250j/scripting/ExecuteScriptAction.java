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
import org.tn5250j.SessionGUI;

public class ExecuteScriptAction extends AbstractAction {
   String _scriptFile;
   SessionGUI ses;

   public ExecuteScriptAction(String name, String scriptFile, SessionGUI session) {
      super(name);
      _scriptFile = scriptFile;
      ses = session;
   }

   public void actionPerformed(ActionEvent e) {
      System.out.println("Invoking " + _scriptFile);

      try {
         InterpreterDriverManager.executeScriptFile(ses,_scriptFile);
      }
      catch (InterpreterDriver.InterpreterException ex) {
         ses.setMacroRunning(false);
         System.out.println(ex);
         ex.printStackTrace();
      }
   }
}
