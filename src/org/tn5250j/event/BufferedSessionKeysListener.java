package org.tn5250j.event;

import java.util.Arrays;
import java.util.List;

import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenField;
import org.tn5250j.framework.tn5250.ScreenOIA;

public class BufferedSessionKeysListener implements SessionKeysListener,ScreenListener,ScreenOIAListener {

	boolean lastInhibited=false;
	StringBuilder keyBuffer= new StringBuilder();
	final SessionKeysListener wrappedListener;
	
	public BufferedSessionKeysListener(SessionKeysListener wrappedListener) {
		super();
		this.wrappedListener = wrappedListener;
	}

	@Override
	public void onOIAChanged(ScreenOIA oia, int change) {
		if(change==ScreenOIAListener.OIA_CHANGED_INPUTINHIBITED){
			boolean inputInhibited = (oia.getInputInhibited() != ScreenOIA.INPUTINHIBITED_NOTINHIBITED);
			if(lastInhibited!=inputInhibited){
				flushKeys();
				lastInhibited=inputInhibited;
			}
		}
	}

	@Override
	public void onScreenChanged(int inUpdate, int startRow, int startCol, int endRow, int endCol) {
		if(startRow != endRow){
			flushKeys();
		}
	}

	@Override
	public void onScreenSizeChanged(int rows, int cols) {
		flushKeys();
	}
	
	final List<Integer> NON_DISPLAYS_ATTR = Arrays.asList((int)'\'',(int)'/',(int)'7',(int)'?');

	@Override
	public void keysSent(Screen5250 screen, String keys) {
		if(keys.length()==1){
			ScreenField field = screen.getScreenFields().getCurrentField();
			//Hide password keys
			if(field != null && NON_DISPLAYS_ATTR.contains(field.getAttr())){
				keyBuffer.append("*");
			}else{
				keyBuffer.append(keys);
			}
		}else{
			keyBuffer.append(keys);
		}
		if(keys.startsWith("[") && keys.endsWith("]")){
			wrappedListener.keysSent(screen, keyBuffer.toString());
			keyBuffer.setLength(0);
		}
	}

	@Override
	public void fieldStringSet(Screen5250 screen, ScreenField field, String keys) {
		wrappedListener.fieldStringSet(screen, field,keys);
	}

	public void flushKeys(){
		if(keyBuffer.length()>0){
			keyBuffer.setLength(0);
		}
	}
}
