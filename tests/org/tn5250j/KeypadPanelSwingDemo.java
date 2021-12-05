/**
 *
 */
package org.tn5250j;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.tn5250j.tools.LangTool;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class KeypadPanelSwingDemo {
    public static void main(final String[] args) throws Exception {
        LangTool.init();

        final SessionBean sessionBean = TestUtils.createSessionBean();
        final KeypadPanelSwing pane = new KeypadPanelSwing(sessionBean.getSession().getConfiguration().getConfig());
        pane.addActionListener(str -> System.out.println(str));

        final JPanel content = new JPanel(new BorderLayout(10, 10));
        content.add(pane, BorderLayout.CENTER);

        //font manipulations
        final JFrame frame = createFrame();
        frame.setSize(frame.getPreferredSize());
        frame.setContentPane(content);
        frame.setVisible(true);
    }

    private static JFrame createFrame() {
        final JFrame frame = new JFrame("KeyPad panel demo");

        //locate frame
        final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        final int w = size.width * 2 / 3;
        final int h = size.height * 2 / 3;
        final int x = (size.width - w) / 2;
        final int y = (size.height - h) / 2;

        frame.setBounds(x, y, w, h);
        return frame;
    }
}
