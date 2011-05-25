import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.tn5250j.Session5250;
import org.tn5250j.SessionBean;
import org.tn5250j.SessionConfig;
import org.tn5250j.SessionGUI;
import org.tn5250j.interfaces.ConfigureFactory;


public class ExampleEmbeddedMinimalBootstrap {

	public static void main(String[] args) {

		try {
			System.setProperty("emulator.settingsDirectory", File.createTempFile("tn5250j", "settings").getAbsolutePath());  
			ConfigureFactory.getInstance();
			org.tn5250j.tools.LangTool.init();
			final SessionBean sb = createSessionbean();
			
			JFrame frame = new JFrame("TN5250j"); 
			frame.setSize(1024, 768);
			frame.addWindowListener(
					new WindowAdapter() {
						public void windowClosing(WindowEvent e) {
							sb.signoff();
							sb.disconnect();
						}
					}
			);
			
			SessionGUI sessgui = new SessionGUI(sb.getSession());
			JPanel main = new JPanel(new BorderLayout());
    		main.add(sessgui,BorderLayout.CENTER);
    		frame.setContentPane(main);
			frame.setVisible(true);
			sb.connect();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static SessionBean createSessionbean() throws Exception {

		String system = "127.0.0.1"; // TODO: your IP/hostname

		SessionBean sessionBean = null;
		SessionConfig config = new SessionConfig(system,system);
		config.setProperty("font", "Lucida Sans Typewriter Regular"); // example config  

		Session5250 session	= new Session5250(new Properties(),system,system,config);

		sessionBean = new SessionBean(session);

		sessionBean.setHostName(system);
		sessionBean.setCodePage("Cp273");
		sessionBean.setNoSaveConfigFile();
		sessionBean.setScreenSize("27x132");
		sessionBean.setDeviceName("devname");

		return sessionBean;
	}

}
