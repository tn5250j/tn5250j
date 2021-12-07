/**
 *
 */
package org.tn5250j;

import org.tn5250j.tools.LangTool;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class KeypadPanelDemo extends Application {

    public static void main(final String[] args) throws Exception {
        LangTool.init();
        launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        final SessionBean sessionBean = DevTools.createSessionBean();
        final KeypadPanel pane = new KeypadPanel(sessionBean.getSession().getConfiguration().getConfig());
        pane.addActionListener(str -> System.out.println(str));

        //font manipulations
        stage.setScene(new Scene(pane));
        stage.show();
    }
}
