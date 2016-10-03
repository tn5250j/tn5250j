/*
 * Title: KeypadMnemonic.java
 * Copyright:   Copyright (c) 2016
 * Company:
 *
 * @author Martin W. Kirst
 * <p>
 * Description:
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */
package org.tn5250j.keyboard;

public enum KeyMnemonic {

  BACK_SPACE("[backspace]", 0x03e9),
  BACK_TAB("[backtab]", 0x03ea),
  UP("[up]", 0x03eb),
  DOWN("[down]", 0x03ec),
  LEFT("[left]", 0x03ed),
  RIGHT("[right]", 0x03ee),
  DELETE("[delete]", 0x03ef),
  TAB("[tab]", 0x03f0),
  END_OF_FIELD("[eof]", 0x03f1),
  ERASE_EOF("[eraseeof]", 0x03f2),
  ERASE_FIELD("[erasefld]", 0x03f3),
  INSERT("[insert]", 0x03f4),
  HOME("[home]", 0x03f5),
  KEYPAD0("[keypad0]", 0x03f6),
  KEYPAD1("[keypad1]", 0x03f7),
  KEYPAD2("[keypad2]", 0x03f8),
  KEYPAD3("[keypad3]", 0x03f9),
  KEYPAD4("[keypad4]", 0x03fa),
  KEYPAD5("[keypad5]", 0x03fb),
  KEYPAD6("[keypad6]", 0x03fc),
  KEYPAD7("[keypad7]", 0x03fd),
  KEYPAD8("[keypad8]", 0x03fe),
  KEYPAD9("[keypad9]", 0x03ff),
  KEYPAD_PERIOD("[keypad.]", 0x0400),
  KEYPAD_COMMA("[keypad,]", 0x0401),
  KEYPAD_MINUS("[keypad-]", 0x0402),
  FIELD_EXIT("[fldext]", 0x0403),
  FIELD_PLUS("[field+]", 0x0404),
  FIELD_MINUS("[field-]", 0x0405),
  BEGIN_OF_FIELD("[bof]", 0x0406),
  ENTER("[enter]", 0x00f1),
  PF1("[pf1]", 0x0031),
  PF2("[pf2]", 0x0032),
  PF3("[pf3]", 0x0033),
  PF4("[pf4]", 0x0034),
  PF5("[pf5]", 0x0035),
  PF6("[pf6]", 0x0036),
  PF7("[pf7]", 0x0037),
  PF8("[pf8]", 0x0038),
  PF9("[pf9]", 0x0039),
  PF10("[pf10]", 0x003a),
  PF11("[pf11]", 0x003b),
  PF12("[pf12]", 0x003c),
  PF13("[pf13]", 0x00b1),
  PF14("[pf14]", 0x00b2),
  PF15("[pf15]", 0x00b3),
  PF16("[pf16]", 0x00b4),
  PF17("[pf17]", 0x00b5),
  PF18("[pf18]", 0x00b6),
  PF19("[pf19]", 0x00b7),
  PF20("[pf20]", 0x00b8),
  PF21("[pf21]", 0x00b9),
  PF22("[pf22]", 0x00ba),
  PF23("[pf23]", 0x00bb),
  PF24("[pf24]", 0x00bc),
  CLEAR("[clear]", 0x00bd),
  HELP("[help]", 0x00f3),
  PAGE_UP("[pgup]", 0x00f4),
  PAGE_DOWN("[pgdown]", 0x00f5),
  ROLL_LEFT("[rollleft]", 0x00d9),
  ROLL_RIGHT("[rollright]", 0x00da),
  PRINT("[hostprint]", 0x00f6),
  PA1("[pa1]", 0x006c),
  PA2("[pa2]", 0x006e),
  PA3("[pa3]", 0x006b),
  SYSREQ("[sysreq]", 0x0407),
  RESET("[reset]", 0x0408),
  NEXTWORD("[nextword]", 0x0409),
  PREVWORD("[prevword]", 0x040a),
  COPY("[copy]", 0x040b),
  PASTE("[paste]", 0x040c),
  ATTN("[attn]", 0x040d),
  MARK_UP("[markup]", 0x040e),
  MARK_DOWN("[markdown]", 0x040f),
  MARK_LEFT("[markleft]", 0x0410),
  MARK_RIGHT("[markright]", 0x0411),
  DUP_FIELD("[dupfield]", 0x0412),
  NEW_LINE("[newline]", 0x0413),
  JUMP_NEXT("[jumpnext]", 0x1388),
  JUMP_PREV("[jumpprev]", 0x1389),
  OPEN_NEW("[opennew]", 0x138a),
  TOGGLE_CONNECTION("[togcon]", 0x138b),
  HOTSPOTS("[hotspots]", 0x138c),
  GUI("[gui]", 0x138d),
  DISP_MESSAGES("[dspmsgs]", 0x138e),
  DISP_ATTRIBUTES("[dspattr]", 0x138f),
  PRINT_SCREEN("[print]", 0x1390),
  CURSOR("[cursor]", 0x1391),
  DEBUG("[debug]", 0x1392),
  CLOSE("[close]", 0x1393),
  FILE_TRANSFER("[transfer]", 0x1394),
  E_MAIL("[e-mail]", 0x1395),
  RUN_SCRIPT("[runscript]", 0x1396),
  SPOOL_FILE("[spoolfile]", 0x1397),
  QUICK_MAIL("[quick-mail]", 0x1398),
  OPEN_SAME("[open-same]", 0x1399),
  FAST_CURSOR_DOWN("[fastcursordown]", 0x139a),
  FAST_CURSOR_UP("[fastcursorup]", 0x139b),
  FAST_CURSOR_RIGHT("[fastcursorright]", 0x139c),
  FAST_CURSOR_LEFT("[fastcursorleft]", 0x139d);

  public final String mnemonic;
  public final int value;

  KeyMnemonic(String mnemonic, int value) {
    this.mnemonic = mnemonic;
    this.value = value;
  }

}
