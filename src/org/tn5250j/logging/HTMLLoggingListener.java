package org.tn5250j.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JFileChooser;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.tn5250j.Session5250;
import org.tn5250j.TN5250jConstants;
import org.tn5250j.event.ScreenOIAListener;
import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenField;
import org.tn5250j.framework.tn5250.ScreenOIA;

import org.tn5250j.logging.HTMLBuilder.HTMLLogInfo;


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
			final VelocityContext context = new VelocityContext();
			context.put("info", infos);
			context.put("date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(Calendar.getInstance().getTime()));
			VelocityEngine ve = new VelocityEngine();
			ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
			ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
			ve.init();
			Template t = ve.getTemplate("/logging/HTMLLogger.template.html");
			t.merge( context, writer );
			writer.close();
			writer=null;
		}
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
	}
	
	public static class RowReader {
		Screen5250 screen;
		int cols;
		int pos = 0;
		String screenChars;
		String attributes;
		final boolean isattr[];

		public RowReader(final Screen5250 screen) {
			this.screen = screen;
			cols = screen.getColumns();
			screenChars = new String(screen.getScreenAsChars());
			final char attr[] = new char[screenChars.length()];
			screen.GetScreen(attr, attr.length, TN5250jConstants.PLANE_ATTR);
			attributes = new String(attr);
			final char isattr[] = new char[screenChars.length()];
			screen.GetScreen(isattr, isattr.length, TN5250jConstants.PLANE_IS_ATTR_PLACE);
			this.isattr = new boolean[screenChars.length()];
			for(int i=0;i<this.isattr.length;i++){
				this.isattr[i] = isattr[i] > 0;
			}
		}

		public String readRow() {
			if (pos + cols <= screenChars.length()) {
				final String row = screenChars.substring(pos, pos + cols);
				final String rowAttr = attributes.substring(pos, pos + cols);
				
				pos += cols;
				final StringBuilder sb = new StringBuilder();
				char currentAttr = ' ';
				ScreenAttribute currentAttrEnum = ScreenAttribute.GRN;
				sb.append("<pre>");
				sb.append("<span class=\"greenText\">");
				for (int i = 0; i < cols; i++) {
					//this should be cleaned up.  but make sure it is blank when it's an attribute.
					final boolean isAttribute = isattr[(pos-cols)+i];
					if(isAttribute){
						if(currentAttrEnum.isUnderLine()){
							sb.append("</span><span class=\"greenText\">").append(' ');
							if(currentAttr == rowAttr.charAt(i)){
								sb.append("</span><span").append(" class=\"");
								sb.append(doClass(currentAttrEnum)).append("\">");
							}
						}else{
							sb.append(' ');
						}
					}
					// The first underline is not shown
					if (currentAttr != rowAttr.charAt(i)) {
						currentAttr = rowAttr.charAt(i);
						currentAttrEnum = ScreenAttribute.getAttrEnum(currentAttr);
						sb.append("</span>").append("<span");
						sb.append(" class=\"").append(doClass(currentAttrEnum)).append("\">");
					}
					if(!isAttribute){
						if (currentAttrEnum.isNonDisplay()) {
							sb.append(" ");
						} else {
							sb.append(StringEscapeUtils.escapeHtml(String.valueOf(row.charAt(i))));
						}
					}
				}
				sb.append("</span>");
				sb.append("</pre>");
				return sb.toString();
			}
			return null;
		}

		String doClass(final ScreenAttribute attr) {
			final StringBuilder sb = new StringBuilder();
			sb.append(attr.getColor()).append("Text");
			if (attr.isUnderLine()) {
				sb.append(" underline");
			}
			return sb.toString();
		}
	}

	public void addNode(String note){
		if(info!=null){
			info.addText(note);
		}
	}
}
