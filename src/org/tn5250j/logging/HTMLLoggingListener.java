package org.tn5250j.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JFileChooser;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.tn5250j.Session5250;
import org.tn5250j.event.ScreenOIAListener;
import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenField;
import org.tn5250j.framework.tn5250.ScreenOIA;
import org.tn5250j.logging.HTMLBuilder.HTMLLogInfo;

/**
 * Implementation of LoggingListener that renders the screens
 * in a series of HTML 'screen shots'
 */
public class HTMLLoggingListener extends LoggingListener{

	JFileChooser fileChooser = new JFileChooser();
	List<HTMLLogInfo> infos = new ArrayList<HTMLLogInfo>();
	
	
	/**
	 * Time in milliseconds of the last time the full screen was changed
	 */
	long lastScreenChange;
	/**
	 * Time in milliseconds of the last time the screen was partially
	 * updated
	 */
	long lastScreenUpdate;
	private FileWriter writer;
	private HTMLLogInfo info;
	
	public HTMLLoggingListener(final Session5250 session) {
		super(session);
	}
	
	@Override
	public void onOIAChanged(ScreenOIA oia, int change) {
		if(change==ScreenOIAListener.OIA_CHANGED_INPUTINHIBITED){
			onInputInhibited(oia.getInputInhibited() != ScreenOIA.INPUTINHIBITED_NOTINHIBITED);
		}
	}

	public void onInputInhibited(boolean b) {
	}
	
	@Override
	public void onScreenChanged(int inUpdate, int startRow, int startCol, int endRow, int endCol) {
		if (startRow == 0 && startCol == 0 && endRow >= 23 && endCol >= 79) {
			onScreenChanged();
			lastScreenChange = System.currentTimeMillis();
		} else {
			onScreenPartialUpdate(startRow, startCol, endRow, endCol);
		}
		lastScreenUpdate = System.currentTimeMillis();
	}
	
	public void onScreenChanged() {
		//Skip entirely blank screen updates
		if(!new String(session.getScreen().getScreenAsAllChars()).trim().isEmpty()){
			if (info != null) {
				addLog(info, false);
			}
			info = new HTMLLogInfo(HTMLBuilder.getHTML(session.getScreen()), null);
		}
	}
	
	public void addLog(final HTMLLogInfo info, final boolean verbose) {
		if (!verbose && info != null && infos.size() > 0) {
			final HTMLLogInfo lastone = infos.get(infos.size() - 1);
			if (lastone.getScreenHtml().equals(info.getScreenHtml())) {
				if (info.getLogText() != null && !info.getLogText().trim().isEmpty()) {
					lastone.addText(info.getLogText().trim());
				}
				return;
			}
		}
		infos.add(info);
	}
	
	public void onScreenPartialUpdate(int startRow, int startCol, int endRow, int endCol) {
		//Single row changes are updates
		if (info != null && Math.abs(endRow-startRow) < 3) {
			info.setScreenHtml(HTMLBuilder.getHTML(session.getScreen()));
		}else{
			if (info != null) {
				addLog(info, false);
			}
			info = new HTMLLogInfo(HTMLBuilder.getHTML(session.getScreen()), null);
		}
	}

	@Override
	public void onScreenSizeChanged(int rows, int cols) {
	}

	@Override
	public void keysSent(Screen5250 screen, String keys) {
		if(info!=null){
			info.addText("Send Keys:" + keys + HTMLBuilder.NEW_LINE);
		}
	}

	@Override
	public void fieldStringSet(Screen5250 screen, ScreenField field, String keys) {
		if(info!=null){
			info.addText("Set Field:" + keys + HTMLBuilder.NEW_LINE);
		}
	}

	@Override
	public void onSessionChanged(SessionChangeEvent changeEvent) {
	}

	@Override
	public void close() throws IOException {
		if(writer!=null){
			if (info != null) {
				addLog(info, false);
			}
			final VelocityContext context = new VelocityContext();
			context.put("info", infos);
			context.put("date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(Calendar.getInstance().getTime()));
			VelocityEngine ve = new VelocityEngine();
			ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
			ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
			ve.init();
			Template t = ve.getTemplate("/logging/HTMLLogger.template.vm");
			t.merge( context, writer );
			writer.close();
			writer=null;
		}
		infos.clear();
		super.close();
	}

    protected void finalize() throws IOException{ 
    	close();
    }

	@Override
	public void open() throws IOException{
		info = new HTMLLogInfo(HTMLBuilder.getHTML(session.getScreen()), null);
		
		int result = fileChooser.showSaveDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			if (!selectedFile.getName().endsWith(".html") && !selectedFile.getName().endsWith(".htm")){
				selectedFile = new File(selectedFile.getAbsolutePath() + ".html");
			}
			writer = new FileWriter(selectedFile);
		}
		super.open();
	}

	public void addNode(String note){
		if(info!=null){
			info.addText(note);
		}
	}
}
