package org.tn5250j;
/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.4
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

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import org.tn5250j.tools.*;

public class Configure implements TN5250jConstants {

   static Properties props = null;

   // property input structures
   static JTextField systemName = null;
   static JTextField systemId = null;
   static JTextField port = null;
   static JTextField deviceName = null;
   static JTextField  fpn = null;
   static JComboBox  cpb = null;
   static JCheckBox ec = null;
   static JCheckBox tc = null;
   static JRadioButton sdNormal = null;
   static JCheckBox useProxy = null;
   static JTextField proxyHost = null;
   static JTextField proxyPort = null;

   static JTabbedPane confTabs;

   public static void doEntry(Frame parent, String propKey, Properties props) {

      confTabs = new JTabbedPane();

      ec = new JCheckBox(LangTool.getString("conf.labelEnhanced"));
      tc = new JCheckBox(LangTool.getString("conf.labelUseSystemName"));
      useProxy = new JCheckBox(LangTool.getString("conf.labelUseProxy"));
      sdNormal = new JRadioButton(LangTool.getString("conf.label24"));
      JRadioButton sdBig = new JRadioButton(LangTool.getString("conf.label27"));

      cpb = new JComboBox();
      String[] availCP = CharMappings.getAvailableCodePages();
      cpb.addItem(LangTool.getString("conf.labelDefault"));

      for (int x = 0; x < availCP.length; x++) {
         cpb.addItem(availCP[x]);
      }

      if (propKey == null) {
         systemName = new JTextField(20);
         systemId = new JTextField(20);
         port = new JTextField("23",5);
         deviceName = new JTextField(20);
         fpn = new JTextField(20);
         proxyHost = new JTextField(20);
         proxyPort = new JTextField("1080",5);

         ec.setSelected(true);
         tc.setSelected(true);
         sdNormal.setSelected(true);

      }
      else {

         String[] args = new String[20];
         parseArgs((String)props.get(propKey),args);
         systemName = new JTextField(propKey,20);
         systemName.setEditable(false);
         systemName.setEnabled(false);

         systemId = new JTextField(args[0],20);

         if (isSpecified("-p",args)) {
            port = new JTextField(getParm("-p",args),5);
         }
         else {
            port = new JTextField("23",5);
         }

         if (isSpecified("-sph",args))
            proxyHost = new JTextField(getParm("-sph",args),20);
         else
            proxyHost = new JTextField(20);

         if (isSpecified("-f",args))
            fpn = new JTextField(getParm("-f",args),20);
         else
            fpn = new JTextField(20);
         if (isSpecified("-cp",args))
            cpb.setSelectedItem(getParm("-cp",args));
         if (isSpecified("-e",args))
            ec.setSelected(true);
         else
            ec.setSelected(false);
         if (isSpecified("-t",args))
            tc.setSelected(true);
         else
            tc.setSelected(false);

         if (isSpecified("-132",args))
            sdBig.setSelected(true);
         else
            sdNormal.setSelected(true);

         if (isSpecified("-dn",args))
            deviceName = new JTextField(getParm("-dn",args),20);
         else
            deviceName = new JTextField(20);

         if (isSpecified("-spp",args)) {
            proxyPort = new JTextField(getParm("-spp",args),5);
         }
         else {
            proxyPort = new JTextField("1080",5);
         }

         if (isSpecified("-usp",args))
            useProxy.setSelected(true);
         else
            useProxy.setSelected(false);
      }

      //Create main attributes panel
      JPanel mp = new JPanel();
      BoxLayout mpLayout = new BoxLayout(mp,BoxLayout.Y_AXIS);

      mp.setLayout(mpLayout);

      //System Name panel
      JPanel snp = new JPanel();
      AlignLayout snpLayout = new AlignLayout(2,5,5);
      snp.setLayout(snpLayout);
      snp.setBorder(BorderFactory.createEtchedBorder());

      addLabelComponent(LangTool.getString("conf.labelSystemName"),
                           systemName,
                           snp);

      //System Id panel
      JPanel sip = new JPanel();

      AlignLayout al = new AlignLayout(2,5,5);

      sip.setLayout(al);
      sip.setBorder(BorderFactory.createTitledBorder(
                        LangTool.getString("conf.labelSystemIdTitle")));


      addLabelComponent(LangTool.getString("conf.labelSystemId"),
                           systemId,
                           sip);

      addLabelComponent(LangTool.getString("conf.labelPort"),
                           port,
                           sip);

      addLabelComponent(LangTool.getString("conf.labelDeviceName"),
                           deviceName,
                           sip);

      // options panel
      JPanel op = new JPanel();
      BoxLayout opLayout = new BoxLayout(op,BoxLayout.Y_AXIS);
      op.setLayout(opLayout);
      op.setBorder(BorderFactory.createTitledBorder(
                        LangTool.getString("conf.labelOptionsTitle")));

      // file name panel
      JPanel fp = new JPanel();
      BoxLayout fpLayout = new BoxLayout(fp,BoxLayout.Y_AXIS);
      fp.setLayout(fpLayout);
      fp.setBorder(BorderFactory.createTitledBorder(
                        LangTool.getString("conf.labelConfFile")));

      fp.add(fpn);

      // screen dimensions panel
      JPanel sdp = new JPanel();
      BoxLayout sdpLayout = new BoxLayout(sdp,BoxLayout.X_AXIS);
      sdp.setLayout(sdpLayout);
      sdp.setBorder(BorderFactory.createTitledBorder(
                        LangTool.getString("conf.labelDimensions")));

      // Group the radio buttons.
      ButtonGroup sdGroup = new ButtonGroup();
      sdGroup.add(sdNormal);
      sdGroup.add(sdBig);

      sdp.add(sdNormal);
      sdp.add(sdBig);

      // code page panel
      JPanel cp = new JPanel();
      BoxLayout cpLayout = new BoxLayout(cp,BoxLayout.Y_AXIS);
      cp.setLayout(cpLayout);
      cp.setBorder(BorderFactory.createTitledBorder(
                        LangTool.getString("conf.labelCodePage")));
      cp.add(cpb);

      // emulation mode panel
      JPanel ep = new JPanel();
      BoxLayout epLayout = new BoxLayout(ep,BoxLayout.X_AXIS);
      ep.setLayout(epLayout);
      ep.setBorder(BorderFactory.createTitledBorder(
                        LangTool.getString("conf.labelEmulateMode")));

      ep.add(ec);

      // title to be use panel
      JPanel tp = new JPanel();
      BoxLayout tpLayout = new BoxLayout(tp,BoxLayout.X_AXIS);
      tp.setLayout(tpLayout);
      tp.setBorder(BorderFactory.createTitledBorder(
                        ""));

      addLabelComponent("",
                           tc,
                           tp);

      // add all options to Options panel
      op.add(fp);
      op.add(sdp);
      op.add(cp);
      op.add(ep);
      op.add(tp);

      //System Id panel
      JPanel sprox = new JPanel();

      AlignLayout spal = new AlignLayout(2,5,5);

      sprox.setLayout(spal);
      sprox.setBorder(BorderFactory.createEtchedBorder());


      addLabelComponent("",
                           useProxy,
                           sprox);

      addLabelComponent(LangTool.getString("conf.labelProxyHost"),
                           proxyHost,
                           sprox);

      addLabelComponent(LangTool.getString("conf.labelProxyPort"),
                           proxyPort,
                           sprox);

      confTabs.addTab(LangTool.getString("conf.tabGeneral"),snp);
      confTabs.addTab(LangTool.getString("conf.tabTCP"),sip);
      confTabs.addTab(LangTool.getString("conf.tabOptions"),op);
      confTabs.addTab(LangTool.getString("conf.tabProxy"),sprox);

      systemName.setAlignmentX(Component.CENTER_ALIGNMENT);
      systemId.setAlignmentX(Component.CENTER_ALIGNMENT);
      fpn.setAlignmentX(Component.CENTER_ALIGNMENT);
      cpb.setAlignmentX(Component.CENTER_ALIGNMENT);

      Object[]      message = new Object[1];
      message[0] = confTabs;

      String[] options = new String[2];
      String title;

      if (propKey == null) {
         options[0] = LangTool.getString("conf.optAdd");
         title = LangTool.getString("conf.addEntryATitle");
      }
      else {
         options[0] = LangTool.getString("conf.optEdit");
         title = LangTool.getString("conf.addEntryETitle");
      }
      options[1] = LangTool.getString("conf.optCancel");

      int result = JOptionPane.showOptionDialog(
             parent,                              // the parent that the dialog blocks
             message,                           // the dialog message array
             title,                             // the title of the dialog window
             JOptionPane.DEFAULT_OPTION,        // option type
             JOptionPane.PLAIN_MESSAGE,   // message type
             null,                              // optional icon, use null to use the default icon
             options,                           // options string array, will be made into buttons//
             options[0]                         // option that should be made into a default button
         );

      if (result == 0) {
         if (propKey == null) {
            props.put(systemName.getText(),toArgString());
         }
         else {
            props.setProperty(systemName.getText(),toArgString());
         }
      }

  }

   private static void addLabelComponent(String text,Component comp,Container container) {

      JLabel label = new JLabel(text);
      label.setAlignmentX(Component.LEFT_ALIGNMENT);
      label.setHorizontalTextPosition(JLabel.LEFT);
      container.add(label);
      container.add(comp);

   }

   static protected String getParm(String parm, String[] args) {

      for (int x = 0; x < args.length; x++) {

         if (args[x].equals(parm))
            return args[x+1];

      }
      return null;
   }

   static protected boolean isSpecified(String parm, String[] args) {

      for (int x = 0; x < args.length; x++) {

         if (args[x] != null && args[x].equals(parm))
            return true;

      }
      return false;
   }

  private static String toArgString() {

      StringBuffer sb = new StringBuffer();
      sb.append(systemId.getText());

      // port
      if (port.getText() != null)
         if (port.getText().trim().length() > 0)
            sb.append(" -p " + port.getText().trim());

      if (fpn.getText() != null)
         if (fpn.getText().length() > 0)
            sb.append(" -f " + fpn.getText());
      if (!LangTool.getString("conf.labelDefault").equals(
               (String)cpb.getSelectedItem()))
         sb.append(" -cp " + (String)cpb.getSelectedItem());
      if (ec.isSelected())
         sb.append(" -e" );

      if (tc.isSelected())
         sb.append(" -t" );

      if (!sdNormal.isSelected())
         sb.append(" -132" );

      if (deviceName.getText() != null)
         if (deviceName.getText().trim().length() > 0)
            if (deviceName.getText().trim().length() > 10)
               sb.append(" -dn " + deviceName.getText().trim().substring(0,10).toUpperCase());
            else
               sb.append(" -dn " + deviceName.getText().trim().toUpperCase());

      if (useProxy.isSelected())
         sb.append(" -usp" );

      if (proxyHost.getText() != null)
         if (proxyHost.getText().length() > 0)
            sb.append(" -sph " + proxyHost.getText());

      if (proxyPort.getText() != null)
         if (proxyPort.getText().length() > 0)
            sb.append(" -spp " + proxyPort.getText());

      return sb.toString();
  }

   protected static void parseArgs(String theStringList, String[] s) {
      int x = 0;
      StringTokenizer tokenizer = new StringTokenizer(theStringList, " ");
      while (tokenizer.hasMoreTokens()) {
         s[x++] = tokenizer.nextToken();
      }
   }

}
