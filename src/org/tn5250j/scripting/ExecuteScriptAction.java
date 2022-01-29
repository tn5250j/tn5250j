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

import org.tn5250j.SessionGui;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class ExecuteScriptAction implements EventHandler<ActionEvent> {

    private static final transient TN5250jLogger LOG = TN5250jLogFactory.getLogger(ExecuteScriptAction.class);

    private String scriptFile;
    private SessionGui ses;
    private final String name;

    public ExecuteScriptAction(final String name, final String scriptFile, final SessionGui session) {
        this.name = name;
        this.scriptFile = scriptFile;
        ses = session;
    }

    public String getName() {
        return name;
    }

    @Override
    public void handle(final ActionEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Invoking " + scriptFile);
        }

        try {
            InterpreterDriverManager.executeScriptFile(ses, scriptFile);
        } catch (final InterpreterDriver.InterpreterException ex) {
            ses.setMacroRunning(false);
            System.out.println(ex);
            ex.printStackTrace();
        }
    }
}
