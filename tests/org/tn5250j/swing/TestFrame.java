package org.tn5250j.swing;

import org.tn5250j.SessionBean;
import org.tn5250j.interfaces.ConfigureFactory;

import javax.swing.*;
import java.awt.*;

/**
 * For testing purpose
 */
public class TestFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    static {
        ConfigureFactory.getInstance();
        // WVL - LDC : 11/07/2003
        //
        //       LEAVE THIS INITIALIZER IN THIS PLACE SO IT HAPPENS
        //       BEFORE ANY OTHER STATIC INITIALISATION
        org.tn5250j.tools.LangTool.init();
    }

    private SessionBean session;
    private JTerminal terminal;

    public TestFrame() {
        super("Terminal");
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        try {
            String system = "LDCDEV";
            String config = system + ".properties";

            session = new SessionBean(config, system);
            session.setHostName("193.168.51.1");
            session.setCodePage("Cp1141");
            session.connect();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        terminal = new JTerminal(session.getSession());

//    RepaintManager repaintManager = RepaintManager.currentManager(terminal);
//    repaintManager.setDoubleBufferingEnabled(false);
//    terminal.setDebugGraphicsOptions(DebugGraphics.FLASH_OPTION);


        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(terminal);
    }

    public static void main(String[] args) {
        JFrame frm = new TestFrame();
        frm.pack();
        frm.setVisible(true);
    }
}
