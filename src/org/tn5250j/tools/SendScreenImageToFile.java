/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.5
 * <p>
 * Description:
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */

package org.tn5250j.tools;

import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;

import org.tn5250j.SessionGui;
import org.tn5250j.gui.TN5250jFileChooser;
import org.tn5250j.tools.encoder.EncodeComponent;
import org.tn5250j.tools.filters.XTFRFileFilter;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

public class SendScreenImageToFile {

    SessionGui session;
    //  Change sent by Luc - LDC to pass a parent frame like the other dialogs
    Frame parent;
    private TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());

    public SendScreenImageToFile(final Frame parent, final SessionGui ses) {

        session = ses;
        this.parent = parent;


        try {
            jbInit();
        } catch (final Exception ex) {
            log.warn("Error in constructor: " + ex.getMessage());
        }
    }

    void jbInit() throws Exception {
        getPCFile();

    }

    /**
     * Get the local file from a file chooser
     */
    private void getPCFile() {

        final String workingDir = System.getProperty("user.dir");
        final TN5250jFileChooser pcFileChooser = new TN5250jFileChooser(workingDir);

        final XTFRFileFilter pngFilter = new XTFRFileFilter("png", "Portable Network Graphics");

        pcFileChooser.setFileFilter(pngFilter);

        final int ret = pcFileChooser.showSaveDialog(parent);

        // check to see if something was actually chosen
        if (ret == JFileChooser.APPROVE_OPTION) {

            File file;

            try {
                if (!pcFileChooser.getSelectedFile().getCanonicalPath().endsWith(".png"))
                    file = new File(pcFileChooser.getSelectedFile().getCanonicalPath()
                            + ".png");
                else
                    file = pcFileChooser.getSelectedFile();


                EncodeComponent.encode(EncodeComponent.PNG, session, file);
            } catch (final Exception e) {
                log.warn("Error generating PNG exception caught: " + e.getMessage());

            }

        }

    }

}
