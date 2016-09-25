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

public enum KeypadMnemonic {

  ATTN("[attn]", 0x03e9),
  BACK_SPACE("[backspace]", 0x03ea),
  BACK_TAB("[backtab]", 0x03eb),
  BEGIN_OF_FIELD("[bof]", 0x03ec),
  CLEAR("[clear]", 0x03ed),
  CLOSE("[close]", 0x03ee),
  COPY("[copy]", 0x03ef),
  CURSOR("[cursor]", 0x03f0),
  DEBUG("[debug]", 0x03f1),
  DELETE("[delete]", 0x03f2),
  DOWN("[down]", 0x03f3),
  DISP_ATTRIBUTES("[dspattr]", 0x03f4),
  DISP_MESSAGES("[dspmsgs]", 0x03f5),
  DUP_FIELD("[dupfield]", 0x03f6),
  E_MAIL("[e-mail]", 0x03f7),
  ENTER("[enter]", 0x03f8),
  END_OF_FIELD("[eof]", 0x03f9),
  ERASE_EOF("[eraseeof]", 0x03fa),
  ERASE_FIELD("[erasefld]", 0x03fb),
  FAST_CURSOR_DOWN("[fastcursordown]", 0x03fc),
  FAST_CURSOR_LEFT("[fastcursorleft]", 0x03fd),
  FAST_CURSOR_RIGHT("[fastcursorright]", 0x03fe),
  FAST_CURSOR_UP("[fastcursorup]", 0x03ff),
  FIELD_PLUS("[field+]", 0x0400),
  FIELD_MINUS("[field-]", 0x0401),
  FIELD_EXIT("[fldext]", 0x0402),
  GUI("[gui]", 0x0403),
  HELP("[help]", 0x0404),
  HOME("[home]", 0x0405),
  PRINT("[hostprint]", 0x0406),
  HOTSPOTS("[hotspots]", 0x00f1),
  INSERT("[insert]", 0x0031),
  JUMP_NEXT("[jumpnext]", 0x0032),
  JUMP_PREV("[jumpprev]", 0x0033),
  KEYPAD_COMMA("[keypad,]", 0x0034),
  KEYPAD_MINUS("[keypad-]", 0x0035),
  KEYPAD_PERIOD("[keypad.]", 0x0036),
  KEYPAD0("[keypad0]", 0x0037),
  KEYPAD1("[keypad1]", 0x0038),
  KEYPAD2("[keypad2]", 0x0039),
  KEYPAD3("[keypad3]", 0x003a),
  KEYPAD4("[keypad4]", 0x003b),
  KEYPAD5("[keypad5]", 0x003c),
  KEYPAD6("[keypad6]", 0x00b1),
  KEYPAD7("[keypad7]", 0x00b2),
  KEYPAD8("[keypad8]", 0x00b3),
  KEYPAD9("[keypad9]", 0x00b4),
  LEFT("[left]", 0x00b5),
  MARK_DOWN("[markdown]", 0x00b6),
  MARK_LEFT("[markleft]", 0x00b7),
  MARK_RIGHT("[markright]", 0x00b8),
  MARK_UP("[markup]", 0x00b9),
  NEW_LINE("[newline]", 0x00ba),
  NEXTWORD("[nextword]", 0x00bb),
  OPEN_SAME("[open-same]", 0x00bc),
  OPEN_NEW("[opennew]", 0x00bd),
  PA1("[pa1]", 0x00f3),
  PA2("[pa2]", 0x00f4),
  PA3("[pa3]", 0x00f5),
  PASTE("[paste]", 0x00d9),
  PF10("[pf10]", 0x00da),
  PF11("[pf11]", 0x00f6),
  PF12("[pf12]", 0x006c),
  PF13("[pf13]", 0x006e),
  PF14("[pf14]", 0x006b),
  PF15("[pf15]", 0x0407),
  PF16("[pf16]", 0x0408),
  PF17("[pf17]", 0x0409),
  PF18("[pf18]", 0x040a),
  PF19("[pf19]", 0x040b),
  PF1("[pf1]", 0x040c),
  PF20("[pf20]", 0x040d),
  PF21("[pf21]", 0x040e),
  PF22("[pf22]", 0x040f),
  PF23("[pf23]", 0x0410),
  PF24("[pf24]", 0x0411),
  PF2("[pf2]", 0x0412),
  PF3("[pf3]", 0x0413),
  PF4("[pf4]", 0x1388),
  PF5("[pf5]", 0x1389),
  PF6("[pf6]", 0x138a),
  PF7("[pf7]", 0x138b),
  PF8("[pf8]", 0x138c),
  PF9("[pf9]", 0x138d),
  PAGE_DOWN("[pgdown]", 0x138e),
  PAGE_UP("[pgup]", 0x138f),
  PREVWORD("[prevword]", 0x1390),
  PRINT_SCREEN("[print]", 0x1391),
  QUICK_MAIL("[quick-mail]", 0x1392),
  RESET("[reset]", 0x1393),
  RIGHT("[right]", 0x1394),
  ROLL_LEFT("[rollleft]", 0x1395),
  ROLL_RIGHT("[rollright]", 0x1396),
  RUN_SCRIPT("[runscript]", 0x1397),
  SPOOL_FILE("[spoolfile]", 0x1398),
  SYSREQ("[sysreq]", 0x1399),
  TAB("[tab]", 0x139a),
  TOGGLE_CONNECTION("[togcon]", 0x139b),
  FILE_TRANSFER("[transfer]", 0x139c),
  UP("[up]", 0x139d);

  public final String mnemonic;
  public final int value;

  KeypadMnemonic(String mnemonic, int value) {
    this.mnemonic = mnemonic;
    this.value = value;
  }

}
