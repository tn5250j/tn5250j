/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.5
 *
 * Description:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */
package org.tn5250j.tools;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;

import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.gui.TN5250jFileChooser;
import org.tn5250j.gui.TN5250jFileFilter;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

public class SendScreenToFile {

	private static final TN5250jLogger LOG = TN5250jLogFactory
			.getLogger(SendScreenToFile.class);

	/**
	 * @param parent
	 * @param screen
	 */
	public static final void showDialog(Component parent, Screen5250 screen) {
		String workingDir = System.getProperty("user.dir");
		TN5250jFileChooser fileChooser = new TN5250jFileChooser(workingDir);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(new TN5250jFileFilter("txt", "Text files"));

		// int ret = pcFileChooser.showSaveDialog(new JFrame());
		int ret = fileChooser.showSaveDialog(parent);

		// check to see if something was actually chosen
		if (ret == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			final String fname = file.getName();
			if (fname.lastIndexOf('.') < 0) {
				file = new File(file.toString() + ".txt");
			}

			StringBuffer sb = new StringBuffer();
			char[] s = screen.getScreenAsChars();
			int c = screen.getColumns();
			int l = screen.getRows() * c;
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

	private static final void writeToFile(String sc, File file) {

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(sc.getBytes());
			out.flush();
			out.close();

		} catch (FileNotFoundException fnfe) {
			LOG.warn("fnfe: " + fnfe.getMessage());
		} catch (IOException ioe) {
			LOG.warn("ioe: " + ioe.getMessage());
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException exc) {
					LOG.warn("ioe finally: " + exc.getMessage());
				}

		}

	}

}
