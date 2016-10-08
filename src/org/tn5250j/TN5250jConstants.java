/*
 * @(#)TN5250jConstants.java
 * Copyright:    Copyright (c) 2001
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */

package org.tn5250j;

public interface TN5250jConstants {

  // Version information
  String VERSION_INFO = "0.8.0-Snapshot";

  // STATE
  static final int STATE_DISCONNECTED = 0;
  static final int STATE_CONNECTED = 1;
  static final int STATE_REMOVE = 2;

  // SESSION Level key value pairs
  String SESSION_HOST = "SESSION_HOST";
  String SESSION_HOST_PORT = "SESSION_HOST_PORT";
  String SESSION_CONFIG_RESOURCE = "SESSION_CONFIG_RESOURCE";
  String SESSION_TYPE = "SESSION_HOST_TYPE";
  String SESSION_TN_ENHANCED = "SESSION_TN_ENHANCED";
  String SESSION_SCREEN_SIZE = "SESSION_SCREEN_SIZE";
  String SESSION_CODE_PAGE = "SESSION_CODE_PAGE";
  String SESSION_PROXY_HOST = "SESSION_PROXY_HOST";
  String SESSION_PROXY_PORT = "SESSION_PROXY_PORT";
  String SESSION_USE_GUI = "SESSION_USE_GUI";
  String SESSION_DEVICE_NAME = "SESSION_DEVICE_NAME";
  String SESSION_NAMES_REFS = "SESSION_NAMES_REFS";
  String SESSION_LOCALE = "SESSION_LOCALE";
  String SESSION_CONFIG_FILE = "SESSION_CONFIG_FILE";
  String SESSION_TERM_NAME_SYSTEM = "SESSION_TERM_NAME_SYSTEM";
  String SESSION_TERM_NAME = "SESSION_TERM_NAME";
  String SESSION_IS_APPLET = "SESSION_IS_APPLET";
  String SESSION_HEART_BEAT = "SESSION_KEEP_ALIVE_ENABLED";

  //    String GUI_MDI_TYPE = "GUI_MDI_TYPE";
  String GUI_FRAME_WIDTH = "GUI_FRAME_WIDTH";
  String GUI_FRAME_HEIGHT = "GUI_FRAME_HEIGHT";
  String GUI_NO_TAB = "GUI_NO_TAB";
  String NO_CHECK_RUNNING = "NO_CHECK_RUNNING";
  String START_MONITOR_THREAD = "START_MONITOR_THREAD";

  //    String SSL_TYPE = "TN5250J_SSL_TYPE";
  String SSL_TYPE = "-sslType";
  String SSL_TYPE_NONE = "NONE";
  String SSL_TYPE_SSLv2 = "SSLv2";
  String SSL_TYPE_SSLv3 = "SSLv3";
  String SSL_TYPE_TLS = "TLS";

  String[] SSL_TYPES = {SSL_TYPE_NONE,
      SSL_TYPE_SSLv2,
      SSL_TYPE_SSLv3,
      SSL_TYPE_TLS};

  // Session JUMP Directions
  static final int JUMP_PREVIOUS = 0;
  static final int JUMP_NEXT = 1;

//   // OS_OHIO_SESSION_TYPE type of sessions
//    String OS_OHIO_SESSION_TYPE_5250_STR   = "2";

  // SCREEN_SIZE Size of screen string
  String SCREEN_SIZE_24X80_STR = "0";
  String SCREEN_SIZE_27X132_STR = "1";

  // SCREEN_SIZE Size of screen int
  int SCREEN_SIZE_24X80 = 0;
  int SCREEN_SIZE_27X132 = 1;

  int NUM_PARMS = 20;

  // mnemonic value constants
  int BACK_SPACE = 1001;
  int BACK_TAB = 1002;
  int UP = 1003;
  int DOWN = 1004;
  int LEFT = 1005;
  int RIGHT = 1006;
  int DELETE = 1007;
  int TAB = 1008;
  int EOF = 1009;
  int ERASE_EOF = 1010;
  int ERASE_FIELD = 1011;
  int INSERT = 1012;
  int HOME = 1013;
  int KEYPAD_0 = 1014;
  int KEYPAD_1 = 1015;
  int KEYPAD_2 = 1016;
  int KEYPAD_3 = 1017;
  int KEYPAD_4 = 1018;
  int KEYPAD_5 = 1019;
  int KEYPAD_6 = 1020;
  int KEYPAD_7 = 1021;
  int KEYPAD_8 = 1022;
  int KEYPAD_9 = 1023;
  int KEYPAD_PERIOD = 1024;
  int KEYPAD_COMMA = 1025;
  int KEYPAD_MINUS = 1026;
  int FIELD_EXIT = 1027;
  int FIELD_PLUS = 1028;
  int FIELD_MINUS = 1029;
  int BOF = 1030;
  int SYSREQ = 1031;
  int RESET = 1032;
  int NEXTWORD = 1033;
  int PREVWORD = 1034;
  int COPY = 1035;
  int PASTE = 1036;
  int ATTN = 1037;
  int MARK_UP = 1038;
  int MARK_DOWN = 1039;
  int MARK_LEFT = 1040;
  int MARK_RIGHT = 1041;
  int DUP_FIELD = 1042;
  int NEW_LINE = 1043;
  int JUMP_NEXT_SESS = 5000;
  int JUMP_PREV_SESS = 5001;
  int OPEN_NEW = 5002;
  int TOGGLE_CONNECTION = 5003;
  int HOTSPOTS = 5004;
  int GUI = 5005;
  int DSP_MSGS = 5006;
  int DSP_ATTRIBUTES = 5007;
  int PRINT_SCREEN = 5008;
  int CURSOR = 5009;
  int DEBUG = 5010;
  int CLOSE = 5011;
  int TRANSFER = 5012;
  int E_MAIL = 5013;
  int RUN_SCRIPT = 5014;
  int SPOOL_FILE = 5015;
  int QUICK_MAIL = 5016;
  int OPEN_SAME = 5017;
  int FAST_CURSOR_DOWN = 5018;
  int FAST_CURSOR_UP = 5019;
  int FAST_CURSOR_RIGHT = 5020;
  int FAST_CURSOR_LEFT = 5021;

  // PF Keys
  int PF1 = 0x31;
  int PF2 = 0x32;
  int PF3 = 0x33;
  int PF4 = 0x34;
  int PF5 = 0x35;
  int PF6 = 0x36;
  int PF7 = 0x37;
  int PF8 = 0x38;
  int PF9 = 0x39;
  int PF10 = 0x3A;
  int PF11 = 0x3B;
  int PF12 = 0x3C;
  int PF13 = 0xB1;
  int PF14 = 0xB2;
  int PF15 = 0xB3;
  int PF16 = 0xB4;
  int PF17 = 0xB5;
  int PF18 = 0xB6;
  int PF19 = 0xB7;
  int PF20 = 0xB8;
  int PF21 = 0xB9;
  int PF22 = 0xBA;
  int PF23 = 0xBB;
  int PF24 = 0xBC;

  // AID-Generating Keys
  int AID_CLEAR = 0xBD;
  int AID_ENTER = 0xF1;
  int AID_HELP = 0xF3;
  int AID_ROLL_UP = 0xF4;
  int AID_ROLL_DOWN = 0xF5;
  int AID_ROLL_LEFT = 0xD9;
  int AID_ROLL_RIGHT = 0xDA;
  int AID_PRINT = 0xF6;
  int AID_PF1 = 0x31;
  int AID_PF2 = 0x32;
  int AID_PF3 = 0x33;
  int AID_PF4 = 0x34;
  int AID_PF5 = 0x35;
  int AID_PF6 = 0x36;
  int AID_PF7 = 0x37;
  int AID_PF8 = 0x38;
  int AID_PF9 = 0x39;
  int AID_PF10 = 0x3A;
  int AID_PF11 = 0x3B;
  int AID_PF12 = 0x3C;
  int AID_PF13 = 0xB1;
  int AID_PF14 = 0xB2;
  int AID_PF15 = 0xB3;
  int AID_PF16 = 0xB4;
  int AID_PF17 = 0xB5;
  int AID_PF18 = 0xB6;
  int AID_PF19 = 0xB7;
  int AID_PF20 = 0xB8;
  int AID_PF21 = 0xB9;
  int AID_PF22 = 0xBA;
  int AID_PF23 = 0xBB;
  int AID_PF24 = 0xBC;

  // negative response categories
  int NR_REQUEST_REJECT = 0x08;
  int NR_REQUEST_ERROR = 0x10;
  int NR_STATE_ERROR = 0x20;
  int NR_USAGE_ERROR = 0x40;
  int NR_PATH_ERROR = 0x80;

  // commands
  byte CMD_WRITE_TO_DISPLAY = 0x11; // 17
  byte CMD_CLEAR_UNIT = 0x40; // 64
  byte CMD_CLEAR_UNIT_ALTERNATE = 0x20; // 32
  byte CMD_CLEAR_FORMAT_TABLE = 0x50; // 80
  byte CMD_READ_INPUT_FIELDS = 0x42; // 66
  byte CMD_READ_MDT_FIELDS = 0x52; // 82
  byte CMD_READ_MDT_IMMEDIATE_ALT = (byte) 0x83; // 131
  //    byte CMD_READ_MDT_FIELDS_ALT = (byte)0x82; // 130
//    byte CMD_READ_IMMEDIATE = 0x72; // 114
  byte CMD_READ_SCREEN_IMMEDIATE = 0x62; // 98
  byte CMD_WRITE_STRUCTURED_FIELD = (byte) 243;  // (byte)0xF3 -13
  byte CMD_SAVE_SCREEN = 0x02; // 02
  byte CMD_RESTORE_SCREEN = 0x12; // 18
  byte CMD_WRITE_ERROR_CODE = 0x21; // 33
  byte CMD_WRITE_ERROR_CODE_TO_WINDOW = 0x22; // 34
  byte CMD_ROLL = 0x23; // 35
  byte CMD_READ_SCREEN_TO_PRINT = (byte) 0x66; // 102

  // PLANES
  int PLANE_TEXT = 1;
  int PLANE_COLOR = 2;
  int PLANE_FIELD = 3;
  int PLANE_EXTENDED = 4;
  int PLANE_EXTENDED_GRAPHIC = 5;
  int PLANE_EXTENDED_FIELD = 6;
  int PLANE_ATTR = 7;
  int PLANE_IS_ATTR_PLACE = 8;

  // COLOR_BG
  char COLOR_BG_BLACK = 0;
  char COLOR_BG_BLUE = 1;
  char COLOR_BG_GREEN = 2;
  char COLOR_BG_CYAN = 3;
  char COLOR_BG_RED = 4;
  char COLOR_BG_MAGENTA = 5;
  char COLOR_BG_YELLOW = 6;
  char COLOR_BG_WHITE = 7;

  // COLOR_FG
  char COLOR_FG_BLACK = 0;
  char COLOR_FG_BLUE = 1;
  char COLOR_FG_GREEN = 2;
  char COLOR_FG_CYAN = 3;
  char COLOR_FG_RED = 4;
  char COLOR_FG_MAGENTA = 5;
  char COLOR_FG_YELLOW = 6;
  char COLOR_FG_WHITE = 7;
  char COLOR_FG_BROWN = 0xE;
  char COLOR_FG_GRAY = 8;
  char COLOR_FG_LIGHT_BLUE = 9;
  char COLOR_FG_LIGHT_GREEN = 0xA;
  char COLOR_FG_LIGHT_CYAN = 0xB;
  char COLOR_FG_LIGHT_RED = 0xC;
  char COLOR_FG_LIGHT_MAGENTA = 0xD;
  char COLOR_FG_WHITE_HIGH = 0xF;

  int EXTENDED_5250_REVERSE = 0x10;
  int EXTENDED_5250_UNDERLINE = 0x08;
  int EXTENDED_5250_BLINK = 0x04;
  int EXTENDED_5250_COL_SEP = 0x02;
  int EXTENDED_5250_NON_DSP = 0x01;

  char ATTR_32 = (COLOR_BG_BLACK << 8 & 0xff00) |
      (COLOR_FG_GREEN & 0xff);
  char ATTR_33 = (COLOR_BG_GREEN << 8 & 0xff00) |
      (COLOR_FG_BLACK & 0xff);
  char ATTR_34 = (COLOR_BG_BLACK << 8 & 0xff00) |
      (COLOR_FG_WHITE & 0xff);
  char ATTR_35 = (COLOR_BG_WHITE << 8 & 0xff00) |
      (COLOR_FG_BLACK & 0xff);
  char ATTR_36 = (COLOR_BG_BLACK << 8 & 0xff00) |
      (COLOR_FG_GREEN & 0xff);
  char ATTR_37 = (COLOR_BG_GREEN << 8 & 0xff00) |
      (COLOR_FG_BLACK & 0xff);
  char ATTR_38 = (COLOR_BG_BLACK << 8 & 0xff00) |
      (COLOR_FG_WHITE & 0xff);
  char ATTR_40 = (COLOR_BG_BLACK << 8 & 0xff00) |
      (COLOR_FG_RED & 0xff);
  char ATTR_41 = (COLOR_BG_RED << 8 & 0xff00) |
      (COLOR_FG_BLACK & 0xff);
  char ATTR_42 = (COLOR_BG_BLACK << 8 & 0xff00) |
      (COLOR_FG_RED & 0xff);
  char ATTR_43 = (COLOR_BG_RED << 8 & 0xff00) |
      (COLOR_FG_BLACK & 0xff);

  char ATTR_44 = (COLOR_BG_BLACK << 8 & 0xff00) |
      (COLOR_FG_RED & 0xff);
  char ATTR_45 = (COLOR_BG_RED << 8 & 0xff00) |
      (COLOR_FG_BLACK & 0xff);
  char ATTR_46 = (COLOR_BG_BLACK << 8 & 0xff00) |
      (COLOR_FG_RED & 0xff);

  char ATTR_48 = (COLOR_BG_BLACK << 8 & 0xff00) |
      (COLOR_FG_CYAN & 0xff);
  char ATTR_49 = (COLOR_BG_CYAN << 8 & 0xff00) |
      (COLOR_FG_BLACK & 0xff);
  char ATTR_50 = (COLOR_BG_BLACK << 8 & 0xff00) |
      (COLOR_FG_YELLOW & 0xff);

  char ATTR_51 = (COLOR_BG_YELLOW << 8 & 0xff00) |
      (COLOR_FG_BLACK & 0xff);
  char ATTR_52 = (COLOR_BG_BLACK << 8 & 0xff00) |
      (COLOR_FG_CYAN & 0xff);
  char ATTR_53 = (COLOR_BG_CYAN << 8 & 0xff00) |
      (COLOR_FG_BLACK & 0xff);
  char ATTR_54 = (COLOR_BG_BLACK << 8 & 0xff00) |
      (COLOR_FG_YELLOW & 0xff);
  char ATTR_56 = (COLOR_BG_BLACK << 8 & 0xff00) |
      (COLOR_FG_MAGENTA & 0xff);
  char ATTR_57 = (COLOR_BG_MAGENTA << 8 & 0xff00) |
      (COLOR_FG_BLACK & 0xff);
  char ATTR_58 = (COLOR_BG_BLACK << 8 & 0xff00) |
      (COLOR_FG_BLUE & 0xff);
  char ATTR_59 = (COLOR_BG_BLUE << 8 & 0xff00) |
      (COLOR_FG_BLACK & 0xff);
  char ATTR_60 = (COLOR_BG_BLACK << 8 & 0xff00) |
      (COLOR_FG_MAGENTA & 0xff);
  char ATTR_61 = (COLOR_BG_MAGENTA << 8 & 0xff00) |
      (COLOR_FG_BLACK & 0xff);
  char ATTR_62 = (COLOR_BG_BLACK << 8 & 0xff00) |
      (COLOR_FG_BLUE & 0xff);

  int NO_GUI = 0;
  int UPPER_LEFT = 1;
  int UPPER = 2;
  int UPPER_RIGHT = 3;
  int GUI_LEFT = 4;
  int GUI_RIGHT = 5;
  int LOWER_LEFT = 6;
  int BOTTOM = 7;
  int LOWER_RIGHT = 8;
  int FIELD_LEFT = 9;
  int FIELD_RIGHT = 10;
  int FIELD_MIDDLE = 11;
  int FIELD_ONE = 12;
  int BUTTON_LEFT = 13;
  int BUTTON_RIGHT = 14;
  int BUTTON_MIDDLE = 15;
  int BUTTON_ONE = 16;
  int BUTTON_LEFT_UP = 17;
  int BUTTON_RIGHT_UP = 18;
  int BUTTON_MIDDLE_UP = 19;
  int BUTTON_ONE_UP = 20;
  int BUTTON_LEFT_DN = 21;
  int BUTTON_RIGHT_DN = 22;
  int BUTTON_MIDDLE_DN = 23;
  int BUTTON_ONE_DN = 24;
  int BUTTON_LEFT_EB = 25;
  int BUTTON_RIGHT_EB = 26;
  int BUTTON_MIDDLE_EB = 27;
  int BUTTON_SB_UP = 28;
  int BUTTON_SB_DN = 29;
  int BUTTON_SB_GUIDE = 30;
  int BUTTON_SB_THUMB = 31;
  int BUTTON_LAST = 31;

}
