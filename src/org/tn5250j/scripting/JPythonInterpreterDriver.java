
/**
 * JPythonInterpreterDriver.java
 * <p>
 * <p>
 * Created: Wed Dec 23 16:03:41 1998
 *
 * @author
 * @version
 */

package org.tn5250j.scripting;

import java.io.File;

import org.python.core.PyException;
import org.python.util.PythonInterpreter;
import org.tn5250j.GlobalConfigure;
import org.tn5250j.SessionGui;
import org.tn5250j.gui.UiUtils;

import javafx.scene.Node;

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
        } catch (final Exception ex) {


        }
        InterpreterDriverManager.registerDriver(_instance);
    }

    JPythonInterpreterDriver() {

        try {
            _interpreter = new PythonInterpreter();
        } catch (final Exception ex) {

        }

    }

    @Override
    public void executeScript(final SessionGui session, final String script)
            throws InterpreterDriver.InterpreterException {
        try {
            session.setMacroRunning(true);
            _interpreter.set("_session", session);
            _interpreter.exec(script);
            session.setMacroRunning(false);
        } catch (final PyException ex) {
            throw new InterpreterDriver.InterpreterException(ex);
        }
    }

    public void executeScript(final String script)
            throws InterpreterDriver.InterpreterException {
        try {
            _interpreter = new PythonInterpreter();
            _interpreter.exec(script);
        } catch (final PyException ex) {
            throw new InterpreterDriver.InterpreterException(ex);
        }
    }

    @Override
    public void executeScriptFile(final SessionGui session, final String scriptFile)
            throws InterpreterDriver.InterpreterException {

        try {
            final String s2 = scriptFile;

            session.setMacroRunning(true);
            final Runnable interpretIt = new Runnable() {
                @Override
                public void run() {
//               PySystemState.initialize(System.getProperties(),null, new String[] {""},this.getClass().getClassLoader());

                    _interpreter = new PythonInterpreter();
                    _interpreter.set("_session", session);
                    try {
                        _interpreter.execfile(s2);
                    } catch (final PyException pse) {
                        UiUtils.showError((Node) session, pse, "Error in script " + s2);
                    } finally {
                        session.setMacroRunning(false);
                    }
                }

            };

            // lets start interpreting it.
            final Thread interpThread = new Thread(interpretIt);
            interpThread.setDaemon(true);
            interpThread.start();

        } catch (final PyException ex) {
            throw new InterpreterDriver.InterpreterException(ex);
        } catch (final Exception ex2) {
            throw new InterpreterDriver.InterpreterException(ex2);
        }
    }

    @Override
    public void executeScriptFile(final String scriptFile)
            throws InterpreterDriver.InterpreterException {

        try {
            _interpreter.execfile(scriptFile);
        } catch (final PyException ex) {
            throw new InterpreterDriver.InterpreterException(ex);
        }
    }

    @Override
    public String[] getSupportedExtensions() {
        return new String[]{"py"};
    }

    @Override
    public String[] getSupportedLanguages() {
        return new String[]{"Python", "JPython"};
    }

    public static void main(final String[] args) {
        try {
            _instance.executeScript("print \"Hello\"");
            _instance.executeScriptFile("test.py");
        } catch (final Exception ex) {
            System.out.println(ex);
        }
    }
}
