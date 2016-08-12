package org.tn5250j.logging;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.tn5250j.Session5250;
import org.tn5250j.framework.tn5250.Screen5250;


public class HTMLBuilder {

	List<HTMLLogInfo> infos = new ArrayList<HTMLLogInfo>();
	static public final String NEW_LINE = System.getProperty("line.separator");
	
	final Writer writer;

	public HTMLBuilder(final Writer writer) throws IOException {
		Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		Velocity.init();
		this.writer = writer;
	}

	public void logScreen(final Session5250 session) throws IOException {
		logScreen(session, null);
	}

	public void logScreen(final Session5250 session, final String notes) throws IOException {
		infos.add(new HTMLLogInfo(getHTML(session.getScreen()), notes));
	}

	public void close() throws IOException {
		final VelocityContext context = new VelocityContext();
		context.put("info", infos);
		Velocity.mergeTemplate("/com/terminaldriver/tn5250j/logger/HTMLLogger.template.html", "UTF-8", context, writer);
		writer.close();
	}

	public static class HTMLLogInfo {
		public final String screenHtml;
		public String logText;
		public String logName;
		List<FieldInfo> fields = new ArrayList<FieldInfo>();

		public String getLogName() {
			return logName;
		}

		public void setLogName(String logName) {
			this.logName = logName;
		}

		public String getScreenHtml() {
			return screenHtml;
		}

		public String getLogText() {
			return logText;
		}

		public List<FieldInfo> getFields() {
			return fields;
		}

		public HTMLLogInfo(final String screenHtml, final String logText) {
			super();
			this.screenHtml = screenHtml;
			this.logText = logText;
		}

		public void addText(final String text) {
			if (logText == null) {
				logText = "";
			}
			logText += "<br>" + text;
		}

		public static class FieldInfo {

		}
	}

	public void addLog(final HTMLLogInfo info) {
		addLog(info, false);
	}

	/**
	 * Auto combine two subsequent identical screens, unless verbose = true
	 * 
	 * @param info
	 * @param verbose
	 */
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
	
	public static String getHTML(final Screen5250 screen) {
		final StringBuilder sb = new StringBuilder();
		final HTMLLoggingListener.RowReader rowReader = new HTMLLoggingListener.RowReader(screen);
		String row;
		sb.append("<div class=\"console\">");
		while ((row = rowReader.readRow()) != null) {
			sb.append("<span class=\"consoleline\">");
			sb.append(row);
			sb.append("</span>").append(NEW_LINE);
		}
		sb.append("</div>").append(NEW_LINE);
		return sb.toString();
	}
}
