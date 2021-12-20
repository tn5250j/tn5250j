/**
 * ExecuteScriptAction.java
 * <p>
 * <p>
 * Created: Wed Dec 23 15:22:01 1998
 *
 * @author
 * @version
 */

package org.tn5250j.scripting;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.tn5250j.SessionGui;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

public class ExecuteScriptAction extends AbstractAction {

    private static final long serialVersionUID = 181938308216785668L;
    private static final transient TN5250jLogger LOG = TN5250jLogFactory.getLogger(ExecuteScriptAction.class);

    private String _scriptFile;
    private SessionGui ses;

    public ExecuteScriptAction(final String name, final String scriptFile, final SessionGui session) {
        super(name);
        _scriptFile = scriptFile;
        ses = session;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Invoking " + _scriptFile);
        }

        try {
            InterpreterDriverManager.executeScriptFile(ses, _scriptFile);
        } catch (final InterpreterDriver.InterpreterException ex) {
            ses.setMacroRunning(false);
            System.out.println(ex);
            ex.printStackTrace();
        }
    }
}
