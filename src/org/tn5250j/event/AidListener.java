package org.tn5250j.event;

/**
 * Notified when the terminal is about to send an AID (attention identifier)
 * key to the host, e.g. Enter or a PF key.
 * <p>
 * Called before the field data is read and sent, so the screen still shows the
 * user's input, cursor position and field MDTs. It may be called from the
 * session thread or the Swing event thread.
 * <p>
 * An aid of 0 means the host issued a Read Immediate; it is not a user key.
 * [sysreq] and [attn] are not AID keys and are not reported.
 */
public interface AidListener {

  void onAidKey(int aid);

}
