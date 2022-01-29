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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.gui.TN5250jFileFilterBuilder;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import javafx.stage.FileChooser;
import javafx.stage.Window;

public class SendScreenToFile {

    private static final TN5250jLogger LOG = TN5250jLogFactory
            .getLogger(SendScreenToFile.class);

    /**
     * @param parent
     * @param screen
     */
    public static final void showDialog(final Window parent, final Screen5250 screen) {
        final String workingDir = System.getProperty("user.dir");
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(workingDir));
        fileChooser.setSelectedExtensionFilter(new TN5250jFileFilterBuilder("txt", "Text files").buildFilter());

        File file = fileChooser.showSaveDialog(parent);

        // check to see if something was actually chosen
        if (file != null) {
            final String fname = file.getName();
            if (fname.lastIndexOf('.') < 0) {
                file = new File(file.toString() + ".txt");
            }

            final StringBuffer sb = new StringBuffer();
            final char[] s = screen.getScreenAsChars();
            final int c = screen.getColumns();
            final int l = screen.getRows() * c;
            int col = 0;
            for (int x = 0; x < l; x++, col++) {
                sb.append(s[x]);
                if (col == c) {
                    sb.append('\n');
                    col = 0;
                }
            }

            writeToFile(sb.toString(), file);

        }
    }

    private static final void writeToFile(final String sc, final File file) {

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(sc.getBytes());
            out.flush();
            out.close();

        } catch (final FileNotFoundException fnfe) {
            LOG.warn("fnfe: " + fnfe.getMessage());
        } catch (final IOException ioe) {
            LOG.warn("ioe: " + ioe.getMessage());
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (final IOException exc) {
                    LOG.warn("ioe finally: " + exc.getMessage());
                }

        }

    }

}
