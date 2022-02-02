/**
 *
 */
package org.tn5250j.spoolfile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.tn5250j.SessionGuiAdapter;
import org.tn5250j.tools.LangTool;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.RequestNotSupportedException;
import com.ibm.as400.access.SpooledFile;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SpoolExportWizardDemo extends Application {
    public static void main(final String[] args) {
        LangTool.init();

        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final SessionGuiAdapter gui = new SessionGuiAdapter();

        final SpoolExportWizard dialog = new SpoolExportWizard(createSpoolFile(), gui);
        dialog.setVisible(true);
    }

    public static SpooledFile createSpoolFile() {
        final Map<Integer, Object> attributes = new HashMap<>();
        attributes.put(SpooledFile.ATTR_PAGES, 71);
        return createSpoolFile(attributes);
    }
    public static SpooledFile createSpoolFile(final Map<Integer, Object> attributes) {
        final AS400 system = new AS400("LocalhostSys");
        final int number = 7;
        final String jobName = "job-7";
        final String jobUser = "dev";
        final String name = "Developer";
        final String creationDate = "07.11.1917";
        final String jobNumber = "num-7";
        final String jobSysName = "sysJobNum";
        final String createTime = "11:11";

        final SpooledFile file = new SpooledFile(system, name, number, jobName, jobUser,
                jobNumber, jobSysName, creationDate, createTime) {
            private static final long serialVersionUID = 1L;

            @Override
            public Integer getIntegerAttribute(final int attributeID)
                    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, IOException,
                    InterruptedException, RequestNotSupportedException {
                final Object attr = attributes.get(attributeID);
                return attr == null ? null : ((Number) attr).intValue();
            }
        };
        return file ;
    }
}
