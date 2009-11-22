package org.tn5250j;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.AlignLayout;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

public class ExternalProgramConfig {
	
	private static TN5250jLogger log =
        TN5250jLogFactory.getLogger("org.tn5250j.ExternalProgramConfig");
	
	private static ExternalProgramConfig etnConfig;
	public static final String EXTERNAL_PROGRAM_REGISTRY_KEY = "etnPgmProps";
	public static final String EXTERNAL_PROGRAM_PROPERTIES_FILE_NAME = "tn5250jExternalProgram.properties";
	public static final String EXTERNAL_PROGRAM_HEADER = "External Program Settings";
	private Properties etnPgmProps;
	
	private static Properties props = null;
	private static JTextField name = null;
	private static JTextField wCommand = null;
	private static JTextField uCommand = null;
	private static JDialog dialog = null;
	private static Object[] options;
	private static String num = "1";
	
	public static ExternalProgramConfig getInstance(){
		if(etnConfig == null){
			etnConfig = new ExternalProgramConfig();
		}
		return etnConfig;
	}
	private  ExternalProgramConfig(){
		etnPgmProps = loadExternalProgramSettings();
	}
	
	public Properties getEtnPgmProps(){
		return this.etnPgmProps;
	}
	private final Properties loadExternalProgramSettings() {
	      Properties etnProps = null;
	      try {
	         etnProps = ConfigureFactory.getInstance().getProperties(
					 					EXTERNAL_PROGRAM_REGISTRY_KEY,
										EXTERNAL_PROGRAM_PROPERTIES_FILE_NAME,false,
	                                    "Default Settings");
			 log.info("begin loading external program settings");   
			 if (etnProps.size() == 0) {
	            Properties defaultProps = new Properties();
	            java.net.URL file=null;
				ClassLoader cl = this.getClass().getClassLoader();				
	            if (cl == null)
	               cl = ClassLoader.getSystemClassLoader();				
	            file = cl.getResource(EXTERNAL_PROGRAM_PROPERTIES_FILE_NAME);
				defaultProps.load(file.openStream());

	            // we will now load the default settings
				for (Enumeration e = defaultProps.keys() ; e.hasMoreElements() ;) {
					 String key = (String)e.nextElement();
					 etnProps.setProperty(key,defaultProps.getProperty(key));

			     }	            
	            ConfigureFactory.getInstance().saveSettings(EXTERNAL_PROGRAM_REGISTRY_KEY,
									EXTERNAL_PROGRAM_PROPERTIES_FILE_NAME,
									EXTERNAL_PROGRAM_HEADER);
	         }
			 
	      }
	      catch (IOException ioe) {
			  log.error(ioe.getMessage());
	      }
	      catch (SecurityException se) {			 
			  log.error(se.getMessage());
	      }
		  
		  return etnProps;
	   }
	
	public static String doEntry(Frame parent, String propKey, Properties props2) {
	     props= props2;
         name = new JTextField(20);		 
         wCommand = new JTextField(40);
         uCommand = new JTextField(40);	         
		 if (propKey != null) {
			  for (Enumeration e = props.keys() ; e.hasMoreElements() ;) {
					 String key = (String)e.nextElement();
					 if(props.getProperty(key) == propKey){
						 String subKey = key.substring(8);
						 int index = subKey.indexOf(".");
						 num = subKey.substring(0,index);
						 name.setText(props.getProperty("etn.pgm."+num+".command.name"));
						 wCommand.setText(props.getProperty("etn.pgm."+num+".command.window"));
						 uCommand.setText(props.getProperty("etn.pgm."+num+".command.unix"));
					 }

			     }	 
	         
	      }

	      //External Program settings panel
	      JPanel etnp = new JPanel();
	      AlignLayout snpLayout = new AlignLayout(2,5,5);
		  etnp.setLayout(snpLayout);
		  etnp.setBorder(BorderFactory.createEtchedBorder());

	      addLabelComponent(LangTool.getString("customized.name"),
	                           name,
	                           etnp);

		  addLabelComponent(LangTool.getString("customized.window"),
				  			   wCommand,
				  			   etnp);
		  addLabelComponent(LangTool.getString("customized.unix"),
	  			   			   uCommand,
	  			   			   etnp);
	      

	      name.setAlignmentX(Component.CENTER_ALIGNMENT);
		  

	      Object[]      message = new Object[1];
	      message[0] = etnp;

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
	         title = LangTool.getString("customized.addEntryTitle");
			 name.setDocument(new SomethingEnteredDocument());
	      }
	      else {
	         Action edit = new AbstractAction(LangTool.getString("conf.optEdit")) {
	            private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
	               doConfigureAction(propKey2);
	            }
	         };
	         options[0] = new JButton(edit);
	         title = LangTool.getString("customized.editEntryTitle");
			 name.setEditable(false);
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

	      return name.getText();

	  }
	
	private static void addLabelComponent(String text,Component comp,Container container) {

	      JLabel label = new JLabel(text);
	      label.setAlignmentX(Component.LEFT_ALIGNMENT);
	      label.setHorizontalTextPosition(JLabel.LEFT);
	      container.add(label);
	      container.add(comp);

	}
	
	/**
	   * React to the configuration action button to perform to Add or Edit the
	   * entry
	   *
	   * @param e - key to act upon
	   */
	   private static void doConfigureAction(String propKey) {

	      if (propKey == null) {
			  String count = props.getProperty("etn.pgm.support.total.num");
			  int maxNum = Integer.parseInt(count)+1;
			  props.setProperty("etn.pgm.support.total.num", String.valueOf(maxNum));
			  props.setProperty("etn.pgm."+maxNum+".command.name", name.getText());
			  props.setProperty("etn.pgm."+maxNum+".command.window", wCommand.getText());
			  props.setProperty("etn.pgm."+maxNum+".command.unix", uCommand.getText());
	      }
	      else {
	         props.setProperty("etn.pgm."+num+".command.name", name.getText());
			 props.setProperty("etn.pgm."+num+".command.window", wCommand.getText());
			 props.setProperty("etn.pgm."+num+".command.unix", uCommand.getText());
	      }
	      dialog.dispose();

	   }
	   
	   private static class SomethingEnteredDocument extends PlainDocument {

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
	   private static void doSomethingEntered() {
		      ((JButton)options[0]).setEnabled(true);
		   }

	   private static void doNothingEntered() {
	      ((JButton)options[0]).setEnabled(false);
	   }
}
