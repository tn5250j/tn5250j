/**
 * Title: tnvt.java
 * Copyright: Copyright (c) 2001 Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.5
 *
 * Description:
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this software; see the file COPYING. If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */
package org.tn5250j.framework.tn5250;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.tn5250j.TN5250jConstants.AID_HELP;
import static org.tn5250j.TN5250jConstants.AID_PRINT;
import static org.tn5250j.TN5250jConstants.CMD_CLEAR_FORMAT_TABLE;
import static org.tn5250j.TN5250jConstants.CMD_CLEAR_UNIT;
import static org.tn5250j.TN5250jConstants.CMD_CLEAR_UNIT_ALTERNATE;
import static org.tn5250j.TN5250jConstants.CMD_READ_INPUT_FIELDS;
import static org.tn5250j.TN5250jConstants.CMD_READ_MDT_FIELDS;
import static org.tn5250j.TN5250jConstants.CMD_READ_MDT_IMMEDIATE_ALT;
import static org.tn5250j.TN5250jConstants.CMD_READ_SCREEN_IMMEDIATE;
import static org.tn5250j.TN5250jConstants.CMD_READ_SCREEN_TO_PRINT;
import static org.tn5250j.TN5250jConstants.CMD_RESTORE_SCREEN;
import static org.tn5250j.TN5250jConstants.CMD_ROLL;
import static org.tn5250j.TN5250jConstants.CMD_SAVE_SCREEN;
import static org.tn5250j.TN5250jConstants.CMD_WRITE_ERROR_CODE;
import static org.tn5250j.TN5250jConstants.CMD_WRITE_ERROR_CODE_TO_WINDOW;
import static org.tn5250j.TN5250jConstants.CMD_WRITE_STRUCTURED_FIELD;
import static org.tn5250j.TN5250jConstants.CMD_WRITE_TO_DISPLAY;
import static org.tn5250j.TN5250jConstants.NR_REQUEST_ERROR;
import static org.tn5250j.TN5250jConstants.PF1;
import static org.tn5250j.TN5250jConstants.PF10;
import static org.tn5250j.TN5250jConstants.PF11;
import static org.tn5250j.TN5250jConstants.PF12;
import static org.tn5250j.TN5250jConstants.PF13;
import static org.tn5250j.TN5250jConstants.PF14;
import static org.tn5250j.TN5250jConstants.PF15;
import static org.tn5250j.TN5250jConstants.PF16;
import static org.tn5250j.TN5250jConstants.PF17;
import static org.tn5250j.TN5250jConstants.PF18;
import static org.tn5250j.TN5250jConstants.PF19;
import static org.tn5250j.TN5250jConstants.PF2;
import static org.tn5250j.TN5250jConstants.PF20;
import static org.tn5250j.TN5250jConstants.PF21;
import static org.tn5250j.TN5250jConstants.PF22;
import static org.tn5250j.TN5250jConstants.PF23;
import static org.tn5250j.TN5250jConstants.PF24;
import static org.tn5250j.TN5250jConstants.PF3;
import static org.tn5250j.TN5250jConstants.PF4;
import static org.tn5250j.TN5250jConstants.PF5;
import static org.tn5250j.TN5250jConstants.PF6;
import static org.tn5250j.TN5250jConstants.PF7;
import static org.tn5250j.TN5250jConstants.PF8;
import static org.tn5250j.TN5250jConstants.PF9;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.net.ssl.SSLSocket;
import javax.swing.SwingUtilities;

import org.tn5250j.Session5250;
import org.tn5250j.TN5250jConstants;
import org.tn5250j.encoding.CharMappings;
import org.tn5250j.encoding.ICodePage;
import org.tn5250j.framework.transport.SocketConnector;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

public final class tnvt implements Runnable {


	// negotiating commands
	private static final byte IAC = (byte) -1; // 255 FF
	private static final byte DONT = (byte) -2; //254 FE
	private static final byte DO = (byte) -3; //253 FD
	private static final byte WONT = (byte) -4; //252 FC
	private static final byte WILL = (byte) -5; //251 FB
	private static final byte SB = (byte) -6; //250 Sub Begin FA
	private static final byte SE = (byte) -16; //240 Sub End F0
	private static final byte EOR = (byte) -17; //239 End of Record EF
	private static final byte TERMINAL_TYPE = (byte) 24; // 18
	private static final byte OPT_END_OF_RECORD = (byte) 25; // 19
	private static final byte TRANSMIT_BINARY = (byte) 0; // 0
	private static final byte QUAL_IS = (byte) 0; // 0
	private static final byte TIMING_MARK = (byte) 6; // 6
	private static final byte NEW_ENVIRONMENT = (byte) 39; // 27
	private static final byte IS = (byte) 0; // 0
	private static final byte SEND = (byte) 1; // 1
	private static final byte INFO = (byte) 2; // 2
	private static final byte VAR = (byte) 0; // 0
	private static final byte VALUE = (byte) 1; // 1
	private static final byte NEGOTIATE_ESC = (byte) 2; // 2
	private static final byte USERVAR = (byte) 3; // 3

	// miscellaneous
	private static final byte ESC = 0x04; // 04

	private Socket sock;
	private BufferedInputStream bin;
	private BufferedOutputStream bout;
	private final BlockingQueue<Object> dsq = new ArrayBlockingQueue<Object>(25);
	private Stream5250 bk;
	private DataStreamProducer producer;
	protected Screen5250 screen52;
	private boolean waitingForInput;
	private boolean invited;
	private boolean negotiated = false;
	private Thread me;
	private Thread pthread;
	private int readType;
	private boolean enhanced = true;
	private Session5250 controller;
	private boolean cursorOn = false;
	private String session = "";
	private int port = 23;
	private boolean connected = false;
	private boolean support132 = true;
	private ByteArrayOutputStream baosp = null;
	private ByteArrayOutputStream baosrsp = null;
	private int devSeq = -1;
	private String devName;
	private String devNameUsed;
	private KbdTypesCodePages kbdTypesCodePage;
	// WVL - LDC : TR.000300 : Callback scenario from 5250
	private boolean scan; // = false;
	private static int STRSCAN = 1;
	// WVL - LDC : 05/08/2005 : TFX.006253 - support STRPCCMD
	private boolean strpccmd; // = false;
	private String user;
	private String password;
	private String library;
	private String initialMenu;
	private String program;
	private boolean keepTrucking = true;
	private boolean pendingUnlock = false;
	private boolean[] dataIncluded;
	protected ICodePage codePage;
	private boolean firstScreen;
	private String sslType;
	private WTDSFParser sfParser;

	private final TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());

	/**
	 * @param session
	 * @param screen52
	 * @param type
	 * @param support132
	 */
	public tnvt(Session5250 session, Screen5250 screen52, boolean type, boolean support132) {

		controller = session;
		if (log.isInfoEnabled()) {
			log.info(" new session -> " + controller.getSessionName());
		}

		enhanced = type;
		this.support132 = support132;
		setCodePage("37");
		this.screen52 = screen52;
		dataIncluded = new boolean[24];

		if (System.getProperties().containsKey("SESSION_CONNECT_USER")) {
			user = System.getProperties().getProperty("SESSION_CONNECT_USER");
			if (System.getProperties().containsKey("SESSION_CONNECT_PASSWORD"))
				password = System.getProperties().getProperty(
				"SESSION_CONNECT_PASSWORD");
			if (System.getProperties().containsKey("SESSION_CONNECT_LIBRARY"))
				library = System.getProperties().getProperty(
				"SESSION_CONNECT_LIBRARY");
			if (System.getProperties().containsKey("SESSION_CONNECT_MENU"))
				initialMenu = System.getProperties().getProperty(
				"SESSION_CONNECT_MENU");
			if (System.getProperties().containsKey("SESSION_CONNECT_PROGRAM"))
				program = System.getProperties().getProperty(
				"SESSION_CONNECT_PROGRAM");
		}

		baosp = new ByteArrayOutputStream();
		baosrsp = new ByteArrayOutputStream();
	}

	public String getHostName() {

		return session;
	}


	public void setSSLType(String type) {
		sslType = type;
	}

	public void setDeviceName(String name) {

		devName = name;

	}

	public String getDeviceName() {
		return devName;
	}

	public String getAllocatedDeviceName() {
		return devNameUsed;
	}

	public boolean isConnected() {

		return connected;
	}

	/**
	 * @return true when SSL is used and socket is connected.
	 * @see {@link #isConnected()}
	 */
	public boolean isSslSocket() {
		if (this.connected && this.sock != null && this.sock instanceof SSLSocket) {
			return true;
		} else {
			return false;
		}
	}

	public final void setProxy(String proxyHost, String proxyPort) {

		Properties systemProperties = System.getProperties();
		systemProperties.put("socksProxySet", "true");
		systemProperties.put("socksProxyHost", proxyHost);
		systemProperties.put("socksProxyPort", proxyPort);

		System.setProperties(systemProperties);
		log.info(" socks set ");
	}

	public final boolean connect() {

		return connect(session, port);

	}


	public final boolean connect(String s, int port) {

		// We will now see if there are any bypass signon parameters to be
		//    processed. The system properties override these parameters so
		//    have precidence if specified.
		Properties props = controller.getConnectionProperties();
		if (user == null && props.containsKey("SESSION_CONNECT_USER")) {
			user = props.getProperty("SESSION_CONNECT_USER");
			log.info(" user -> " + user + " " + controller.getSessionName());
			if (props.containsKey("SESSION_CONNECT_PASSWORD"))
				password = props.getProperty("SESSION_CONNECT_PASSWORD");
			if (props.containsKey("SESSION_CONNECT_LIBRARY"))
				library = props.getProperty("SESSION_CONNECT_LIBRARY");
			if (props.containsKey("SESSION_CONNECT_MENU"))
				initialMenu = props.getProperty("SESSION_CONNECT_MENU");
			if (props.containsKey("SESSION_CONNECT_PROGRAM"))
				program = props.getProperty("SESSION_CONNECT_PROGRAM");
		}


		try {
			session = s;
			this.port = port;

			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						screen52.getOIA().setInputInhibited(ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT,
								ScreenOIA.OIA_LEVEL_INPUT_INHIBITED,"X - Connecting");
					}
				});

			} catch (Exception exc) {
				log.warn("setStatus(ON) " + exc.getMessage());

			}

			//         sock = new Socket(s, port);
			//smk - For SSL compability
			SocketConnector sc = new SocketConnector();
			if (sslType != null)
				sc.setSSLType(sslType);
			sock = sc.createSocket(s, port);

			if (sock == null) {
				log.warn("I did not get a socket");
				disconnect();
				return false;
			}

			connected = true;
			// used for JDK1.3
			sock.setKeepAlive(true);
			sock.setTcpNoDelay(true);
			sock.setSoLinger(false, 0);
			InputStream in = sock.getInputStream();
			OutputStream out = sock.getOutputStream();

			bin = new BufferedInputStream(in, 8192);
			bout = new BufferedOutputStream(out);

			byte abyte0[];
			while (negotiate(abyte0 = readNegotiations()));
			negotiated = true;
			try {
				screen52.setCursorActive(false);
			} catch (Exception excc) {
				log.warn("setCursorOff " + excc.getMessage());

			}

			producer = new DataStreamProducer(this, bin, dsq, abyte0);
			pthread = new Thread(producer);
			//         pthread.setPriority(pthread.MIN_PRIORITY);
			pthread.setPriority(Thread.NORM_PRIORITY);
			//			pthread.setPriority(Thread.NORM_PRIORITY / 2);
			pthread.start();

			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						screen52.getOIA().setInputInhibited(ScreenOIA.INPUTINHIBITED_NOTINHIBITED,
								ScreenOIA.OIA_LEVEL_INPUT_INHIBITED);
					}
				});

			} catch (Exception exc) {
				log.warn("setStatus(OFF) " + exc.getMessage());
			}

			keepTrucking = true;
			me = new Thread(this);
			me.start();

		} catch (Exception exception) {
			if (exception.getMessage() == null)
				exception.printStackTrace();
			log.warn("connect() " + exception.getMessage());

			if (sock == null)
				log.warn("I did not get a socket");

			disconnect();
			return false;
		}
		return true;

	}

	public final boolean disconnect() {

		// Added by LUC - LDC to fix a null pointer exception.
		if (!connected) {
			screen52.getOIA().setInputInhibited(ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT,
					ScreenOIA.OIA_LEVEL_INPUT_INHIBITED,"X - Disconnected");
			return false;
		}

		if (me != null && me.isAlive()) {
			me.interrupt();
			keepTrucking = false;
			pthread.interrupt();
		}

		screen52.getOIA().setInputInhibited(ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT,
				ScreenOIA.OIA_LEVEL_INPUT_INHIBITED,"X - Disconnected");
		screen52.getOIA().setKeyBoardLocked(false);
		pendingUnlock = false;

		try {
			if (sock != null) {
				log.info("Closing socket");
				sock.close();
			}
			if (bin != null)
				bin.close();
			if (bout != null)
				bout.close();
			connected = false;
			firstScreen = false;

			// WVL - LDC : TR.000345 : properly disconnect and clear screen
			// Is this the right place to set screen realestate on disconnect?
			//controller.getScreen().clearAll();
			screen52.goto_XY(0);
			screen52.setCursorActive(false);
			screen52.clearAll();
			screen52.restoreScreen();

			controller.fireSessionChanged(TN5250jConstants.STATE_DISCONNECTED);

		} catch (Exception exception) {
			log.warn(exception.getMessage());
			connected = false;
			devSeq = -1;
			return false;

		}
		devSeq = -1;
		return true;
	}

	private final ByteArrayOutputStream appendByteStream(byte abyte0[]) {
		ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
		for (int i = 0; i < abyte0.length; i++) {
			bytearrayoutputstream.write(abyte0[i]);
			if (abyte0[i] == -1)
				bytearrayoutputstream.write(-1);
		}

		return bytearrayoutputstream;
	}

	private final byte[] readNegotiations() throws IOException {
		int i = bin.read();
		if (i < 0) {
			throw new IOException("Connection closed.");
		} else {
			int j = bin.available();
			byte abyte0[] = new byte[j + 1];
			abyte0[0] = (byte) i;
			bin.read(abyte0, 1, j);
			return abyte0;
		}
	}

	private final void writeByte(byte abyte0[]) throws IOException {

		bout.write(abyte0);
		bout.flush();
	}

	//	private final void writeByte(byte byte0) throws IOException {
	//
	//		bout.write(byte0);
	//		bout.flush();
	//	}

	public final void sendHeartBeat() throws IOException {

		byte[] b = { (byte) 0xff, (byte) 0xf1 };
		bout.write(b);
		bout.flush();
	}

	private final void readImmediate(int readType) {

		if (screen52.isStatusErrorCode()) {
			screen52.restoreErrorLine();
			screen52.setStatus(Screen5250.STATUS_ERROR_CODE,
					Screen5250.STATUS_VALUE_OFF, null);
		}

		if (!enhanced) {
			screen52.setCursorActive(false);
		}
		//		screen52.setStatus(Screen5250.STATUS_SYSTEM,
		//				Screen5250.STATUS_VALUE_ON, null);
		screen52.getOIA().setInputInhibited(ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT,
				ScreenOIA.OIA_LEVEL_INPUT_INHIBITED);

		screen52.getOIA().setKeyBoardLocked(true);
		pendingUnlock = false;
		invited = false;

		screen52.getScreenFields().readFormatTable(baosp, readType, codePage);

		try {

			writeGDS(0, 3, baosp.toByteArray());
		} catch (IOException ioe) {

			log.warn(ioe.getMessage());
			baosp.reset();
		}
		baosp.reset();

	}

	public final boolean sendAidKey(int aid) {

		if (screen52.isStatusErrorCode()) {
			screen52.restoreErrorLine();
			screen52.setStatus(Screen5250.STATUS_ERROR_CODE,
					Screen5250.STATUS_VALUE_OFF, null);
		}

		if (!enhanced) {
			screen52.setCursorActive(false);
		}
		//		screen52.setStatus(Screen5250.STATUS_SYSTEM,
		//				Screen5250.STATUS_VALUE_ON, null);
		screen52.getOIA().setInputInhibited(ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT,
				ScreenOIA.OIA_LEVEL_INPUT_INHIBITED);

		screen52.getOIA().setKeyBoardLocked(true);
		pendingUnlock = false;
		invited = false;
		baosp.write(screen52.getCurrentRow());
		baosp.write(screen52.getCurrentCol());
		baosp.write(aid);

		if (dataIncluded(aid))

			screen52.getScreenFields().readFormatTable(baosp, readType,
					codePage);

		try {

			writeGDS(0, 3, baosp.toByteArray());
		} catch (IOException ioe) {

			log.warn(ioe.getMessage());
			baosp.reset();
			return false;
		}
		baosp.reset();
		return true;

	}

	private boolean dataIncluded(int aid) {

		switch (aid) {

		case PF1:
			return !dataIncluded[0];
		case PF2:
			return !dataIncluded[1];
		case PF3:
			return !dataIncluded[2];
		case PF4:
			return !dataIncluded[3];
		case PF5:
			return !dataIncluded[4];
		case PF6:
			return !dataIncluded[5];
		case PF7:
			return !dataIncluded[6];
		case PF8:
			return !dataIncluded[7];
		case PF9:
			return !dataIncluded[8];
		case PF10:
			return !dataIncluded[9];
		case PF11:
			return !dataIncluded[10];
		case PF12:
			return !dataIncluded[11];
		case PF13:
			return !dataIncluded[12];
		case PF14:
			return !dataIncluded[13];
		case PF15:
			return !dataIncluded[14];
		case PF16:
			return !dataIncluded[15];
		case PF17:
			return !dataIncluded[16];
		case PF18:
			return !dataIncluded[17];
		case PF19:
			return !dataIncluded[18];
		case PF20:
			return !dataIncluded[19];
		case PF21:
			return !dataIncluded[20];
		case PF22:
			return !dataIncluded[21];
		case PF23:
			return !dataIncluded[22];
		case PF24:
			return !dataIncluded[23];

		default:
			return true;

		}

	}

	/**
	 * Help request -
	 *
	 *
	 * See notes inside method
	 */
	public final void sendHelpRequest() {

		// Client sends header 000D12A0000004000003####F3FFEF
		//       operation code 3
		//       row - first ##
		//       column - second ##
		//       F3 - Help Aid Key
		//      System.out.println("Help request sent");
		baosp.write(screen52.getCurrentRow());
		baosp.write(screen52.getCurrentCol());
		baosp.write(AID_HELP);

		try {
			writeGDS(0, 3, baosp.toByteArray());
		} catch (IOException ioe) {

			log.warn(ioe.getMessage());
		}
		baosp.reset();
	}

	/**
	 * Attention Key -
	 *
	 *
	 * See notes inside method
	 */
	public final void sendAttentionKey() {

		// Client sends header 000A12A000004400000FFEF
		//    0x40 -> 01000000
		//
		// flags
		// bit 0 - ERR
		// bit 1 - ATN Attention
		// bits 2-4 - reserved
		// bit 5 - SRQ system request
		// bit 6 - TRQ Test request key
		// bit 7 - HLP

		//      System.out.println("Attention key sent");

		try {
			writeGDS(0x40, 0, null);
		} catch (IOException ioe) {

			log.warn(ioe.getMessage());
		}
	}

	/**
	 * Opens a dialog and asks the user before sending a request
	 *
	 * @see {@link #systemRequest(String)}
	 */
	public final void systemRequest() {
		final String sysreq = this.controller.showSystemRequest();
		systemRequest(sysreq);
	}

	/**
	 * @param sr - system request option
	 * @see {@link #systemRequest(String)}
	 */
	public final void systemRequest(char sr) {
		systemRequest(Character.toString(sr));
	}

	/**
	 * System request, taken from the rfc1205, 5250 Telnet interface section 4.3
	 *
	 * @param sr system request option (allowed to be null, but than nothing happens)
	 */
	public final void systemRequest(String sr) {
		byte[] bytes = null;

		if ( (sr != null) && (sr.length() > 0)) {
			// XXX: Not sure, if this is a sufficient check for 'clear dataq'
			if (sr.charAt(0) == '2') {
				dsq.clear();
			}
			for (int i = 0, l = sr.length(); i < l; i++) {
				baosp.write(codePage.uni2ebcdic(sr.charAt(i)));
			}
			bytes = baosp.toByteArray();
		}

		try	{
			writeGDS(4, 0, bytes);
		} catch (IOException ioe) {
			log.info(ioe.getMessage());
		}
		baosp.reset();
	}

	/**
	 * Cancel Invite - taken from the rfc1205 - 5250 Telnet interface section
	 * 4.3
	 *
	 * See notes inside method
	 */
	public final void cancelInvite() {

		//		screen52.setStatus(Screen5250.STATUS_SYSTEM,
		//				Screen5250.STATUS_VALUE_ON, null);
		screen52.getOIA().setInputInhibited(ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT,
				ScreenOIA.OIA_LEVEL_INPUT_INHIBITED);

		// from rfc1205 section 4.3
		// Server: Sends header with the 000A12A0 00000400 000AFFEF
		// Opcode = Cancel Invite.

		// Client: sends header with the 000A12A0 00000400 000AFFEF
		// Opcode = Cancel Invite to
		// indicate that the work station is
		// no longer invited.
		try {
			writeGDS(0, 10, null);
		} catch (IOException ioe) {

			log.warn(ioe.getMessage());
		}

	}

	public final void hostPrint(int aid) {

		if (screen52.isStatusErrorCode()) {
			screen52.restoreErrorLine();
			screen52.setStatus(Screen5250.STATUS_ERROR_CODE,
					Screen5250.STATUS_VALUE_OFF, null);
		}

		screen52.setCursorActive(false);
		//		screen52.setStatus(Screen5250.STATUS_SYSTEM,
		//				Screen5250.STATUS_VALUE_ON, null);
		screen52.getOIA().setInputInhibited(ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT,
				ScreenOIA.OIA_LEVEL_INPUT_INHIBITED);

		// From client access ip capture
		// it seems to use an operation code of 3 and 4
		// also note that the flag field that says reserved is being sent as
		// well
		// with a value of 0x80
		//
		// I have tried with not setting these flags and sending with 3 or 1
		// there is no effect and I still get a host print screen. Go figure
		//0000: 000D 12A0 0000 0400 8003 1407 F6FFEF
		//0000: 000D 12A0 0000 0400 8001 110E F6FFEF
		//
		// Client sends header 000D12A0000004000003####F6FFEF
		//       operation code 3
		//       row - first ##
		//       column - second ##
		//       F6 - Print Aid Key

		baosp.write(screen52.getCurrentRow());
		baosp.write(screen52.getCurrentCol());
		baosp.write(AID_PRINT); // aid key

		try {
			writeGDS(0, 3, baosp.toByteArray());
		} catch (IOException ioe) {

			log.warn(ioe.getMessage());
		}
		baosp.reset();
	}

	public final void toggleDebug() {
		producer.toggleDebug(codePage);
	}

	// write gerneral data stream
	private final void writeGDS(int flags, int opcode, byte abyte0[])
	throws IOException {

		// Added to fix for JDK 1.4 this was null coming from another method.
		//  There was a weird keyRelease event coming from another panel when
		//  using a key instead of the mouse to select button.
		//  The other method was fixed as well but this check should be here
		// anyway.
		if (bout == null)
			return;

		int length;
		if (abyte0 != null)
			length = abyte0.length + 10;
		else
			length = 10;

		// refer to rfc1205 - 5250 Telnet interface
		// Section 3. Data Stream Format

		// Logical Record Length - 16 bits
		baosrsp.write(length >> 8); // Length LL
		baosrsp.write(length & 0xff); //        LL

		// Record Type - 16 bits
		// It should always be set to '12A0'X to indicate the
		// General Data Stream (GDS) record type.
		baosrsp.write(18); // 0x12
		baosrsp.write(160); // 0xA0

		// the next 16 bits are not used
		baosrsp.write(0); // 0x00
		baosrsp.write(0); // 0x00

		//  The second part is meant to be variable in length
		//  currently this portion is 4 octets long (1 byte or 8 bits for us ;-O)
		baosrsp.write(4); // 0x04

		baosrsp.write(flags); // flags
		// bit 0 - ERR
		// bit 1 - ATN Attention
		// bits 2-4 - reserved
		// bit 5 - SRQ system request
		// bit 6 - TRQ Test request key
		// bit 7 - HLP
		baosrsp.write(0); // reserved - set to 0x00
		baosrsp.write(opcode); // opcode

		if (abyte0 != null)
			baosrsp.write(abyte0, 0, abyte0.length);

		baosrsp = appendByteStream(baosrsp.toByteArray());

		// make sure we indicate no more to be sent
		baosrsp.write(IAC);
		baosrsp.write(EOR);

		baosrsp.writeTo(bout);

		//        byte[] b = new byte[baosrsp.size()];
		//        b = baosrsp.toByteArray();
		//      dump(b);
		bout.flush();
		//      baos = null;
		baosrsp.reset();
	}

	protected final int getOpCode() {

		return bk.getOpCode();
	}

	//	private final void sendNotify() throws IOException {
	//
	//		writeGDS(0, 0, null);
	//	}

	protected boolean[] getActiveAidKeys() {
		boolean aids[] = new boolean[dataIncluded.length];
		System.arraycopy(dataIncluded,0,aids,0,dataIncluded.length);
		return aids;
	}

	private final void setInvited() {

		log.debug("invited");
		if (!screen52.isStatusErrorCode())
			screen52.getOIA().setInputInhibited(ScreenOIA.INPUTINHIBITED_NOTINHIBITED,
					ScreenOIA.OIA_LEVEL_INPUT_INHIBITED);

		invited = true;
	}

	// WVL - LDC : 05/08/2005 : TFX.006253 - Support STRPCCMD
	private void strpccmd()
	{
		try
		{
			int str = 11;
			char c;
			ScreenPlanes planes = screen52.getPlanes();
			c = planes.getChar(str);
			boolean waitFor = !(c == 'a');

			StringBuffer command = new StringBuffer();
			for (int i = str+1; i < 132; i++)
			{
				c = planes.getChar(i);
				if (Character.isISOControl(c))
					c = ' ';
				command.append(c);
			}

			String cmd = command.toString().trim();

			run(cmd, waitFor);
		}
		finally
		{
			strpccmd = false;
			screen52.sendKeys(TN5250jConstants.MNEMONIC_ENTER);
		}
	}

	// WVL - LDC : 05/08/2005 : TFX.006253 - Support STRPCCMD
	private void run(String cmd, boolean waitFor)
	{
		try
		{
			log.debug("RUN cmd = " + cmd);
			log.debug("RUN wait = " + waitFor);

			Runtime r = Runtime.getRuntime();
			Process p = r.exec(cmd);
			if (waitFor)
			{
				int result = p.waitFor();
				log.debug("RUN result = " + result);
			}
		}
		catch (Throwable t)
		{
			log.error(t);
		}
	}


	// WVL - LDC : TR.000300 : Callback scenario from 5250
	/**
	 * Activate or deactivate the command scanning behaviour.
	 *
	 * @param scan
	 *            if true, scanning is enabled; disabled otherwise.
	 *
	 * @see scan4Cmd()
	 */
	public void setScanningEnabled(boolean scan) {
		this.scan = scan;
	}

	// WVL - LDC : TR.000300 : Callback scenario from 5250
	/**
	 * Checks whether command scanning is enabled.
	 *
	 * @return true is command scanning is enabled; false otherwise.
	 */
	public boolean isScanningEnabled() {
		return this.scan;
	}

	// WVL - LDC : TR.000300 : Callback scenario from 5250
	/**
	 * When command scanning is activated, the terminal reads the first and
	 * second character in the datastream (the zero position allows to
	 * devisualize the scan stream). If the sequence <code>#!</code> is
	 * encountered and if this sequence is <strong>not </strong> followed by a
	 * blank character, the {@link parseCommand(ScreenChar[])}is called.
	 */
	private void scan() {
		//     System.out.println("Checking command : " +
		// screen52.screen[1].getChar() + screen52.screen[2].getChar());

		//		ScreenChar[] screen = screen52.screen;
		ScreenPlanes planes = screen52.getPlanes();

		if ((planes.getChar(STRSCAN) == '#')
				&& (planes.getChar(STRSCAN + 1) == '!')
				&& (planes.getChar(STRSCAN + 2) != ' ')) {
			try {
				parseCommand();
			} catch (Throwable t) {
				log.info("Exec cmd: " + t.getMessage());
				t.printStackTrace();
			}
		}
	}

	// WVL - LDC : TR.000300 : Callback scenario from 5250
	/**
	 * The screen is parsed starting from second position until a white space is
	 * encountered. When found the Session#execCommand(String, int) is
	 * called with the parsed string. The position immediately following the
	 * encountered white space, separating the command from the rest of the
	 * screen, is passed as starting index.
	 *
	 * Note that the character at the starting position can potentially be a
	 * white space itself. The starting position in <code>execCommand</code>
	 * provided to make the scanning sequence more flexible. We'd like for
	 * example to embed also a <code>+</code> or <code>-</code> sign to
	 * indicate whether the tnvt should trigger a repaint or not. This would
	 * allow the flashing of command sequences without them becoming visible.
	 *
	 * <ul>
	 * <li><strong>PRE </strong> The screen character at position
	 * <code>STRSCAN + 2</code> is not a white space.</li>
	 * </ul>
	 */
	private void parseCommand() {
		// Search for the command i.e. the first token in the stream
		// after the #! sequence separated by a space from the rest
		// of the screen.
		char[] screen = screen52.getScreenAsAllChars();
		for (int s = STRSCAN + 2, i = s; i < screen.length; i++) {
			if (screen[i] == ' ') {
				String command = new String(screen, s, i - s);

				// Skip all white spaces between the command and the rest of
				// the screen.
				//for (; (i < screen.length) && (screen[i] == ' '); i++);

				String remainder = new String(screen, i + 1, screen.length
						- (i + 1));
				//        System.out.println("Sensing action command in the input! = "
				// + command);
				controller.fireScanned(command, remainder);
				break;
			}
		}
	}

	public void run() {

		if (enhanced)
			sfParser = new WTDSFParser(this);

		bk = new Stream5250();

		while (keepTrucking) {

			try {
				bk.initialize((byte[]) dsq.take());
			} catch (InterruptedException ie) {
				log.warn("   vt thread interrupted and stopping ");
				keepTrucking = false;
				continue;
			}

			// lets play nicely with the others on the playground
			//         me.yield();

			Thread.yield();

			invited = false;

			screen52.setCursorActive(false);

			//      System.out.println("operation code: " + bk.getOpCode());
			if (bk == null)
				continue;

			switch (bk.getOpCode()) {
			case 00:
				log.debug("No operation");
				break;
			case 1:
				log.debug("Invite Operation");
				parseIncoming();
				//               screen52.setKeyboardLocked(false);
				pendingUnlock = true;
				cursorOn = true;
				setInvited();
				break;
			case 2:
				log.debug("Output Only");
				parseIncoming();
				//               System.out.println(screen52.dirty);
				screen52.updateDirty();

				//            invited = true;

				break;
			case 3:
				log.debug("Put/Get Operation");
				parseIncoming();
				//               inviteIt =true;
				setInvited();
				if (!firstScreen) {
					firstScreen = true;
					controller.fireSessionChanged(TN5250jConstants.STATE_CONNECTED);
				}
				break;
			case 4:
				log.debug("Save Screen Operation");
				parseIncoming();
				break;

			case 5:
				log.debug("Restore Screen Operation");
				parseIncoming();
				break;
			case 6:
				log.debug("Read Immediate");
				sendAidKey(0);
				break;
			case 7:
				log.debug("Reserved");
				break;
			case 8:
				log.debug("Read Screen Operation");
				try {
					readScreen();
				} catch (IOException ex) {
					log.warn(ex.getMessage());
				}
				break;

			case 9:
				log.debug("Reserved");
				break;

			case 10:
				log.debug("Cancel Invite Operation");
				cancelInvite();
				break;

			case 11:
				log.debug("Turn on message light");
				screen52.getOIA().setMessageLightOn();
				screen52.setCursorActive(true);

				break;
			case 12:
				log.debug("Turn off Message light");
				screen52.getOIA().setMessageLightOff();
				screen52.setCursorActive(true);

				break;
			default:
				break;
			}

			if (screen52.isUsingGuiInterface())
				screen52.drawFields();

			//      if (screen52.screen[0][1].getChar() == '#' &&
			//         screen52.screen[0][2].getChar() == '!')
			//         execCmd();
			//      else {

			//			if (screen52.isHotSpots()) {
			//				screen52.checkHotSpots();
			//			}

			try {
				if (!strpccmd) {
					//               SwingUtilities.invokeAndWait(
					//                  new Runnable () {
					//                     public void run() {
					//                        screen52.updateDirty();
					//                     }
					//                  }
					//               );
					screen52.updateDirty();
					//				controller.validate();
					//				log.debug("update dirty");
				} else {
					strpccmd();
				}
			} catch (Exception exd) {
				log.warn(" tnvt.run: " + exd.getMessage());
				exd.printStackTrace();
			}

			if (pendingUnlock && !screen52.isStatusErrorCode()) {
				screen52.getOIA().setKeyBoardLocked(false);
				pendingUnlock = false;
			}

			if (cursorOn && !screen52.getOIA().isKeyBoardLocked()) {
				screen52.setCursorActive(true);
				cursorOn = false;
			}

			// lets play nicely with the others on the playground
			//me.yield();
			Thread.yield();

		}
	}

	public void dumpStuff() {

		if (log.isDebugEnabled()) {
			log.debug(" Pending unlock " + pendingUnlock);
			log.debug(" Status Error " + screen52.isStatusErrorCode());
			log.debug(" Keyboard Locked " + screen52.getOIA().isKeyBoardLocked());
			log.debug(" Cursor On " + cursorOn);
			log.debug(" Cursor Active " + screen52.cursorActive);
		}

	}

	//      private final void execCmd() {
	//         String name = "";
	//         String argString = "";
	//
	//         StringBuffer sb = new StringBuffer();
	//         sb.append(screen52.screen[0][3].getChar());
	//         sb.append(screen52.screen[0][4].getChar());
	//         sb.append(screen52.screen[0][5].getChar());
	//         sb.append(screen52.screen[0][6].getChar());
	//
	//         System.out.println("command = " + sb);
	//         int x = 8;
	//         sb.setLength(0);
	//         while (screen52.screen[0][x].getChar() > ' ') {
	//            sb.append(screen52.screen[0][x].getChar());
	//            x++;
	//         }
	//         name = sb.toString();
	//         System.out.println("name = " + name);
	//
	//         sb.setLength(0);
	//         x++;
	//         while (screen52.screen[0][x].getChar() >= ' ') {
	//            sb.append(screen52.screen[0][x].getChar());
	//            x++;
	//         }
	//         argString = sb.toString();
	//         System.out.println("args = " + argString);
	//
	//         sendAidKey(AID_ENTER);
	//
	//         try {
	//
	//            Class c = Class.forName(name);
	//            String args1[] = {argString};
	//            String args2[] = {};
	//
	//            Method m = c.getMethod("main",
	//            new Class[] { args1.getClass() });
	//            m.setAccessible(true);
	//            int mods = m.getModifiers();
	//            if (m.getReturnType() !=
	//                   void.class || !Modifier.isStatic(mods) ||
	//                  !Modifier.isPublic(mods)) {
	//
	//                     throw new NoSuchMethodException("main");
	//                  }
	//            try {
	//               if (argString.length() > 0)
	//                  m.invoke(null, new Object[] { args1 });
	//               else
	//                  m.invoke(null, new Object[] { args2 });
	//            }
	//            catch (IllegalAccessException e) {
	//                 // This should not happen, as we have
	//                 // disabled access checks
	//                  System.out.println("iae " + e.getMessage());
	//
	//            }
	//         }
	//         catch (ClassNotFoundException cnfe) {
	//            System.out.println("cnfe " + cnfe.getMessage());
	//         }
	//         catch (NoSuchMethodException nsmf) {
	//            System.out.println("nsmf " + nsmf.getMessage());
	//         }
	//         catch (InvocationTargetException ite) {
	//            System.out.println("ite " + ite.getMessage());
	//         }
	//   // catch (IllegalAccessException iae) {
	//   // System.out.println("iae " + iae.getMessage());
	//   // }
	//   // catch (InstantiationException ie) {
	//   // System.out.println("ie " + ie.getMessage());
	//   // }
	//   // try {
	//   //
	//   // Runtime rt = Runtime.getRuntime();
	//   // Process proc = rt.exec("notepad");
	//   // int exitVal = proc.exitValue();
	//   // }
	//   // catch (Throwable t) {
	//   //
	//   // t.printStackTrace();
	//   // }
	//      }

	private final void readScreen() throws IOException {

		int rows = screen52.getRows();
		int cols = screen52.getColumns();
		byte abyte0[] = new byte[rows * cols];
		fillScreenArray(abyte0, rows, cols);
		writeGDS(0, 0, abyte0);
		abyte0 = null;
	}

	private final void fillScreenArray(byte[] sa, int rows, int cols) {

		int la = 32;
		int sac = 0;
		int len = rows * cols;

		ScreenPlanes planes = screen52.planes;

		for (int y = 0; y < len; y++) { // save the screen data

			if (planes.isAttributePlace(y)) {
				la = planes.getCharAttr(y);
				sa[sac++] = (byte) la;
			} else {
				if (planes.getCharAttr(y) != la) {
					la = planes.getCharAttr(y);
					sac = max(sac--, 0);
					sa[sac++] = (byte) la;
				}
				//LDC: Check to see if it is an displayable character. If not,
				//  do not convert the character.
				//  The characters on screen are in unicode
				//sa[sac++] =
				// (byte)codePage.uni2ebcdic(screen52.screen[y].getChar());
				char ch = planes.getChar(y);
				byte byteCh = (byte) ch;
				if (isDataUnicode(ch))
					byteCh = codePage.uni2ebcdic(ch);
				sa[min(sac++, len - 1)] = byteCh;
			}
		}
	}

	private final void fillRegenerationBuffer(ByteArrayOutputStream sc, int rows, int cols)
	throws IOException {

		int la = 32;
		int sac = 0;
		int len = rows * cols;

		ScreenPlanes planes = screen52.planes;
		byte[] sa = new byte[len];

		try {
			boolean guiExists = sfParser != null && sfParser.isGuisExists();


			for (int y = 0; y < len; y++) { // save the screen data

				if (guiExists) {

					byte[] guiSeg = sfParser.getSegmentAtPos(y);
					if (guiSeg != null) {
						//log.info(" gui saved at " + y + " - " + screen52.getRow(y) + "," +
						//    screen52.getCol(y));

						byte[] gsa = new byte[sa.length + guiSeg.length + 2];
						System.arraycopy(sa,0,gsa,0,sa.length);
						System.arraycopy(guiSeg,0,gsa,sac+2,guiSeg.length);
						sa = new byte[gsa.length];
						System.arraycopy(gsa,0,sa,0,gsa.length);
						sa[sac++] = (byte)0x04;
						sa[sac++] = (byte)0x11;
						sac += guiSeg.length;
						//y--;
						//		         continue;
					}
				}
				if (planes.isAttributePlace(y)) {
					la = planes.getCharAttr(y);
					sa[sac++] = (byte) la;
				} else {
					if (planes.getCharAttr(y) != la) {
						la = planes.getCharAttr(y);
						sac = max(sac--, 0);
						sa[sac++] = (byte) la;
					}
					//LDC: Check to see if it is an displayable character. If not,
					//  do not convert the character.
					//  The characters on screen are in unicode
					//sa[sac++] =
					// (byte)codePage.uni2ebcdic(screen52.screen[y].getChar());
					char ch = planes.getChar(y);
					byte byteCh = (byte) ch;
					if (isDataUnicode(ch))
						byteCh = codePage.uni2ebcdic(ch);
					sa[min(sac++, len - 1)] = byteCh;
				}
			}
		}
		catch(Exception exc) {

			log.info(exc.getMessage());
			exc.printStackTrace();
		}
		sc.write(sa);
	}

	public final void saveScreen() throws IOException {

		ByteArrayOutputStream sc = new ByteArrayOutputStream();
		sc.write(4);
		sc.write(0x12); // 18
		sc.write(0); // 18
		sc.write(0); // 18

		sc.write((byte) screen52.getRows()); // store the current size
		sc.write((byte) screen52.getColumns()); //    ""

		int cp = screen52.getCurrentPos(); // save off current position
		// fix below submitted by Mitch Blevins
		//int cp = screen52.getScreenFields().getCurrentFieldPos();
		// save off current position
		sc.write((byte) (cp >> 8 & 0xff)); //    ""
		sc.write((byte) (cp & 0xff)); //    ""

		sc.write((byte) (screen52.homePos >> 8 & 0xff)); // save home pos
		sc.write((byte) (screen52.homePos & 0xff)); //    ""

		int rows = screen52.getRows(); // store the current size
		int cols = screen52.getColumns(); //    ""

		//		byte[] sa = new byte[rows * cols];
		fillRegenerationBuffer(sc,rows,cols);
		//		fillScreenArray(sa, rows, cols);
		//
		//		sc.write(sa);
		//		sa = null;
		int sizeFields = screen52.getScreenFields().getSize();
		sc.write((byte) (sizeFields >> 8 & 0xff)); //    ""
		sc.write((byte) (sizeFields & 0xff)); //    ""

		if (sizeFields > 0) {
			int x = 0;
			int s = screen52.getScreenFields().getSize();
			ScreenField sf = null;
			while (x < s) {
				sf = screen52.getScreenFields().getField(x);
				sc.write((byte) sf.getAttr()); // attribute
				int sp = sf.startPos();
				sc.write((byte) (sp >> 8 & 0xff)); //    ""
				sc.write((byte) (sp & 0xff)); //    ""
				if (sf.mdt)
					sc.write((byte) 1);
				else
					sc.write((byte) 0);
				sc.write((byte) (sf.getLength() >> 8 & 0xff)); //    ""
				sc.write((byte) (sf.getLength() & 0xff)); //    ""
				sc.write((byte) sf.getFFW1() & 0xff);
				sc.write((byte) sf.getFFW2() & 0xff);
				sc.write((byte) sf.getFCW1() & 0xff);
				sc.write((byte) sf.getFCW2() & 0xff);
				log.debug("Saved ");
				log.debug(sf.toString());

				x++;
			}
			sf = null;
		}

		// The following two lines of code looks to have caused all sorts of
		//    problems so for now we have commented them out.
		//      screen52.getScreenFields().setCurrentField(null); // set it to null
		// for GC ?
		//      screen52.clearTable();

		try {
			writeGDS(0, 3, sc.toByteArray());
		} catch (IOException ioe) {

			log.warn(ioe.getMessage());
		}

		sc = null;
		log.debug("Save Screen end ");
	}

	/**
	 *
	 * @throws IOException
	 */
	public final void restoreScreen() throws IOException {
		int which = 0;

		ScreenPlanes planes = screen52.planes;

		try {
			log.debug("Restore ");

			bk.getNextByte();
			bk.getNextByte();

			int rows = bk.getNextByte() & 0xff;
			int cols = bk.getNextByte() & 0xff;
			int pos = bk.getNextByte() << 8 & 0xff00; // current position
			pos |= bk.getNextByte() & 0xff;
			int hPos = bk.getNextByte() << 8 & 0xff00; // home position
			hPos |= bk.getNextByte() & 0xff;
			if (rows != screen52.getRows())
				screen52.setRowsCols(rows, cols);
			screen52.clearAll(); // initialize what we currenty have
			if (sfParser != null && sfParser.isGuisExists())
				sfParser.clearGuiStructs();

			int b = 32;
			int la = 32;
			int len = rows * cols;
			for (int y = 0; y < len; y++) {

				b = bk.getNextByte();
				if (b == 0x04) {

					log.info(" gui restored at " + y + " - " + screen52.getRow(y) + "," +
							screen52.getCol(y));
					int command = bk.getNextByte();
					byte[] seg = bk.getSegment();

					if (seg.length > 0) {
						screen52.goto_XY(y);
						sfParser.parseWriteToDisplayStructuredField(seg);
					}
					y--;
					//				      screen52.goto_XY(y);
				}
				else {
					//				b = bk.getNextByte();
					if (planes.isUseGui(y))
						continue;
					if (isAttribute(b)) {
						planes.setScreenCharAndAttr(y, planes.getChar(y), b, true);
						la = b;

					}
					else {
						//LDC - 12/02/2003 - Check to see if it is an displayable
						// character. If not,
						//  do not convert the character.
						//  The characters on screen are in unicode
						char ch = (char) b;
						if (isDataEBCDIC(b))
							ch = codePage.ebcdic2uni(b);

						planes.setScreenCharAndAttr(y, ch, la, false);
					}
				}
			}

			int numFields = bk.getNextByte() << 8 & 0xff00;
			numFields |= bk.getNextByte() & 0xff;
			log.debug("number of fields " + numFields);

			if (numFields > 0) {
				int x = 0;
				int attr = 0;
				int fPos = 0;
				int fLen = 0;
				int ffw1 = 0;
				int ffw2 = 0;
				int fcw1 = 0;
				int fcw2 = 0;
				boolean mdt = false;

				ScreenField sf = null;
				while (x < numFields) {

					attr = bk.getNextByte();
					fPos = bk.getNextByte() << 8 & 0xff00;
					fPos |= bk.getNextByte() & 0xff;
					if (bk.getNextByte() == 1)
						mdt = true;
					else
						mdt = false;
					fLen = bk.getNextByte() << 8 & 0xff00;
					fLen |= bk.getNextByte() & 0xff;
					ffw1 = bk.getNextByte();
					ffw2 = bk.getNextByte();
					fcw1 = bk.getNextByte();
					fcw2 = bk.getNextByte();

					sf = screen52.getScreenFields().setField(attr,
							screen52.getRow(fPos), screen52.getCol(fPos), fLen,
							ffw1, ffw2, fcw1, fcw2);

					while (fLen-- > 0) {

						// now we set the field plane attributes
						planes.setScreenFieldAttr(fPos++,ffw1);

					}

					if (mdt) {
						sf.setMDT();
						screen52.getScreenFields().setMasterMDT();
					}
					if (log.isDebugEnabled()) {
						log.debug("/nRestored ");
						log.debug(sf.toString());
					}
					x++;
				}
			}

			//  Redraw the gui fields if we are in gui mode
			if (screen52.isUsingGuiInterface())
				screen52.drawFields();

			screen52.restoreScreen(); // display the screen

			//  The position was saved with currentPos which 1,1 offset of the
			//     screen position.
			//  The setPendingInsert is the where the cursor position will be
			//  displayed after the restore.
			screen52.setPendingInsert(true, screen52.getRow(pos + cols), screen52
					.getCol(pos + cols));
			//  We need to offset the pos by -1 since the position is 1,1 based
			//    and the goto_XY is 0,0 based.
			screen52.goto_XY(pos - 1);
			screen52.isInField();
			//			//  Redraw the gui fields if we are in gui mode
			//			if (screen52.isUsingGuiInterface())
			//				screen52.drawFields();
		} catch (Exception e) {
			log.warn("error restoring screen " + which + " with "
					+ e.getMessage());
		}
	}

	public final boolean waitingForInput() {

		return waitingForInput;
	}

	private void parseIncoming() {

		boolean done = false;
		boolean error = false;

		try {
			while (bk.hasNext() && !done) {
				byte b = bk.getNextByte();

				switch (b) {
				case 0:
				case 1:
					break;
				case CMD_SAVE_SCREEN: // 0x02 2 Save Screen
				case 3: // 0x03 3 Save Partial Screen
					log.debug("save screen partial");
					saveScreen();
					break;

				case ESC: // ESCAPE
					break;
				case 7: // audible bell
					controller.signalBell();
					bk.getNextByte();
					bk.getNextByte();
					break;
				case CMD_WRITE_TO_DISPLAY: // 0x11 17 write to display
					error = writeToDisplay(true);
					// WVL - LDC : TR.000300 : Callback scenario from 5250
					// Only scan when WRITE_TO_DISPLAY operation (i.e. refill
					// screen buffer)
					// has been issued!
					if (scan)
						scan();

					break;
				case CMD_RESTORE_SCREEN: // 0x12 18 Restore Screen
				case 13: // 0x13 19 Restore Partial Screen
					log.debug("restore screen partial");
					restoreScreen();
					break;

				case CMD_CLEAR_UNIT_ALTERNATE: // 0x20 32 clear unit alternate
					int param = bk.getNextByte();
					if (param != 0) {
						log.debug(" clear unit alternate error "
								+ Integer.toHexString(param));
						sendNegResponse(NR_REQUEST_ERROR, 03, 01, 05,
						" clear unit alternate not supported");
						done = true;
					} else {
						if (screen52.getRows() != 27)
							screen52.setRowsCols(27, 132);

						screen52.clearAll();
						if (sfParser != null && sfParser.isGuisExists())
							sfParser.clearGuiStructs();


					}
					break;

				case CMD_WRITE_ERROR_CODE: // 0x21 33 Write Error Code
					writeErrorCode();
					error = writeToDisplay(false);
					break;
				case CMD_WRITE_ERROR_CODE_TO_WINDOW: // 0x22 34
					// Write Error Code to window
					writeErrorCodeToWindow();
					error = writeToDisplay(false);
					break;

				case CMD_READ_SCREEN_IMMEDIATE: // 0x62 98
				case CMD_READ_SCREEN_TO_PRINT: // 0x66 102 read screen to print
					readScreen();
					break;

				case CMD_CLEAR_UNIT: // 64 0x40 clear unit
					if (screen52.getRows() != 24)
						screen52.setRowsCols(24, 80);
					screen52.clearAll();
					if (sfParser != null && sfParser.isGuisExists())
						sfParser.clearGuiStructs();

					break;

				case CMD_CLEAR_FORMAT_TABLE: // 80 0x50 Clear format table
					screen52.clearTable();
					break;

				case CMD_READ_INPUT_FIELDS: //0x42 66 read input fields
				case CMD_READ_MDT_FIELDS: // 0x52 82 read MDT Fields
					bk.getNextByte();
					bk.getNextByte();
					readType = b;
					screen52.goHome();
					// do nothing with the cursor here it is taken care of
					//   in the main loop.
					//////////////// screen52.setCursorOn();
					waitingForInput = true;
					pendingUnlock = true;
					//                  screen52.setKeyboardLocked(false);
					break;
				case CMD_READ_MDT_IMMEDIATE_ALT: // 0x53 83
					readType = b;
					//                  screen52.goHome();
					//                  waitingForInput = true;
					//                  screen52.setKeyboardLocked(false);
					readImmediate(readType);
					break;
				case CMD_WRITE_STRUCTURED_FIELD: // 243 0xF3 -13 Write
					// structured field
					writeStructuredField();
					break;
				case CMD_ROLL: // 0x23 35 Roll Not sure what it does right now
					int updown = bk.getNextByte();
					int topline = bk.getNextByte();
					int bottomline = bk.getNextByte();
					screen52.rollScreen(updown, topline, bottomline);
					break;

				default:
					done = true;
					sendNegResponse(NR_REQUEST_ERROR, 03, 01, 01,
					"parseIncoming");
					break;
				}

				if (error)
					done = true;
			}
			//       BEGIN FRAMEWORK
			//  I took this out for debugging a problem
			//			ScreenField[] a = this.screen52.getScreenFields().getFields();
			//			if (log.isDebugEnabled()) {
			//				for (int x = 0; x < a.length; x++) {
			//					log.debug(a[x].toString());
			//				}
			//			}
			//
			//			String strokes = this.screen52.getKeys();
			//			if (!strokes.equals("")) {
			//				Tn5250jKeyEvents e = new Tn5250jKeyEvents(this.screen52,
			//						strokes);
			//				//from the previous screen.
			//				Tn5250jController.getCurrent().handleEvent(e);
			//			}
			//
			//			Tn5250jEvent event = new Tn5250jEvent(screen52);
			//			Tn5250jController.getCurrent().handleEvent(event);
			//
			//			//END FRAMEWORK
		} catch (Exception exc) {
			log.warn("incoming " + exc.getMessage());
		}
		;
	}

	/**
	 * This routine handles sending negative responses back to the host.
	 *
	 * You can find a description of the types of responses to be sent back by
	 * looking at section 12.4 of the 5250 Functions Reference manual
	 *
	 *
	 * @param cat
	 * @param modifier
	 * @param uByte1
	 * @param uByte2
	 * @param from
	 *
	 */
	protected void sendNegResponse(int cat, int modifier, int uByte1,
			int uByte2, String from) {

		try {

			int os = bk.getByteOffset(-1) & 0xf0;
			int cp = (bk.getCurrentPos() - 1);
			log.info("invalid " + from + " command " + os
					+ " at pos " + cp);
		} catch (Exception e) {

			log.warn("Send Negative Response error " + e.getMessage());
		}

		baosp.write(cat);
		baosp.write(modifier);
		baosp.write(uByte1);
		baosp.write(uByte2);

		try {
			writeGDS(128, 0, baosp.toByteArray());
		} catch (IOException ioe) {

			log.warn(ioe.getMessage());
		}
		baosp.reset();

	}

	public void sendNegResponse2(int ec) {

		screen52.setPrehelpState(true, true, false);
		baosp.write(0x00);
		baosp.write(ec);

		try {
			writeGDS(1, 0, baosp.toByteArray());
		} catch (IOException ioe) {

			log.warn(ioe.getMessage());
		}

		baosp.reset();
	}

	private boolean writeToDisplay(boolean controlsExist) {

		boolean error = false;
		boolean done = false;
		int attr;
		byte control0 = 0;
		byte control1 = 0;
		int saRows = screen52.getRows();
		int saCols = screen52.getColumns();

		try {
			if (controlsExist) {
				control0 = bk.getNextByte();
				control1 = bk.getNextByte();
				processCC0(control0);
			}
			while (bk.hasNext() && !done) {
				//            pos = bk.getCurrentPos();

				//            int rowc = screen52.getCurrentRow();
				//            int colc = screen52.getCurrentCol();

				byte bytebk = bk.getNextByte();

				switch (bytebk) {

				case 1: // SOH - Start of Header Order
					log.debug("SOH - Start of Header Order");
					error = processSOH();

					break;
				case 02: // RA - Repeat to address
					log.debug("RA - Repeat to address");
					int row = screen52.getCurrentRow();
					int col = screen52.getCurrentCol();

					int toRow = bk.getNextByte();
					int toCol = bk.getNextByte() & 0xff;
					if (toRow >= row) {
						int repeat = bk.getNextByte();

						// a little intelligence here I hope
						if (row == 1 && col == 2 && toRow == screen52.getRows()
								&& toCol == screen52.getColumns())

							screen52.clearScreen();
						else {
							if (repeat != 0) {
								//LDC - 13/02/2003 - convert it to unicode
								repeat = codePage.ebcdic2uni(repeat);
								//repeat = getASCIIChar(repeat);
							}

							int times = ((toRow * screen52.getColumns()) + toCol)
							- ((row * screen52.getColumns()) + col);
							while (times-- >= 0) {
								screen52.setChar(repeat);
							}

						}
					} else {
						sendNegResponse(NR_REQUEST_ERROR, 0x05, 0x01, 0x23,
						" RA invalid");
						error = true;
					}
					break;

				case 03: // EA - Erase to address
					log.debug("EA - Erase to address");
					int EArow = screen52.getCurrentRow();
					int EAcol = screen52.getCurrentCol();

					int toEARow = bk.getNextByte();
					int toEACol = bk.getNextByte() & 0xff;
					int EALength = bk.getNextByte() & 0xff;
					while (--EALength > 0) {

						bk.getNextByte();

					}
					char EAAttr = (char) 0;

					// a little intelligence here I hope
					if (EArow == 1 && EAcol == 2
							&& toEARow == screen52.getRows()
							&& toEACol == screen52.getColumns())

						screen52.clearScreen();
					else {
						int times = ((toEARow * screen52.getColumns()) + toEACol)
						- ((EArow * screen52.getColumns()) + EAcol);
						while (times-- >= 0) {
							screen52.setChar(EAAttr);
						}
					}
					break;
				case 04: // Command - Escape
					log.debug("Command - Escape");
					done = true;
					break;

				case 16: // TD - Transparent Data
					log.debug("TD - Transparent Data");
					int j = (bk.getNextByte() & 0xff) << 8 | bk.getNextByte()
					& 0xff; // length
					break;

				case 17: // SBA - set buffer address order (row column)
					log.debug("SBA - set buffer address order (row column)");
					int saRow = bk.getNextByte();
					int saCol = bk.getNextByte() & 0xff;
					// make sure it is in bounds
					if (saRow >= 0 && saRow <= screen52.getRows() && saCol >= 0
							&& saCol <= screen52.getColumns()) {
						screen52.setCursor(saRow, saCol); // now set screen
						// position for output

					} else {

						sendNegResponse(NR_REQUEST_ERROR, 0x05, 0x01, 0x22,
								"invalid row/col order" + " saRow = " + saRow
								+ " saRows = " + screen52.getRows()
								+ " saCol = " + saCol);

						error = true;

					}
					break;

				case 18: // WEA - Extended Attribute
					log.debug("WEA - Extended Attribute");
					bk.getNextByte();
					bk.getNextByte();
					break;

				case 19: // IC - Insert Cursor
					log.debug("IC - Insert Cursor");
					int icX = bk.getNextByte();
					int icY = bk.getNextByte() & 0xff;
					if (icX >= 0 && icX <= saRows && icY >= 0 && icY <= saCols) {

						log.debug(" IC " + icX + " " + icY);
						screen52.setPendingInsert(true, icX, icY);
					} else {
						sendNegResponse(NR_REQUEST_ERROR, 0x05, 0x01, 0x22,
						" IC/IM position invalid ");
						error = true;
					}

					break;

				case 20: // MC - Move Cursor
					log.debug("MC - Move Cursor");
					int imcX = bk.getNextByte();
					int imcY = bk.getNextByte() & 0xff;
					if (imcX >= 0 && imcX <= saRows && imcY >= 0
							&& imcY <= saCols) {

						log.debug(" MC " + imcX + " " + imcY);
						screen52.setPendingInsert(false, imcX, imcY);
					} else {
						sendNegResponse(NR_REQUEST_ERROR, 0x05, 0x01, 0x22,
						" IC/IM position invalid ");
						error = true;
					}

					break;

				case 21: // WTDSF - Write To Display Structured Field order
					log
					.debug("WTDSF - Write To Display Structured Field order");
					byte[] seg = bk.getSegment();
					error = sfParser.parseWriteToDisplayStructuredField(seg);

					//                  error = writeToDisplayStructuredField();
					break;

				case 29: // SF - Start of Field
					log.debug("SF - Start of Field");
					int fcw1 = 0;
					int fcw2 = 0;
					int ffw1 = 0;
					int ffw0 = bk.getNextByte() & 0xff; // FFW

					if ((ffw0 & 0x40) == 0x40) {
						ffw1 = bk.getNextByte() & 0xff; // FFW 1

						fcw1 = bk.getNextByte() & 0xff; // check for field
						// control word

						// check if the first fcw1 is an 0x81 if it is then get
						// the
						// next pair for checking
						if (fcw1 == 0x81) {
							bk.getNextByte();
							fcw1 = bk.getNextByte() & 0xff; // check for field
							// control word
						}

						if (!isAttribute(fcw1)) {

							fcw2 = bk.getNextByte() & 0xff; // FCW 2
							attr = bk.getNextByte() & 0xff; // attribute field

							while (!isAttribute(attr)) {
								log.info(Integer.toHexString(fcw1) + " "
										+ Integer.toHexString(fcw2)
										+ " ");
								log.info(Integer.toHexString(attr)
										+ " "
										+ Integer.toHexString(bk
												.getNextByte() & 0xff));
								//                           bk.getNextByte();
								attr = bk.getNextByte() & 0xff; // attribute
								// field
							}
						} else {
							attr = fcw1; // attribute of field
							fcw1 = 0;
						}
					} else {
						attr = ffw0;
					}

					int fLength = (bk.getNextByte() & 0xff) << 8
					| bk.getNextByte() & 0xff;
					screen52.addField(attr, fLength, ffw0, ffw1, fcw1, fcw2);

					break;
					// WVL - LDC : 05/08/2005 : TFX.006253 - Support STRPCCMD
				case -128: //STRPCCMD
					//          if (screen52.getCurrentPos() == 82) {
					log.debug("STRPCCMD got a -128 command at " + screen52.getCurrentPos());
					StringBuffer value = new StringBuffer();
					int b;
					char c;
					int[] pco = new int[9];
					int[] pcoOk = {0xfc, 0xd7, 0xc3, 0xd6, 0x40, 0x83, 0x80, 0xa1, 0x80};

					for (int i = 0; i < 9; i++)
					{
						b = bk.getNextByte();
						pco[i] = ((b & 0xff));
						c = codePage.ebcdic2uni(b);
						value.append(c);
					}

					// Check "PCO-String"
					if (Arrays.equals(pco, pcoOk)) {
						strpccmd = true;
					}
					// we return in the stream to have all chars
					// arrive at the screen for later processing
					for (int i = 0; i < 9; i++)
						bk.setPrevByte();
					//}
					// no break: so every chars arrives
					// on the screen for later parsing
					//break;

				default: // all others must be output to screen
					log.debug("all others must be output to screen");
					byte byte0 = bk.getByteOffset(-1);
					if (isAttribute(byte0)) {
						screen52.setAttr(byte0);
					} else {
						if (!screen52.isStatusErrorCode()) {
							if (!isDataEBCDIC(byte0)) {
								//                           if (byte0 == 255) {
								//                              sendNegResponse(NR_REQUEST_ERROR,0x05,0x01,0x42,
								//                              " Attempt to send FF to screen");
								//                           }
								//                           else

								screen52.setChar(byte0);
							} else
								//LDC - 13/02/2003 - Convert it to unicode
								//screen52.setChar(getASCIIChar(byte0));
								screen52.setChar(codePage.ebcdic2uni(byte0));
						} else {
							if (byte0 == 0)
								screen52.setChar(byte0);
							else
								//LDC - 13/02/2003 - Convert it to unicode
								//screen52.setChar(getASCIIChar(byte0));
								screen52.setChar(codePage.ebcdic2uni(byte0));
						}
					}

					break;
				}

				if (error)
					done = true;
			}
		}

		catch (Exception e) {
			log.warn("write to display " + e.getMessage());
			e.printStackTrace();
		}
		;

		processCC1(control1);

		return error;

	}

	private boolean processSOH() throws Exception {

		int l = bk.getNextByte(); // length
		log.debug(" byte 0 " + l);

		if (l > 0 && l <= 7) {
			bk.getNextByte(); // flag byte 2
			bk.getNextByte(); // reserved
			bk.getNextByte(); // resequence fields

			screen52.clearTable();

			// well that is the first time I have seen this. This fixes a
			// problem
			// with S/36 command line. Finally got it.
			if (l <= 3)
				return false;

			screen52.setErrorLine(bk.getNextByte()); // error row

			int byte1 = 0;
			if (l >= 5) {
				byte1 = bk.getNextByte();
				dataIncluded[23] = (byte1 & 0x80) == 0x80;
				dataIncluded[22] = (byte1 & 0x40) == 0x40;
				dataIncluded[21] = (byte1 & 0x20) == 0x20;
				dataIncluded[20] = (byte1 & 0x10) == 0x10;
				dataIncluded[19] = (byte1 & 0x8) == 0x8;
				dataIncluded[18] = (byte1 & 0x4) == 0x4;
				dataIncluded[17] = (byte1 & 0x2) == 0x2;
				dataIncluded[16] = (byte1 & 0x1) == 0x1;
			}

			if (l >= 6) {
				byte1 = bk.getNextByte();
				dataIncluded[15] = (byte1 & 0x80) == 0x80;
				dataIncluded[14] = (byte1 & 0x40) == 0x40;
				dataIncluded[13] = (byte1 & 0x20) == 0x20;
				dataIncluded[12] = (byte1 & 0x10) == 0x10;
				dataIncluded[11] = (byte1 & 0x8) == 0x8;
				dataIncluded[10] = (byte1 & 0x4) == 0x4;
				dataIncluded[9] = (byte1 & 0x2) == 0x2;
				dataIncluded[8] = (byte1 & 0x1) == 0x1;
			}

			if (l >= 7) {
				byte1 = bk.getNextByte();
				dataIncluded[7] = (byte1 & 0x80) == 0x80;
				dataIncluded[6] = (byte1 & 0x40) == 0x40;
				dataIncluded[5] = (byte1 & 0x20) == 0x20;
				dataIncluded[4] = (byte1 & 0x10) == 0x10;
				dataIncluded[3] = (byte1 & 0x8) == 0x8;
				dataIncluded[2] = (byte1 & 0x4) == 0x4;
				dataIncluded[1] = (byte1 & 0x2) == 0x2;
				dataIncluded[0] = (byte1 & 0x1) == 0x1;
			}
			return false;
		} else {
			sendNegResponse(NR_REQUEST_ERROR, 0x05, 0x01, 0x2B,
			"invalid SOH length");
			return true;
		}

	}

	private void processCC0(byte byte0) {
		log.debug(" Control byte0 " + Integer.toBinaryString(byte0 & 0xff));
		boolean lockKeyboard = true;
		boolean resetMDT = false;
		boolean resetMDTAll = false;
		boolean nullMDT = false;
		boolean nullAll = false;

		// Bits 3 to 6 are reserved and should be set to '0000'
		// 0xE0 = '11100000' - only the first 3 bits are tested
		if ((byte0 & 0xE0) == 0x00) {
			lockKeyboard = false;
		}

		// '00100000' = 0x20 /32 -- just lock keyboard
		// '01000000' = 0x40 /64
		// '01100000' = 0x60 /96
		// '10000000' = 0x80 /128
		// '10100000' = 0xA0 /160
		// '11000000' = 0xC0 /192
		// '11100000' = 0xE0 /224

		switch (byte0 & 0xE0) {

		case 0x40:
			resetMDT = true;
			break;
		case 0x60:
			resetMDTAll = true;
			break;
		case 0x80:
			nullMDT = true;
			break;
		case 0xA0:
			resetMDT = true;
			nullAll = true;
			break;
		case 0xC0:
			resetMDT = true;
			nullMDT = true;
			break;

		case 0xE0:
			resetMDTAll = true;
			nullAll = true;
			break;

		}

		if (lockKeyboard) {
			screen52.getOIA().setKeyBoardLocked(true);
			pendingUnlock = false;
		} else
			pendingUnlock = false;

		if (resetMDT || resetMDTAll || nullMDT || nullAll) {
			ScreenField sf;

			int f = screen52.getScreenFields().getSize();
			for (int x = 0; x < f; x++) {
				sf = screen52.getScreenFields().getField(x);

				if (!sf.isBypassField()) {
					if ((nullMDT && sf.mdt) || nullAll) {
						sf.setFieldChar((char) 0x0);
						screen52.drawField(sf);
					}
				}
				if (resetMDTAll || (resetMDT && !sf.isBypassField()))
					sf.resetMDT();

			}
			sf = null;
		}

	}

	private void processCC1(byte byte1) {
		log.debug(" Control byte1 " + Integer.toBinaryString(byte1 & 0xff));

		if ((byte1 & 0x04) == 0x04) {
			controller.signalBell();
		}
		if ((byte1 & 0x02) == 0x02) {
			screen52.getOIA().setMessageLightOff();
		}
		if ((byte1 & 0x01) == 0x01) {
			screen52.getOIA().setMessageLightOn();
		}

		if ((byte1 & 0x01) == 0x01 && (byte1 & 0x02) == 0x02) {
			screen52.getOIA().setMessageLightOn();
		}

		// reset blinking cursor seems to control whether to set or not set the
		// the cursor position. No documentation for this just testing and
		// looking at the bit settings of this field. This was a pain in the
		// ass!
		//
		// if it is off '0' then keep existing cursor positioning information
		// if it is on '1' then reset the cursor positioning information
		// *** Note *** unless we receive bit 4 on at the same time
		// this seems to work so far
		if ((byte1 & 0x20) == 0x20 && (byte1 & 0x08) == 0x00) {
			screen52.setPendingInsert(false);
			log.debug(" WTD position no move");
		} else {

			screen52.setPendingInsert(true);
			log.debug(" WTD position move to home" + screen52.homePos + " row "
					+ screen52.getRow(screen52.homePos) + " col "
					+ screen52.getCol(screen52.homePos));

		}
		// in enhanced mode we sometimes only receive bit 6 turned on which
		// is reset blinking cursor
		if ((byte1 & 0x20) == 0x20 && enhanced) {
			cursorOn = true;
		}

		if (!screen52.isStatusErrorCode() && (byte1 & 0x08) == 0x08) {

			//         screen52.setStatus(screen52.STATUS_SYSTEM,screen52.STATUS_VALUE_OFF,null);
			cursorOn = true;
		}

		if ((byte1 & 0x20) == 0x20 && (byte1 & 0x08) == 0x00) {
			screen52.setPendingInsert(false, 1, 1);
		}

	}

	private boolean isAttribute(int byte0) {
		int byte1 = byte0 & 0xff;
		return (byte1 & 0xe0) == 0x20;
	}

	//LDC - 12/02/2003 - Function name changed from isData to isDataEBCDIC
	private boolean isDataEBCDIC(int byte0) {
		int byte1 = byte0 & 0xff;
		// here it should always be less than 255
		if (byte1 >= 64 && byte1 < 255)

			return true;
		else
			return false;

	}

	//LDC - 12/02/2003 - Test if the unicode character is a displayable
	// character.
	//  The first 32 characters are non displayable characters
	//  This is normally the inverse of isDataEBCDIC (That's why there is a
	//  check on 255 -> 0xFFFF
	private boolean isDataUnicode(int byte0) {
		return (((byte0 < 0) || (byte0 >= 32)) && (byte0 != 0xFFFF));
	}

	private void writeStructuredField() {

		boolean done = false;
		try {
			int length = ((bk.getNextByte() & 0xff) << 8 | (bk.getNextByte() & 0xff));
			while (bk.hasNext() && !done) {
				switch (bk.getNextByte()) {

				case -39: // SOH - Start of Header Order

					switch (bk.getNextByte()) {
					case 112: // 5250 Query
						bk.getNextByte(); // get null required field
						sendQueryResponse();
						break;
					default:
						log.debug("invalid structured field sub command "
								+ bk.getByteOffset(-1));
						break;
					}
					break;
				default:
					log.debug("invalid structured field command "
							+ bk.getByteOffset(-1));
					break;
				}
			}
		} catch (Exception e) {
		}
		;

	}

	private final void writeErrorCode() throws Exception {
		screen52.setCursor(screen52.getErrorLine(), 1); // Skip the control byte
		screen52.setStatus(Screen5250.STATUS_ERROR_CODE,
				Screen5250.STATUS_VALUE_ON, null);
		screen52.saveErrorLine();
		cursorOn = true;

	}

	private final void writeErrorCodeToWindow() throws Exception {
		int fromCol = bk.getNextByte() & 0xff; // from column
		int toCol = bk.getNextByte() & 0xff; // to column
		screen52.setCursor(screen52.getErrorLine(), fromCol); // Skip the control
		// byte
		screen52.setStatus(Screen5250.STATUS_ERROR_CODE,
				Screen5250.STATUS_VALUE_ON, null);
		screen52.saveErrorLine();
		cursorOn = true;

	}

	/**
	 * Method sendQueryResponse
	 *
	 * The query command is used to obtain information about the capabilities of
	 * the 5250 display.
	 *
	 * The Query command must follow an Escape (0x04) and Write Structured Field
	 * command (0xF3).
	 *
	 * This section is modeled after the rfc1205 - 5250 Telnet Interface section
	 * 5.3
	 *
	 * @throws IOException
	 */
	private final void sendQueryResponse() throws IOException {

		log.info("sending query response");
		byte abyte0[] = new byte[64];
		abyte0[0] = 0; // Cursor Row/column (set to zero)
		abyte0[1] = 0; //           ""
		abyte0[2] = -120; // X'88' inbound write structure Field aid
		if (enhanced == true) {
			abyte0[3] = 0; // 0x003D (61) length of query response
			abyte0[4] = 64; //       "" see note below ?????????
		} else {
			abyte0[3] = 0; // 0x003A (58) length of query response
			abyte0[4] = 58; //       ""
			//  the length between 58 and 64 seems to cause
			//  different formatting codes to be sent from
			//  the host ???????????????? why ???????
			//    Well the why can be found in the manual if
			//       read a little more ;-)
		}
		abyte0[5] = -39; // command class 0xD9
		abyte0[6] = 112; // Command type query 0x70
		abyte0[7] = -128; // 0x80 Flag byte
		abyte0[8] = 6; // Controller Hardware Class
		abyte0[9] = 0; // 0x0600 - Other WSF or another 5250 Emulator
		abyte0[10] = 1; // Controller Code Level
		abyte0[11] = 1; //    Version 1 Rel 1.0
		abyte0[12] = 0; //       ""

		abyte0[13] = 0; // 13 - 28 are reserved so set to 0x00
		abyte0[14] = 0; //       ""
		abyte0[15] = 0; //       ""
		abyte0[16] = 0; //       ""
		abyte0[17] = 0; //       ""
		abyte0[18] = 0; //       ""
		abyte0[19] = 0; //       ""
		abyte0[20] = 0; //       ""
		abyte0[21] = 0; //       ""
		abyte0[22] = 0; //       ""
		abyte0[23] = 0; //       ""
		abyte0[24] = 0; //       ""
		abyte0[25] = 0; //       ""
		abyte0[26] = 0; //       ""
		abyte0[27] = 0; //       ""
		abyte0[28] = 0; //       ""
		abyte0[29] = 1; // Device type - 0x01 5250 Emulator
		abyte0[30] = codePage.uni2ebcdic('5'); // Device type character
		abyte0[31] = codePage.uni2ebcdic('2'); //          ""
		abyte0[32] = codePage.uni2ebcdic('5'); //          ""
		abyte0[33] = codePage.uni2ebcdic('1'); //          ""
		abyte0[34] = codePage.uni2ebcdic('0'); //          ""
		abyte0[35] = codePage.uni2ebcdic('1'); //          ""
		abyte0[36] = codePage.uni2ebcdic('1'); //          ""

		abyte0[37] = 2; // Keyboard Id - 0x02 Standard Keyboard
		abyte0[38] = 0; // extended keyboard id
		abyte0[39] = 0; // reserved

		abyte0[40] = 0; // 40 - 43 Display Serial Number
		abyte0[41] = 36; //
		abyte0[42] = 36; //
		abyte0[43] = 0; //

		abyte0[44] = 1; // Maximum number of display fields - 256
		abyte0[45] = 0; // 0x0100
		abyte0[46] = 0; // 46 -48 Reserved set to 0x00
		abyte0[47] = 0;
		abyte0[48] = 0;
		abyte0[49] = 1; // 49 - 53 Controller Display Capability
		abyte0[50] = 17; //      see rfc - tired of typing :-)
		abyte0[51] = 0; //          ""
		abyte0[52] = 0; //          ""

		//  53
		//    Bit 0-2: B'000' - no graphics capability
		//             B'001' - 5292-2 style graphics
		//    Bit 3-7: B '00000' = reserved (it seems for Client access)

		if (enhanced == true) {
			//         abyte0[53] = 0x5E; // 0x5E turns on ehnhanced mode
			//         abyte0[53] = 0x27; // 0x5E turns on ehnhanced mode
			abyte0[53] = 0x7; //  0x5E turns on ehnhanced mode
			log.info("enhanced options");
		} else
			abyte0[53] = 0x0; //  0x0 is normal emulation

		abyte0[54] = 24; // 54 - 60 Reserved set to 0x00
		//  54 - I found out is used for enhanced user
		//       interface level 3. Bit 4 allows headers
		//       and footers for windows
		abyte0[54] = 8; // 54 - 60 Reserved set to 0x00
		//  54 - I found out is used for enhanced user
		//       interface level 3. Bit 4 allows headers
		//       and footers for windows
		abyte0[55] = 0;
		abyte0[56] = 0;
		abyte0[57] = 0;
		abyte0[58] = 0;
		abyte0[59] = 0;
		abyte0[60] = 0;
		abyte0[61] = 0; // gridlines are not supported
		abyte0[62] = 0; // gridlines are not supported
		abyte0[63] = 0;
		writeGDS(0, 0, abyte0); // now tell them about us
		abyte0 = null;

	}

	protected final boolean negotiate(byte abyte0[]) throws IOException {
		int i = 0;


		// from server negotiations
		if(abyte0[i] == IAC) { // -1

			while(i < abyte0.length && abyte0[i++] == -1)
				//         while(i < abyte0.length && (abyte0[i] == -1 || abyte0[i++] == 0x20))
				switch(abyte0[i++]) {

				// we will not worry about what it WONT do
				case WONT:            // -4
				default:
					break;

				case DO: //-3

					// not sure why but since moving to V5R2 we are receiving a
					//   DO with no option when connecting a second session with
					//   device name.  Can not find the cause at all.  If anybody
					//   is interested please debug this until then this works.
					if (i < abyte0.length) {
						switch(abyte0[i]) {
						case TERMINAL_TYPE: // 24
							baosp.write(IAC);
							baosp.write(WILL);
							baosp.write(TERMINAL_TYPE);
							writeByte(baosp.toByteArray());
							baosp.reset();

							break;

						case OPT_END_OF_RECORD: // 25

							baosp.write(IAC);
							baosp.write(WILL);
							baosp.write(OPT_END_OF_RECORD);
							writeByte(baosp.toByteArray());
							baosp.reset();
							break;

						case TRANSMIT_BINARY: // 0

							baosp.write(IAC);
							baosp.write(WILL);
							baosp.write(TRANSMIT_BINARY);
							writeByte(baosp.toByteArray());
							baosp.reset();

							break;

						case TIMING_MARK: // 6   rfc860
							//                        System.out.println("Timing Mark Received and notifying " +
							//                        "the server that we will not do it");
							baosp.write(IAC);
							baosp.write(WONT);
							baosp.write(TIMING_MARK);
							writeByte(baosp.toByteArray());
							baosp.reset();

							break;

						case NEW_ENVIRONMENT: // 39 rfc1572, rfc4777
							// allways send new environment vars ...
							baosp.write(IAC);
							baosp.write(WILL);
							baosp.write(NEW_ENVIRONMENT);
							writeByte(baosp.toByteArray());
							baosp.reset();
							break;

						default:  // every thing else we will not do at this time
							baosp.write(IAC);
							baosp.write(WONT);
							baosp.write(abyte0[i]); // either
							writeByte(baosp.toByteArray());
							baosp.reset();

							break;
						}
					}

					i++;
					break;

				case WILL:

					switch(abyte0[i]) {
					case OPT_END_OF_RECORD: // 25
						baosp.write(IAC);
						baosp.write(DO);
						baosp.write(OPT_END_OF_RECORD);
						writeByte(baosp.toByteArray());
						baosp.reset();

						break;

					case TRANSMIT_BINARY: // '\0'
						baosp.write(IAC);
						baosp.write(DO);
						baosp.write(TRANSMIT_BINARY);
						writeByte(baosp.toByteArray());
						baosp.reset();

						break;
					}
					i++;
					break;

				case SB: // -6

					if(abyte0[i] == NEW_ENVIRONMENT && abyte0[i + 1] == 1) {
						negNewEnvironment();

						while (++i < abyte0.length && abyte0[i + 1] != IAC);
					}

					if(abyte0[i] == TERMINAL_TYPE && abyte0[i + 1] == 1) {
						baosp.write(IAC);
						baosp.write(SB);
						baosp.write(TERMINAL_TYPE);
						baosp.write(QUAL_IS);
						if(!support132)
							baosp.write("IBM-3179-2".getBytes());
						else
							baosp.write("IBM-3477-FC".getBytes());
						baosp.write(IAC);
						baosp.write(SE);
						writeByte(baosp.toByteArray());
						baosp.reset();

						i++;
					}
					i++;
					break;
				}
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Negotiate new environment string for device name
	 *
	 * @throws IOException
	 */
	private void negNewEnvironment() throws IOException {

		baosp.write(IAC);
		baosp.write(SB);
		baosp.write(NEW_ENVIRONMENT);
		baosp.write(IS);

		// http://tools.ietf.org/html/rfc4777

		if (kbdTypesCodePage != null) {
			baosp.write(USERVAR);
			baosp.write("KBDTYPE".getBytes());
			baosp.write(VALUE);
			baosp.write(kbdTypesCodePage.kbdType.getBytes());

			baosp.write(USERVAR);
			baosp.write("CODEPAGE".getBytes());
			baosp.write(VALUE);
			baosp.write(kbdTypesCodePage.codepage.getBytes());

			baosp.write(USERVAR);
			baosp.write("CHARSET".getBytes());
			baosp.write(VALUE);
			baosp.write(kbdTypesCodePage.charset.getBytes());
		}

		if (devName != null) {
			baosp.write(USERVAR);

			baosp.write("DEVNAME".getBytes());

			baosp.write(VALUE);

			baosp.write(negDeviceName().getBytes());
		}

		if (user != null) {

			baosp.write(VAR);
			baosp.write("USER".getBytes());
			baosp.write(VALUE);
			baosp.write(user.getBytes());

			if (password != null) {
				baosp.write(USERVAR);
				baosp.write("IBMRSEED".getBytes());
				baosp.write(VALUE);
				baosp.write(NEGOTIATE_ESC);
				baosp.write(0x0);
				baosp.write(0x0);
				baosp.write(0x0);
				baosp.write(0x0);
				baosp.write(0x0);
				baosp.write(0x0);
				baosp.write(0x0);
				baosp.write(0x0);
				baosp.write(USERVAR);
				baosp.write("IBMSUBSPW".getBytes());
				baosp.write(VALUE);
				baosp.write(password.getBytes());
			}

			if (library != null) {
				baosp.write(USERVAR);
				baosp.write("IBMCURLIB".getBytes());
				baosp.write(VALUE);
				baosp.write(library.getBytes());
			}

			if (initialMenu != null) {
				baosp.write(USERVAR);
				baosp.write("IBMIMENU".getBytes());
				baosp.write(VALUE);
				baosp.write(initialMenu.getBytes());
			}

			if (program != null) {
				baosp.write(USERVAR);
				baosp.write("IBMPROGRAM".getBytes());
				baosp.write(VALUE);
				baosp.write(program.getBytes());
			}
		}
		baosp.write(IAC);
		baosp.write(SE);

		writeByte(baosp.toByteArray());
		baosp.reset();

	}

	/**
	 * This will negotiate a device name with controller. if the sequence is
	 * less than zero then it will send the device name as specified. On each
	 * unsuccessful attempt a sequential number is appended until we find one or
	 * the controller says no way.
	 *
	 * @return String
	 */
	private String negDeviceName() {

		if (devSeq++ == -1) {
			devNameUsed = devName;
			return devName;
		} else {
			StringBuffer sb = new StringBuffer(devName + devSeq);
			int ei = 1;
			while (sb.length() > 10) {

				sb.setLength(0);
				sb.append(devName.substring(0, devName.length() - ei++));
				sb.append(devSeq);

			}
			devNameUsed = sb.toString();
			return devNameUsed;
		}
	}

	public final void setCodePage(String cp) {
		codePage = CharMappings.getCodePage(cp);
		cp = cp.toLowerCase();
		for (KbdTypesCodePages kbdtyp : KbdTypesCodePages.values()) {
			if (("cp"+kbdtyp.codepage).equals(cp) || kbdtyp.ccsid.equals(cp)) {
				kbdTypesCodePage = kbdtyp;
				break;
			}
		}
		if (log.isInfoEnabled()) {
			log.info("Choosed keyboard mapping " + kbdTypesCodePage.toString() + " for code page " + cp);
		}
	}

	public final ICodePage getCodePage() {
		return codePage;
	}

	/**
	 * @see org.tn5250j.Session5250#signalBell()
	 */
	public void signalBell() {
		controller.signalBell();
	}

}
