package org.tn5250j.sessionsettings;
/*
 * Title: PrinterAttributesPanel
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

import org.tn5250j.SessionConfig;
import org.tn5250j.gui.TN5250jFontsSelection;
import org.tn5250j.tools.LangTool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;

class PrinterAttributesPanel extends AttributesPanel {

  private static final long serialVersionUID = 1L;
  private JCheckBox defaultPrinter;
  private Paper pappyPort;
  private Paper pappyLand;
  private TN5250jFontsSelection fs;

  PrinterAttributesPanel(SessionConfig config) {
    super(config, "Printer");
  }

  /**
   * Component initialization
   */
  public void initPanel() throws Exception {


    setLayout(new BorderLayout());
    contentPane = new JPanel();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    add(contentPane, BorderLayout.NORTH);

    // define ppPanel panel
    JPanel ppp = new JPanel();
    ppp.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.print")));
    defaultPrinter = new JCheckBox(LangTool.getString("sa.defaultPrinter"));

    if (getStringProperty("defaultPrinter").equals("Yes"))
      defaultPrinter.setSelected(true);

    ppp.add(defaultPrinter);

    //--- Create a printerJob object
    PrinterJob printJob = PrinterJob.getPrinterJob();

    // will have to remember this for the next time.
    //   Always set a page format before call setPrintable to
    //   set the orientation.
    PageFormat pf = printJob.defaultPage();

    pappyPort = pf.getPaper();

    pappyLand = pf.getPaper();

    // Portrait paper parameters
    if (getStringProperty("print.portWidth").length() != 0 &&
        getStringProperty("print.portHeight").length() != 0 &&
        getStringProperty("print.portImageWidth").length() != 0 &&
        getStringProperty("print.portImageHeight").length() != 0 &&
        getStringProperty("print.portImage.X").length() != 0 &&
        getStringProperty("print.portImage.Y").length() != 0) {

      pappyPort.setSize(Double.parseDouble(getStringProperty("print.portWidth")),
          Double.parseDouble(getStringProperty("print.portHeight")));

      pappyPort.setImageableArea(Double.parseDouble(getStringProperty("print.portImage.X")),
          Double.parseDouble(getStringProperty("print.portImage.Y")),
          Double.parseDouble(getStringProperty("print.portImageWidth")),
          Double.parseDouble(getStringProperty("print.portImageHeight")));
    }

    // Landscape paper parameters
    if (getStringProperty("print.landWidth").length() != 0 &&
        getStringProperty("print.landHeight").length() != 0 &&
        getStringProperty("print.landImageWidth").length() != 0 &&
        getStringProperty("print.landImageHeight").length() != 0 &&
        getStringProperty("print.landImage.X").length() != 0 &&
        getStringProperty("print.landImage.Y").length() != 0) {

      pappyLand.setSize(Double.parseDouble(getStringProperty("print.landWidth")),
          Double.parseDouble(getStringProperty("print.landHeight")));

      pappyLand.setImageableArea(Double.parseDouble(getStringProperty("print.landImage.X")),
          Double.parseDouble(getStringProperty("print.landImage.Y")),
          Double.parseDouble(getStringProperty("print.landImageWidth")),
          Double.parseDouble(getStringProperty("print.landImageHeight")));
    }

    // define page panel
    JPanel page = new JPanel();
    page.setBorder(BorderFactory.createTitledBorder(
        LangTool.getString("sa.pageParameters")));

    page.setLayout(new BorderLayout());
    JButton setPortAttributes = new JButton(LangTool.getString("sa.columns24"));

    setPortAttributes.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getPortraitAttributes();
      }
    });

    JButton setLandAttributes = new JButton(LangTool.getString("sa.columns132"));

    setLandAttributes.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getLandscapeAttributes();
      }
    });

    // now create page dialog holder panel
    JPanel pagePage = new JPanel();

    pagePage.add(setPortAttributes);
    pagePage.add(setLandAttributes);

    page.add(pagePage, BorderLayout.NORTH);

    // now create fonts selection
    JPanel pageFont = new JPanel();
    fs = new TN5250jFontsSelection();

    if (getStringProperty("print.font").length() != 0) {
      fs.setSelectedItem(getStringProperty("print.font"));
    }

    pageFont.add(fs);

    page.add(pageFont, BorderLayout.SOUTH);
    contentPane.add(ppp);
    contentPane.add(page);

  }

  private void getPortraitAttributes() {

    PrinterJob printJob = PrinterJob.getPrinterJob();

    PageFormat documentPageFormat = new PageFormat();
    documentPageFormat.setOrientation(PageFormat.PORTRAIT);
    documentPageFormat.setPaper(pappyPort);

    documentPageFormat = printJob.pageDialog(documentPageFormat);

    pappyPort = documentPageFormat.getPaper();


  }

  private void getLandscapeAttributes() {

    PrinterJob printJob = PrinterJob.getPrinterJob();

    PageFormat documentPageFormat = new PageFormat();
    documentPageFormat.setOrientation(PageFormat.LANDSCAPE);
    documentPageFormat.setPaper(pappyLand);

    documentPageFormat = printJob.pageDialog(documentPageFormat);

    pappyLand = documentPageFormat.getPaper();


  }

  public void applyAttributes() {

    if (defaultPrinter.isSelected()) {
      changes.firePropertyChange(this, "defaultPrinter",
          getStringProperty("defaultPrinter"),
          "Yes");
      setProperty("defaultPrinter", "Yes");
    } else {
      changes.firePropertyChange(this, "defaultPrinter",
          getStringProperty("defaultPrinter"),
          "No");
      setProperty("defaultPrinter", "No");
    }

    // portrait parameters
    changes.firePropertyChange(this, "print.portWidth",
        getStringProperty("print.portWidth"),
        pappyPort.getWidth());
    setProperty("print.portWidth", Double.toString(pappyPort.getWidth()));

    changes.firePropertyChange(this, "print.portImageWidth",
        getStringProperty("print.portImageWidth"),
        pappyPort.getImageableWidth());
    setProperty("print.portImageWidth", Double.toString(pappyPort.getImageableWidth()));

    changes.firePropertyChange(this, "print.portHeight",
        getStringProperty("print.portHeight"),
        pappyPort.getHeight());
    setProperty("print.portHeight", Double.toString(pappyPort.getHeight()));

    changes.firePropertyChange(this, "print.portImageHeight",
        getStringProperty("print.portImageHeight"),
        pappyPort.getImageableHeight());
    setProperty("print.portImageHeight", Double.toString(pappyPort.getImageableHeight()));

    changes.firePropertyChange(this, "print.portImage.X",
        getStringProperty("print.portImage.X"),
        pappyPort.getImageableX());
    setProperty("print.portImage.X", Double.toString(pappyPort.getImageableX()));

    changes.firePropertyChange(this, "print.portImage.Y",
        getStringProperty("print.portImage.Y"),
        pappyPort.getImageableY());
    setProperty("print.portImage.Y", Double.toString(pappyPort.getImageableY()));

    // landscape parameters
    changes.firePropertyChange(this, "print.landWidth",
        getStringProperty("print.landWidth"),
        pappyLand.getWidth());
    setProperty("print.landWidth", Double.toString(pappyLand.getWidth()));

    changes.firePropertyChange(this, "print.landImageWidth",
        getStringProperty("print.landImageWidth"),
        pappyLand.getImageableWidth());
    setProperty("print.landImageWidth", Double.toString(pappyLand.getImageableWidth()));

    changes.firePropertyChange(this, "print.landHeight",
        getStringProperty("print.landHeight"),
        pappyLand.getHeight());
    setProperty("print.landHeight", Double.toString(pappyLand.getHeight()));

    changes.firePropertyChange(this, "print.landImageHeight",
        getStringProperty("print.landImageHeight"),
        pappyLand.getImageableHeight());
    setProperty("print.landImageHeight", Double.toString(pappyLand.getImageableHeight()));

    changes.firePropertyChange(this, "print.landImage.X",
        getStringProperty("print.landImage.X"),
        pappyLand.getImageableX());
    setProperty("print.landImage.X", Double.toString(pappyLand.getImageableX()));

    changes.firePropertyChange(this, "print.landImage.Y",
        getStringProperty("print.landImage.Y"),
        pappyLand.getImageableY());
    setProperty("print.landImage.Y", Double.toString(pappyLand.getImageableY()));


    if (fs.getSelectedItem() != null) {
      changes.firePropertyChange(this, "print.font",
          getStringProperty("print.font"),
          fs.getSelectedItem());
      setProperty("print.font", (String) fs.getSelectedItem());
    }
  }
}
