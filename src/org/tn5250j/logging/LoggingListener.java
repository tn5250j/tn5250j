package org.tn5250j.logging;

import java.io.Closeable;
import java.io.IOException;

import org.tn5250j.Session5250;
import org.tn5250j.event.ScreenListener;
import org.tn5250j.event.ScreenOIAListener;
import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.event.SessionKeysListener;
import org.tn5250j.event.SessionListener;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenField;
import org.tn5250j.framework.tn5250.ScreenOIA;

/**
 * Base class for logging the session screens
 * 
 */
public abstract class LoggingListener implements SessionListener,SessionKeysListener,ScreenListener,ScreenOIAListener,Closeable{

	final Session5250 session;
	boolean open=false;
	
	public LoggingListener(final Session5250 session) {
		super();
		this.session = session;
	}

	@Override
	public void onOIAChanged(ScreenOIA oia, int change) {
		if(change==ScreenOIAListener.OIA_CHANGED_INPUTINHIBITED){
			onInputInhibited(oia.getInputInhibited() != ScreenOIA.INPUTINHIBITED_NOTINHIBITED);
		}
	}

	public void onInputInhibited(boolean b) {
		
	}
	
	public void open() throws IOException{
		open=true;
	}
	
	/**
	 * Time in milliseconds of the last time the full screen was changed
	 */
	long lastScreenChange;
	/**
	 * Time in milliseconds of the last time the screen was partially
	 * updated
	 */
	long lastScreenUpdate;
	

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
		
	}
	
	public void onScreenPartialUpdate(int startRow, int startCol, int endRow, int endCol) {
		
	}

	@Override
	public void onScreenSizeChanged(int rows, int cols) {

	}

	@Override
	public void keysSent(Screen5250 screen, String keys) {
		
	}

	@Override
	public void fieldStringSet(Screen5250 screen, ScreenField field, String keys) {
		
	}

	@Override
	public void onSessionChanged(SessionChangeEvent changeEvent) {
		
	}

	@Override
	public void close() throws IOException {
		open=false;
	}
	
	public void addNode(String note){
		
	}
	
	public boolean isOpen(){
		return open;
	}

}
