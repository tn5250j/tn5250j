package org.tn5250j.framework.tn5250;

import org.tn5250j.encoding.ICodePage;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

import static org.tn5250j.framework.tn5250.Stream5250.OPCODE_OFFSET;

public class DataStreamProducer implements Runnable {

  private static final int MINIMAL_PARTIAL_STREAM_LEN = 2;

  private BufferedInputStream bin;
  private ByteArrayOutputStream baosin;
  private byte[] saveStream;
  private final BlockingQueue<Object> dsq;
  private tnvt vt;
  private byte[] dataStream;

  private DataStreamDumper dataStreamDumper = new DataStreamDumper();

  private TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());

  public DataStreamProducer(tnvt vt, BufferedInputStream in, BlockingQueue<Object> queue, byte[] init) {
    bin = in;
    this.vt = vt;
    baosin = new ByteArrayOutputStream();
    dsq = queue;
    dataStream = init;
  }

  public final void run() {

    boolean done = false;

    Thread me = Thread.currentThread();

    // load the first response screen
    loadStream(dataStream, 0);

    while (!done) {
      try {

        byte[] abyte0 = readIncoming();

        // WVL - LDC : 17/05/2004 : Device name negotiations send TIMING MARK
        // Restructured to the readIncoming() method to return null
        // on TIMING MARK. Don't process in that case (abyte0 == null)!
        if (abyte0 != null) {
          // WVL - LDC : 16/07/2003 : TR.000345
          // When the socket has been closed, the reading returns
          // no bytes (an empty byte arrray).
          // But the loadStream fails on this, so we check it here!
          if (abyte0.length > 0) {
            loadStream(abyte0, 0);
          }
          // WVL - LDC : 16/07/2003 : TR.000345
          // Returning no bytes means the input buffer has
          // reached end-of-stream, so we do a disconnect!
          else {
            done = true;
            vt.disconnect();
          }
        }

      } catch (SocketException se) {
        log.warn("   DataStreamProducer thread interrupted and stopping " + se.getMessage());
        done = true;
      } catch (IOException ioe) {

        log.warn(ioe.getMessage());
        if (me.isInterrupted())
          done = true;

      } catch (Exception ex) {

        log.warn(ex.getMessage());
        if (me.isInterrupted())
          done = true;

      }
    }
  }

  private void loadStream(byte streamBuffer[], int offset) {

    int partialLen = (streamBuffer[offset] & 0xff) << 8 | streamBuffer[offset + 1] & 0xff;
    int bufferLen = streamBuffer.length;

    if (log.isDebugEnabled()) {
      log.debug("loadStream() offset=" + offset + " partialLen=" + partialLen + " bufferLen=" + bufferLen);
    }

    if (saveStream != null) {
      log.debug("partial stream found");
      bufferLen = saveStream.length + streamBuffer.length;
      byte[] inter = new byte[bufferLen];
      System.arraycopy(saveStream, 0, inter, 0, saveStream.length);
      System.arraycopy(streamBuffer, 0, inter, saveStream.length, streamBuffer.length);
      streamBuffer = new byte[bufferLen];
      System.arraycopy(inter, 0, streamBuffer, 0, bufferLen);
      saveStream = null;
    }

    if (partialLen > bufferLen) {
      saveStream = new byte[streamBuffer.length];
      log.debug("partial stream saved");
      System.arraycopy(streamBuffer, 0, saveStream, 0, streamBuffer.length);
    } else {
      int buf_len = partialLen + 2;
      byte[] buf = new byte[buf_len];
      if (isBufferShifted(partialLen, bufferLen) && isOpcodeShifted(streamBuffer, offset)) {
        log.debug("Invalid stream buffer detected. Ignoring the inserted byte.");
        System.arraycopy(streamBuffer, offset, buf, 0, MINIMAL_PARTIAL_STREAM_LEN);
        System.arraycopy(streamBuffer, offset + MINIMAL_PARTIAL_STREAM_LEN + 1, buf, MINIMAL_PARTIAL_STREAM_LEN, partialLen);
      } else {
        System.arraycopy(streamBuffer, offset, buf, 0, buf_len);
      }
      try {
        dsq.put(buf);
        if (streamBuffer.length > buf.length + offset + MINIMAL_PARTIAL_STREAM_LEN)
          loadStream(streamBuffer, offset + buf_len);
      } catch (InterruptedException ex) {
        log.warn("load stream error.", ex);
      }
    }
  }

  private boolean isOpcodeShifted(byte[] streamBuffer, int offset) {
    byte code = streamBuffer[offset + 1 + OPCODE_OFFSET];
    return (0 <= code && code <= 12);
  }

  private boolean isBufferShifted(int partialLen, int bufferLen) {
    return partialLen + MINIMAL_PARTIAL_STREAM_LEN + 1 == bufferLen;
  }

  public final byte[] readIncoming() throws IOException {

    boolean done = false;
    boolean negotiate = false;

    baosin.reset();
    int j = -1;

    while (!done) {

      int i = bin.read();

      // WVL - LDC : 16/07/2003 : TR.000345
      // The inStream return -1 when end-of-stream is reached. This
      // happens e.g. when the connection is closed from the AS/400.
      // So we stop in this case!
      // ==> an empty byte array is returned from this method.
      if (i == -1) // nothing read!
      {
        done = true;
        vt.disconnect();
        continue;
      }

      // We use the values instead of the static values IAC and EOR
      //    because they are defined as bytes.
      //
      // The > if(i != 255 || j != 255)  < is a hack for the double FF FF's
      // that are being returned.  I do not know why this is like this and
      // can not find any documentation for it.  It is also being returned
      // on my Client Access tcp dump as well so they are handling it.
      //
      // my5250
      // 0000:  00 50 DA 44 C8 45 42 00 00 00 00 24 08 00 45 00 .P.D.EB....$..E.
      // 0010:  04 2A BC F9 00 00 40 06 D0 27 C1 A8 33 04 C1 A8 .*....@..'..3...
      // 0020:  33 58 00 17 04 18 6F A2 83 CB 00 1E D1 BA 50 18 3X....o.......P.
      // 0030:  20 00 8A 9A 00 00 03 FF FF 12 A0 00 00 04 00 00  ...............
      // --------------------------- || || -------------------------------------
      // 0040:  03 04 40 04 11 00 20 01 07 00 00 00 18 00 00 10 ..@... .........

      if (j == 255 && i == 255) {
        j = -1;
        continue;
      }
      baosin.write(i);
      // check for end of record EOR and IAC  - FFEF
      if (j == 255 && i == 239)
        done = true;

      // This is to check for the TELNET TIMING MARK OPTION
      // rfc860 explains this in more detail.  When we receive it
      // we will negotiate with the server by sending a WONT'T TIMING-MARK
      // This will let the server know that we processed the information
      // and are just waiting for the user to enter some data so keep the
      // socket alive.   This is more or less a AYT (ARE YOU THERE) or not.
      if (i == 253 && j == 255) {
        done = true;
        negotiate = true;
      }
      j = i;
    }

    // after the initial negotiation we might get other options such as
    //    timing marks ??????????????  do we ???????????? look at telnet spec
    // yes we do. rfc860 explains about timing marks.

    // WVL - LDC : 17/05/2004 : Device name negotiations send TIMING MARK
    //                          to existing device!
    // Handled incorrectly: we cannot continue processing the TIMING MARK DO
    // after we have handled it in the vt.negotiate()
    // We should not return the bytes;
    // ==> restructured to return null after negotiation!
    //     Impacts the run method! Added the null check.
    byte[] rBytes = baosin.toByteArray();

    dataStreamDumper.dump(rBytes);

    if (negotiate) {
      // get the negotiation option
      baosin.write(bin.read());
      vt.negotiate(rBytes);

      return null;
    }
    return rBytes;
  }

  protected void toggleDebug(ICodePage codePage) {
    dataStreamDumper.toggleDebug(codePage);
  }
}
