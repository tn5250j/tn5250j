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
package org.tn5250j;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.tn5250j.encoding.CharMappings;
import org.tn5250j.tools.AlignLayout;
import org.tn5250j.tools.LangTool;

public class Configure {

   static Properties props = null;

   // property input structures
   static JTextField systemName = null;
   static JTextField systemId = null;
   static JTextField port = null;
   static JTextField deviceName = null;
   static JTextField  fpn = null;
   static JComboBox  cpb = null;
   static JCheckBox  jtb = null;
   static JCheckBox ec = null;
   static JCheckBox tc = null;
   static JCheckBox sdn = null;
   static JRadioButton sdNormal = null;
   static JCheckBox useProxy = null;
   static JTextField proxyHost = null;
   static JTextField proxyPort = null;
   static JCheckBox noEmbed = null;
   static JCheckBox deamon = null;
   static JCheckBox newJVM = null;
   static JComboBox sslType = null;
   static JCheckBox heartBeat = null;

   static JTabbedPane confTabs;

   static JDialog dialog = null;

   static Object[] options;

   public static String doEntry(Frame parent, String propKey, Properties props2) {

      props = props2;

      confTabs = new JTabbedPane();

      ec = new JCheckBox(LangTool.getString("conf.labelEnhanced"));
      tc = new JCheckBox(LangTool.getString("conf.labelUseSystemName"));
      sdn = new JCheckBox(LangTool.getString("conf.labelUseHostName"));
      useProxy = new JCheckBox(LangTool.getString("conf.labelUseProxy"));
      sdNormal = new JRadioButton(LangTool.getString("conf.label24"));
      JRadioButton sdBig = new JRadioButton(LangTool.getString("conf.label27"));
      noEmbed = new JCheckBox(LangTool.getString("conf.labelEmbed"));
      deamon = new JCheckBox(LangTool.getString("conf.labelDeamon"));
      newJVM = new JCheckBox(LangTool.getString("conf.labelNewJVM"));
      heartBeat = new JCheckBox(LangTool.getString("conf.labelHeartBeat"));

      jtb = new JCheckBox("AS/400 Toolbox");
      jtb.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            doCPStateChanged(e);
         }
      });

      cpb = new JComboBox();

      String[] availCP = getAvailableCodePages();

      cpb.addItem(LangTool.getString("conf.labelDefault"));

      for (int x = 0; x < availCP.length; x++) {
         cpb.addItem(availCP[x]);
      }

      sslType = new JComboBox();

      for (int x = 0; x < TN5250jConstants.SSL_TYPES.length; x++) {
         sslType.addItem(TN5250jConstants.SSL_TYPES[x]);
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
         jtb.setSelected(false);
         sdNormal.setSelected(true);
         deamon.setSelected(true);

         newJVM.setEnabled(false);
         noEmbed.setEnabled(false);
         deamon.setEnabled(false);

         systemName.setDocument(new SomethingEnteredDocument());
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

         if (isSpecified("-sslType",args))
            sslType.setSelectedItem(getParm("-sslType",args));

         if (isSpecified("-sph",args))
            proxyHost = new JTextField(getParm("-sph",args),20);
         else
            proxyHost = new JTextField(20);

         if (isSpecified("-f",args))
            fpn = new JTextField(getParm("-f",args),20);
         else
            fpn = new JTextField(20);
         if (isSpecified("-cp",args)) {
            String codepage = getParm("-cp",args);
            String[] acps = CharMappings.getAvailableCodePages();
            jtb.setSelected(true);
            for (int x = 0; x < acps.length; x++) {

               if (acps[x].equals(codepage))
                  jtb.setSelected(false);

            }
            cpb.setSelectedItem(codepage);

         }

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

         if (isSpecified("-dn=hostname",args)) {
            sdn.setSelected(true);
            deviceName.setEnabled(false);
         }
         else {
            sdn.setSelected(false);
            deviceName.setEnabled(true);
         }

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

         if (isSpecified("-noembed",args))
            noEmbed.setSelected(true);
         else
            noEmbed.setSelected(false);

         if (isSpecified("-d",args))
            deamon.setSelected(true);
         else
            deamon.setSelected(false);

         if (isSpecified("-nc",args))
            newJVM.setSelected(true);
         else
            newJVM.setSelected(false);

         if (isSpecified("-hb",args))
            heartBeat.setSelected(true);
         else
            heartBeat.setSelected(false);

         if (isSpecified("-hb",args))
            heartBeat.setSelected(true);
         else
            heartBeat.setSelected(false);

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

      addLabelComponent(" ",
                           noEmbed,
                           snp);

      addLabelComponent(" ",
                           deamon,
                           snp);

      addLabelComponent(" ",
                           newJVM,
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

      addLabelComponent("",
                           sdn,
                           sip);

      sdn.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            doItemStateChanged(e);
         }
      });

      addLabelComponent(LangTool.getString("conf.labelSSLType"),
                           sslType,
                           sip);

      addLabelComponent("",
                           heartBeat,
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
      BoxLayout cpLayout = new BoxLayout(cp,BoxLayout.X_AXIS);
      cp.setLayout(cpLayout);
      cp.setBorder(BorderFactory.createTitledBorder(
                        LangTool.getString("conf.labelCodePage")));
      cp.add(cpb);
      cp.add(jtb);

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

      if (systemName.getText().trim().length() <= 0) {
         confTabs.setEnabledAt(1,false);
         confTabs.setEnabledAt(2,false);
         confTabs.setEnabledAt(3,false);
      }


      systemName.setAlignmentX(Component.CENTER_ALIGNMENT);
      systemId.setAlignmentX(Component.CENTER_ALIGNMENT);
      fpn.setAlignmentX(Component.CENTER_ALIGNMENT);
      cpb.setAlignmentX(Component.CENTER_ALIGNMENT);


      Object[]      message = new Object[1];
      message[0] = confTabs;

      options = new JButton[2];
      String title;

      final String propKey2 = propKey;

      if (propKey2 == null) {
         Action add = new AbstractAction(LangTool.getString("conf.optAdd")) {
            private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
               doConfigureAction(propKey2);
            }
         };
         options[0] = new JButton(add);
         ((JButton)options[0]).setEnabled(false);
         title = LangTool.getString("conf.addEntryATitle");
      }
      else {
         Action edit = new AbstractAction(LangTool.getString("conf.optEdit")) {
            private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
               doConfigureAction(propKey2);
            }
         };
         options[0] = new JButton(edit);
         title = LangTool.getString("conf.addEntryETitle");
      }

      Action cancel = new AbstractAction(LangTool.getString("conf.optCancel")) {
         private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
            dialog.dispose();
         }
      };
      options[1] = new JButton(cancel);

      JOptionPane             pane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE,
                                                       JOptionPane.DEFAULT_OPTION, null,
                                                       options, options[0]);

      Component parentComponent = parent;
      pane.setInitialValue(options[0]);
      pane.setComponentOrientation(parentComponent.getComponentOrientation());
      dialog = pane.createDialog(parentComponent, title); //, JRootPane.PLAIN_DIALOG);

      dialog.setVisible(true);

      return systemName.getText();

  }


  /**
   * Return the list of available code pages depending on which character
   * mapping flag is set.
   *
   * @return list of available code pages
   *
   */
   private static String[] getAvailableCodePages() {
      return CharMappings.getAvailableCodePages();
   }

  /**
   * React to the configuration action button to perform to Add or Edit the
   * entry
   *
   * @param e - key to act upon
   */
   private static void doConfigureAction(String propKey) {

      if (propKey == null) {
         props.put(systemName.getText(),toArgString());
      }
      else {
         props.setProperty(systemName.getText(),toArgString());
      }
      dialog.dispose();

   }

   /**
    * React on the state change for radio buttons
    *
    * @param e Item event to react to
    */
   private static void doItemStateChanged(ItemEvent e) {

      deviceName.setEnabled(true);

      if (e.getStateChange() == ItemEvent.SELECTED) {
         if (sdn.isSelected()) {
            deviceName.setEnabled(false);
         }
      }
   }

   /**
    * Available Code Page selection state change
    *
    * @param e Item event to react to changes
    */
   private static void doCPStateChanged(ItemEvent e) {

      String[] availCP = getAvailableCodePages();
      cpb.removeAllItems();
      cpb.addItem(LangTool.getString("conf.labelDefault"));

      for (int x = 0; x < availCP.length; x++) {
         cpb.addItem(availCP[x]);
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

   private static void doSomethingEntered() {

      confTabs.setEnabledAt(1,true);
      confTabs.setEnabledAt(2,true);
      confTabs.setEnabledAt(3,true);
      ((JButton)options[0]).setEnabled(true);
      newJVM.setEnabled(true);
      noEmbed.setEnabled(true);
      deamon.setEnabled(true);
   }

   private static void doNothingEntered() {

      confTabs.setEnabledAt(1,false);
      confTabs.setEnabledAt(2,false);
      confTabs.setEnabledAt(3,false);
      ((JButton)options[0]).setEnabled(false);
      newJVM.setEnabled(false);
      noEmbed.setEnabled(false);
      deamon.setEnabled(false);

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
               cpb.getSelectedItem()))
         sb.append(" -cp " + (String)cpb.getSelectedItem());

      if (!TN5250jConstants.SSL_TYPE_NONE.equals(sslType.getSelectedItem()))
         sb.append(" -sslType " + (String)sslType.getSelectedItem());

      if (ec.isSelected())
         sb.append(" -e" );

      if (tc.isSelected())
         sb.append(" -t" );

      if (!sdNormal.isSelected())
         sb.append(" -132" );

      if (deviceName.getText() != null && !sdn.isSelected())
         if (deviceName.getText().trim().length() > 0)
            if (deviceName.getText().trim().length() > 10)
               sb.append(" -dn " + deviceName.getText().trim().substring(0,10).toUpperCase());
            else
               sb.append(" -dn " + deviceName.getText().trim().toUpperCase());

      if (sdn.isSelected())
         sb.append(" -dn=hostname");

      if (useProxy.isSelected())
         sb.append(" -usp" );

      if (proxyHost.getText() != null)
         if (proxyHost.getText().length() > 0)
            sb.append(" -sph " + proxyHost.getText());

      if (proxyPort.getText() != null)
         if (proxyPort.getText().length() > 0)
            sb.append(" -spp " + proxyPort.getText());

      if (noEmbed.isSelected())
         sb.append(" -noembed ");

      if (deamon.isSelected())
         sb.append(" -d ");

      if (newJVM.isSelected())
         sb.append(" -nc ");

      if (heartBeat.isSelected())
         sb.append(" -hb ");

      return sb.toString();
  }

   protected static void parseArgs(String theStringList, String[] s) {
      int x = 0;
      StringTokenizer tokenizer = new StringTokenizer(theStringList, " ");
      while (tokenizer.hasMoreTokens()) {
         s[x++] = tokenizer.nextToken();
      }
   }

   public static class SomethingEnteredDocument extends PlainDocument {

      private static final long serialVersionUID = 1L;

	public void insertString(int offs, String str, AttributeSet a)
                                                   throws BadLocationException {

         super.insertString(offs, str, a);
         if (getText(0, getLength()).length() > 0)
            doSomethingEntered();
      }

      public void remove(int offs, int len) throws BadLocationException {
         super.remove(offs, len);
         if (getText(0, getLength()).length() == 0)
            doNothingEntered();
      }
   }
}
