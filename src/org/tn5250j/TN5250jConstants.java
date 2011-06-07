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
   public static final String tn5250jRelease = "0";
   public static final String tn5250jVersion = ".7";
   public static final String tn5250jSubVer= ".0";
   
   public static final String VERSION_INFO = tn5250jRelease + tn5250jVersion + tn5250jSubVer;

   // STATE
   static final int STATE_DISCONNECTED   =  0;
   static final int STATE_CONNECTED   =  1;
   static final int STATE_REMOVE   =  2;

   // SESSION Level key value pairs
   public static final String SESSION_HOST      = "SESSION_HOST";
   public static final String SESSION_HOST_PORT ="SESSION_HOST_PORT";
   public static final String SESSION_CONFIG_RESOURCE = "SESSION_CONFIG_RESOURCE";
   public static final String SESSION_TYPE     = "SESSION_HOST_TYPE";
   public static final String SESSION_TN_ENHANCED = "SESSION_TN_ENHANCED";
   public static final String SESSION_SCREEN_SIZE = "SESSION_SCREEN_SIZE";
   public static final String SESSION_CODE_PAGE = "SESSION_CODE_PAGE";
   public static final String SESSION_PROXY_HOST = "SESSION_PROXY_HOST";
   public static final String SESSION_PROXY_PORT = "SESSION_PROXY_PORT";
   public static final String SESSION_USE_GUI = "SESSION_USE_GUI";
   public static final String SESSION_DEVICE_NAME = "SESSION_DEVICE_NAME";
   public static final String SESSION_NAMES_REFS = "SESSION_NAMES_REFS";
   public static final String SESSION_LOCALE = "SESSION_LOCALE";
   public static final String SESSION_CONFIG_FILE = "SESSION_CONFIG_FILE";
   public static final String SESSION_TERM_NAME_SYSTEM = "SESSION_TERM_NAME_SYSTEM";
   public static final String SESSION_TERM_NAME = "SESSION_TERM_NAME";
   public static final String SESSION_IS_APPLET = "SESSION_IS_APPLET";
   public static final String SESSION_HEART_BEAT = "SESSION_KEEP_ALIVE_ENABLED";

//   public static final String GUI_MDI_TYPE = "GUI_MDI_TYPE";
   public static final String GUI_FRAME_WIDTH = "GUI_FRAME_WIDTH";
   public static final String GUI_FRAME_HEIGHT = "GUI_FRAME_HEIGHT";
   public static final String GUI_NO_TAB = "GUI_NO_TAB";
   public static final String NO_CHECK_RUNNING = "NO_CHECK_RUNNING";
   public static final String START_MONITOR_THREAD = "START_MONITOR_THREAD";

//   public static final String SSL_TYPE = "TN5250J_SSL_TYPE";
   public static final String SSL_TYPE = "-sslType";
   public static final String SSL_TYPE_NONE = "NONE";
   public static final String SSL_TYPE_SSLv2 = "SSLv2";
   public static final String SSL_TYPE_SSLv3 = "SSLv3";
   public static final String SSL_TYPE_TLS = "TLS";

   public static final String[] SSL_TYPES = {SSL_TYPE_NONE,
                                             SSL_TYPE_SSLv2,
                                             SSL_TYPE_SSLv3,
                                             SSL_TYPE_TLS};

   // Session JUMP Directions
   static final int JUMP_PREVIOUS   =  0;
   static final int JUMP_NEXT   =  1;

//   // OS_OHIO_SESSION_TYPE type of sessions
//   public static final String OS_OHIO_SESSION_TYPE_5250_STR   = "2";

   // SCREEN_SIZE Size of screen string
   public static final String SCREEN_SIZE_24X80_STR   = "0";
   public static final String SCREEN_SIZE_27X132_STR   = "1";

   // SCREEN_SIZE Size of screen int
   public static final int SCREEN_SIZE_24X80   = 0;
   public static final int SCREEN_SIZE_27X132   = 1;

   public static final int NUM_PARMS = 20;

   // mnemonic value constants
   public static final int BACK_SPACE = 1001;
   public static final int BACK_TAB  = 1002;
   public static final int UP = 1003;
   public static final int DOWN = 1004;
   public static final int LEFT = 1005;
   public static final int RIGHT = 1006;
   public static final int DELETE = 1007;
   public static final int TAB = 1008;
   public static final int EOF = 1009;
   public static final int ERASE_EOF = 1010;
   public static final int ERASE_FIELD = 1011;
   public static final int INSERT = 1012;
   public static final int HOME = 1013;
   public static final int KEYPAD_0 = 1014;
   public static final int KEYPAD_1 = 1015;
   public static final int KEYPAD_2 = 1016;
   public static final int KEYPAD_3 = 1017;
   public static final int KEYPAD_4 = 1018;
   public static final int KEYPAD_5 = 1019;
   public static final int KEYPAD_6 = 1020;
   public static final int KEYPAD_7 = 1021;
   public static final int KEYPAD_8 = 1022;
   public static final int KEYPAD_9 = 1023;
   public static final int KEYPAD_PERIOD = 1024;
   public static final int KEYPAD_COMMA = 1025;
   public static final int KEYPAD_MINUS = 1026;
   public static final int FIELD_EXIT = 1027;
   public static final int FIELD_PLUS = 1028;
   public static final int FIELD_MINUS = 1029;
   public static final int BOF = 1030;
   public static final int SYSREQ = 1031;
   public static final int RESET = 1032;
   public static final int NEXTWORD = 1033;
   public static final int PREVWORD = 1034;
   public static final int COPY = 1035;
   public static final int PASTE = 1036;
   public static final int ATTN = 1037;
   public static final int MARK_UP = 1038;
   public static final int MARK_DOWN = 1039;
   public static final int MARK_LEFT = 1040;
   public static final int MARK_RIGHT = 1041;
   public static final int DUP_FIELD = 1042;
   public static final int NEW_LINE = 1043;
   public static final int JUMP_NEXT_SESS = 5000;
   public static final int JUMP_PREV_SESS = 5001;
   public static final int OPEN_NEW = 5002;
   public static final int TOGGLE_CONNECTION = 5003;
   public static final int HOTSPOTS = 5004;
   public static final int GUI = 5005;
   public static final int DSP_MSGS = 5006;
   public static final int DSP_ATTRIBUTES = 5007;
   public static final int PRINT_SCREEN = 5008;
   public static final int CURSOR = 5009;
   public static final int DEBUG = 5010;
   public static final int CLOSE = 5011;
   public static final int TRANSFER = 5012;
   public static final int E_MAIL = 5013;
   public static final int RUN_SCRIPT = 5014;
   public static final int SPOOL_FILE = 5015;
   public static final int QUICK_MAIL = 5016;
   public static final int OPEN_SAME = 5017;
   public static final int FAST_CURSOR_DOWN = 5018;
   public static final int FAST_CURSOR_UP = 5019;
   public static final int FAST_CURSOR_RIGHT = 5020;
   public static final int FAST_CURSOR_LEFT = 5021;

   // PF Keys
   public static final int PF1 = 0x31;
   public static final int PF2 = 0x32;
   public static final int PF3 = 0x33;
   public static final int PF4 = 0x34;
   public static final int PF5 = 0x35;
   public static final int PF6 = 0x36;
   public static final int PF7 = 0x37;
   public static final int PF8 = 0x38;
   public static final int PF9 = 0x39;
   public static final int PF10 = 0x3A;
   public static final int PF11 = 0x3B;
   public static final int PF12 = 0x3C;
   public static final int PF13 = 0xB1;
   public static final int PF14 = 0xB2;
   public static final int PF15 = 0xB3;
   public static final int PF16 = 0xB4;
   public static final int PF17 = 0xB5;
   public static final int PF18 = 0xB6;
   public static final int PF19 = 0xB7;
   public static final int PF20 = 0xB8;
   public static final int PF21 = 0xB9;
   public static final int PF22 = 0xBA;
   public static final int PF23 = 0xBB;
   public static final int PF24 = 0xBC;

   public static final String mnemonicData[] = {
        "[backspace]", "[backtab]", "[up]", "[down]", "[left]",
        "[right]", "[delete]", "[tab]", "[eof]", "[eraseeof]",
        "[erasefld]", "[insert]", "[home]", "[keypad0]", "[keypad1]",
        "[keypad2]", "[keypad3]", "[keypad4]", "[keypad5]", "[keypad6]",
        "[keypad7]", "[keypad8]", "[keypad9]", "[keypad.]", "[keypad,]",
        "[keypad-]", "[fldext]", "[field+]", "[field-]", "[bof]",
        "[enter]","[pf1]","[pf2]","[pf3]","[pf4]",
        "[pf5]","[pf6]","[pf7]","[pf8]","[pf9]",
        "[pf10]","[pf11]","[pf12]","[pf13]","[pf14]",
        "[pf15]","[pf16]","[pf17]","[pf18]","[pf19]",
        "[pf20]","[pf21]","[pf22]","[pf23]","[pf24]",
        "[clear]", "[help]", "[pgup]", "[pgdown]", "[rollleft]",
        "[rollright]", "[hostprint]", "[pa1]", "[pa2]", "[pa3]",
        "[sysreq]","[reset]","[nextword]", "[prevword]", "[copy]",
        "[paste]","[attn]","[markup]", "[markdown]", "[markleft]",
        "[markright]","[dupfield]","[newline]","[jumpnext]","[jumpprev]",
        "[opennew]","[togcon]","[hotspots]","[gui]","[dspmsgs]",
        "[dspattr]","[print]","[cursor]","[debug]","[close]",
        "[transfer]","[e-mail]","[runscript]","[spoolfile]","[quick-mail]",
        "[open-same]","[fastcursordown]","[fastcursorup]","[fastcursorright]","[fastcursorleft]"
   };

   public static final int mnemonicValue[] = {
        1001, 1002, 1003, 1004, 1005,
        1006, 1007, 1008, 1009, 1010,
        1011, 1012, 1013, 1014, 1015,
        1016, 1017, 1018, 1019, 1020,
        1021, 1022, 1023, 1024, 1025,
        1026, 1027, 1028, 1029, 1030,
        0xF1, 0x31, 0x32, 0x33, 0x34,
        0x35, 0x36, 0x37, 0x38, 0x39,
        0x3A, 0x3B, 0x3C, 0xB1, 0xB2,
        0xB3, 0xB4, 0xB5, 0xB6, 0xB7,
        0xB8, 0xB9, 0xBA, 0xBB, 0xBC,
        0xBD, 0xF3, 0xF4, 0xF5, 0xD9,
        0xDA, 0xF6, 0x6C, 0x6E, 0x6B,
        1031, 1032, 1033, 1034, 1035,
        1036, 1037, 1038, 1039, 1040,
        1041, 1042, 1043, 5000, 5001,
        5002, 5003, 5004, 5005, 5006,
        5007, 5008, 5009, 5010, 5011,
        5012, 5013, 5014, 5015, 5016,
        5017, 5018, 5019, 5020, 5021
   };

   public static final String MNEMONIC_CLEAR   =  "[clear]";
   public static final String MNEMONIC_ENTER   =  "[enter]";
   public static final String MNEMONIC_HELP   =  "[help]";
   public static final String MNEMONIC_PAGE_DOWN   =  "[pgdown]";
   public static final String MNEMONIC_PAGE_UP   =  "[pgup]";
   public static final String MNEMONIC_PRINT   =  "[hostprint]";
   public static final String MNEMONIC_PF1   =  "[pf1]";
   public static final String MNEMONIC_PF2   =  "[pf2]";
   public static final String MNEMONIC_PF3   =  "[pf3]";
   public static final String MNEMONIC_PF4   =  "[pf4]";
   public static final String MNEMONIC_PF5   =  "[pf5]";
   public static final String MNEMONIC_PF6   =  "[pf6]";
   public static final String MNEMONIC_PF7   =  "[pf7]";
   public static final String MNEMONIC_PF8   =  "[pf8]";
   public static final String MNEMONIC_PF9   =  "[pf9]";
   public static final String MNEMONIC_PF10   =  "[pf10]";
   public static final String MNEMONIC_PF11   =  "[pf11]";
   public static final String MNEMONIC_PF12   =  "[pf12]";
   public static final String MNEMONIC_PF13   =  "[pf13]";
   public static final String MNEMONIC_PF14   =  "[pf14]";
   public static final String MNEMONIC_PF15   =  "[pf15]";
   public static final String MNEMONIC_PF16   =  "[pf16]";
   public static final String MNEMONIC_PF17   =  "[pf17]";
   public static final String MNEMONIC_PF18   =  "[pf18]";
   public static final String MNEMONIC_PF19   =  "[pf19]";
   public static final String MNEMONIC_PF20   =  "[pf20]";
   public static final String MNEMONIC_PF21   =  "[pf21]";
   public static final String MNEMONIC_PF22   =  "[pf22]";
   public static final String MNEMONIC_PF23   =  "[pf23]";
   public static final String MNEMONIC_PF24   =  "[pf24]";
   public static final String MNEMONIC_BACK_SPACE   =  "[backspace]";
   public static final String MNEMONIC_BACK_TAB   =  "[backtab]";
   public static final String MNEMONIC_UP   =  "[up]";
   public static final String MNEMONIC_DOWN   =  "[down]";
   public static final String MNEMONIC_LEFT   =  "[left]";
   public static final String MNEMONIC_RIGHT   =  "[right]";
   public static final String MNEMONIC_DELETE   =  "[delete]";
   public static final String MNEMONIC_TAB =  "[tab]";
   public static final String MNEMONIC_END_OF_FIELD   =  "[eof]";
   public static final String MNEMONIC_ERASE_EOF   =  "[eraseeof]";
   public static final String MNEMONIC_ERASE_FIELD   =  "[erasefld]";
   public static final String MNEMONIC_INSERT   =  "[insert]";
   public static final String MNEMONIC_HOME   =  "[home]";
   public static final String MNEMONIC_KEYPAD0   =  "[keypad0]";
   public static final String MNEMONIC_KEYPAD1   =  "[keypad1]";
   public static final String MNEMONIC_KEYPAD2   =  "[keypad2]";
   public static final String MNEMONIC_KEYPAD3   =  "[keypad3]";
   public static final String MNEMONIC_KEYPAD4   =  "[keypad4]";
   public static final String MNEMONIC_KEYPAD5   =  "[keypad5]";
   public static final String MNEMONIC_KEYPAD6   =  "[keypad6]";
   public static final String MNEMONIC_KEYPAD7   =  "[keypad7]";
   public static final String MNEMONIC_KEYPAD8   =  "[keypad8]";
   public static final String MNEMONIC_KEYPAD9   =  "[keypad9]";
   public static final String MNEMONIC_KEYPAD_PERIOD   =  "[keypad.]";
   public static final String MNEMONIC_KEYPAD_COMMA   =  "[keypad,]";
   public static final String MNEMONIC_KEYPAD_MINUS   =  "[keypad-]";
   public static final String MNEMONIC_FIELD_EXIT   =  "[fldext]";
   public static final String MNEMONIC_FIELD_PLUS   =  "[field+]";
   public static final String MNEMONIC_FIELD_MINUS   =  "[field-]";
   public static final String MNEMONIC_BEGIN_OF_FIELD   =  "[bof]";
   public static final String MNEMONIC_PA1   =  "[pa1]";
   public static final String MNEMONIC_PA2   =  "[pa2]";
   public static final String MNEMONIC_PA3   =  "[pa3]";
   public static final String MNEMONIC_SYSREQ   =  "[sysreq]";
   public static final String MNEMONIC_RESET   =  "[reset]";
   public static final String MNEMONIC_NEXTWORD   =  "[nextword]";
   public static final String MNEMONIC_PREVWORD   =  "[prevword]";
   public static final String MNEMONIC_ATTN   =  "[attn]";
   public static final String MNEMONIC_MARK_LEFT   =  "[markleft]";
   public static final String MNEMONIC_MARK_RIGHT   =  "[markright]";
   public static final String MNEMONIC_MARK_UP   =  "[markup]";
   public static final String MNEMONIC_MARK_DOWN   =  "[markdown]";
   public static final String MNEMONIC_DUP_FIELD   =  "[dupfield]";
   public static final String MNEMONIC_NEW_LINE   =  "[newline]";
   public static final String MNEMONIC_JUMP_NEXT   =  "[jumpnext]";
   public static final String MNEMONIC_JUMP_PREV   =  "[jumpprev]";
   public static final String MNEMONIC_OPEN_NEW   =  "[opennew]";
   public static final String MNEMONIC_TOGGLE_CONNECTION   =  "[togcon]";
   public static final String MNEMONIC_HOTSPOTS   =  "[hotspots]";
   public static final String MNEMONIC_GUI   =  "[gui]";
   public static final String MNEMONIC_DISP_MESSAGES   =  "[dspmsgs]";
   public static final String MNEMONIC_DISP_ATTRIBUTES   =  "[dspattr]";
   public static final String MNEMONIC_PRINT_SCREEN   =  "[print]";
   public static final String MNEMONIC_CURSOR   =  "[cursor]";
   public static final String MNEMONIC_DEBUG   =  "[debug]";
   public static final String MNEMONIC_CLOSE   =  "[close]";
   public static final String MNEMONIC_E_MAIL   =  "[e-mail]";
   public static final String MNEMONIC_COPY   =  "[copy]";
   public static final String MNEMONIC_PASTE   =  "[paste]";
   public static final String MNEMONIC_FILE_TRANSFER   =  "[transfer]";
   public static final String MNEMONIC_RUN_SCRIPT   =  "[runscript]";
   public static final String MNEMONIC_SPOOL_FILE   =  "[spoolfile]";
   public static final String MNEMONIC_QUICK_MAIL   =  "[quick-mail]";
   public static final String MNEMONIC_OPEN_SAME   =  "[open-same]";
   public static final String MNEMONIC_FAST_CURSOR_DOWN   =  "[fastcursordown]";
   public static final String MNEMONIC_FAST_CURSOR_UP   =  "[fastcursorup]";
   public static final String MNEMONIC_FAST_CURSOR_RIGHT   =  "[fastcursorright]";
   public static final String MNEMONIC_FAST_CURSOR_LEFT   =  "[fastcursorleft]";

   // AID-Generating Keys
   public static final int AID_CLEAR = 0xBD;
   public static final int AID_ENTER = 0xF1;
   public static final int AID_HELP = 0xF3;
   public static final int AID_ROLL_UP = 0xF4;
   public static final int AID_ROLL_DOWN = 0xF5;
   public static final int AID_ROLL_LEFT = 0xD9;
   public static final int AID_ROLL_RIGHT = 0xDA;
   public static final int AID_PRINT = 0xF6;
   public static final int AID_PF1 = 0x31;
   public static final int AID_PF2 = 0x32;
   public static final int AID_PF3 = 0x33;
   public static final int AID_PF4 = 0x34;
   public static final int AID_PF5 = 0x35;
   public static final int AID_PF6 = 0x36;
   public static final int AID_PF7 = 0x37;
   public static final int AID_PF8 = 0x38;
   public static final int AID_PF9 = 0x39;
   public static final int AID_PF10 = 0x3A;
   public static final int AID_PF11 = 0x3B;
   public static final int AID_PF12 = 0x3C;
   public static final int AID_PF13 = 0xB1;
   public static final int AID_PF14 = 0xB2;
   public static final int AID_PF15 = 0xB3;
   public static final int AID_PF16 = 0xB4;
   public static final int AID_PF17 = 0xB5;
   public static final int AID_PF18 = 0xB6;
   public static final int AID_PF19 = 0xB7;
   public static final int AID_PF20 = 0xB8;
   public static final int AID_PF21 = 0xB9;
   public static final int AID_PF22 = 0xBA;
   public static final int AID_PF23 = 0xBB;
   public static final int AID_PF24 = 0xBC;

   // negative response categories
   public static final int NR_REQUEST_REJECT = 0x08;
   public static final int NR_REQUEST_ERROR = 0x10;
   public static final int NR_STATE_ERROR = 0x20;
   public static final int NR_USAGE_ERROR = 0x40;
   public static final int NR_PATH_ERROR = 0x80;

   // commands
   public static final byte CMD_WRITE_TO_DISPLAY = 0x11; // 17
   public static final byte CMD_CLEAR_UNIT = 0x40; // 64
   public static final byte CMD_CLEAR_UNIT_ALTERNATE = 0x20; // 32
   public static final byte CMD_CLEAR_FORMAT_TABLE = 0x50; // 80
   public static final byte CMD_READ_INPUT_FIELDS = 0x42; // 66
   public static final byte CMD_READ_MDT_FIELDS = 0x52; // 82
   public static final byte CMD_READ_MDT_IMMEDIATE_ALT = (byte)0x83; // 131
//   public static final byte CMD_READ_MDT_FIELDS_ALT = (byte)0x82; // 130
//   public static final byte CMD_READ_IMMEDIATE = 0x72; // 114
   public static final byte CMD_READ_SCREEN_IMMEDIATE = 0x62; // 98
   public static final byte CMD_WRITE_STRUCTURED_FIELD = (byte)243;  // (byte)0xF3 -13
   public static final byte CMD_SAVE_SCREEN = 0x02; // 02
   public static final byte CMD_RESTORE_SCREEN = 0x12; // 18
   public static final byte CMD_WRITE_ERROR_CODE = 0x21; // 33
   public static final byte CMD_WRITE_ERROR_CODE_TO_WINDOW = 0x22; // 34
   public static final byte CMD_ROLL = 0x23; // 35
   public static final byte CMD_READ_SCREEN_TO_PRINT = (byte)0x66; // 102

   // PLANES
   public static final int PLANE_TEXT   =  1;
   public static final int PLANE_COLOR   =  2;
   public static final int PLANE_FIELD   =  3;
   public static final int PLANE_EXTENDED   =  4;
   public static final int PLANE_EXTENDED_GRAPHIC   =  5;
   public static final int PLANE_EXTENDED_FIELD   =  6;
   public static final int PLANE_ATTR   =  7;
   public static final int PLANE_IS_ATTR_PLACE   =  8;

   // COLOR_BG
   public static final char COLOR_BG_BLACK   =  0;
   public static final char COLOR_BG_BLUE   =  1;
   public static final char COLOR_BG_GREEN   =  2;
   public static final char COLOR_BG_CYAN   =  3;
   public static final char COLOR_BG_RED   =  4;
   public static final char COLOR_BG_MAGENTA   =  5;
   public static final char COLOR_BG_YELLOW   =  6;
   public static final char COLOR_BG_WHITE   =  7;

   // COLOR_FG
   public static final char COLOR_FG_BLACK   =  0;
   public static final char COLOR_FG_BLUE   =  1;
   public static final char COLOR_FG_GREEN   =  2;
   public static final char COLOR_FG_CYAN   =  3;
   public static final char COLOR_FG_RED   =  4;
   public static final char COLOR_FG_MAGENTA   =  5;
   public static final char COLOR_FG_YELLOW   =  6;
   public static final char COLOR_FG_WHITE   =  7;
   public static final char COLOR_FG_BROWN   =  0xE;
   public static final char COLOR_FG_GRAY   =  8;
   public static final char COLOR_FG_LIGHT_BLUE   =  9;
   public static final char COLOR_FG_LIGHT_GREEN   =  0xA;
   public static final char COLOR_FG_LIGHT_CYAN   =  0xB;
   public static final char COLOR_FG_LIGHT_RED   =  0xC;
   public static final char COLOR_FG_LIGHT_MAGENTA   =  0xD;
   public static final char COLOR_FG_WHITE_HIGH   =  0xF;

   public static final int EXTENDED_5250_REVERSE   =  0x10;
   public static final int EXTENDED_5250_UNDERLINE   =  0x08;
   public static final int EXTENDED_5250_BLINK   =  0x04;
   public static final int EXTENDED_5250_COL_SEP   =  0x02;
   public static final int EXTENDED_5250_NON_DSP   =  0x01;

   public static final char ATTR_32 = (COLOR_BG_BLACK << 8 & 0xff00) |
   													(COLOR_FG_GREEN & 0xff);
   public static final char ATTR_33 = (COLOR_BG_GREEN << 8 & 0xff00) |
   													(COLOR_FG_BLACK & 0xff);
   public static final char ATTR_34 = (COLOR_BG_BLACK << 8 & 0xff00) |
   													(COLOR_FG_WHITE & 0xff);
   public static final char ATTR_35 = (COLOR_BG_WHITE << 8 & 0xff00) |
   													(COLOR_FG_BLACK & 0xff);
   public static final char ATTR_36 = (COLOR_BG_BLACK << 8 & 0xff00) |
   													(COLOR_FG_GREEN & 0xff);
   public static final char ATTR_37 = (COLOR_BG_GREEN << 8 & 0xff00) |
   													(COLOR_FG_BLACK & 0xff);
   public static final char ATTR_38 = (COLOR_BG_BLACK << 8 & 0xff00) |
   													(COLOR_FG_WHITE & 0xff);
   public static final char ATTR_40 = (COLOR_BG_BLACK << 8 & 0xff00) |
   													(COLOR_FG_RED & 0xff);
   public static final char ATTR_41 = (COLOR_BG_RED << 8 & 0xff00) |
   													(COLOR_FG_BLACK & 0xff);
   public static final char ATTR_42 = (COLOR_BG_BLACK << 8 & 0xff00) |
														(COLOR_FG_RED & 0xff);
   public static final char ATTR_43 = (COLOR_BG_RED << 8 & 0xff00) |
   													(COLOR_FG_BLACK & 0xff);

   public static final char ATTR_44 = (COLOR_BG_BLACK << 8 & 0xff00) |
   													(COLOR_FG_RED & 0xff);
   public static final char ATTR_45 = ( COLOR_BG_RED << 8 & 0xff00) |
   													( COLOR_FG_BLACK & 0xff);
   public static final char ATTR_46 = (COLOR_BG_BLACK << 8 & 0xff00) |
														(COLOR_FG_RED & 0xff);

   public static final char ATTR_48 = (COLOR_BG_BLACK << 8 & 0xff00) |
      												(COLOR_FG_CYAN & 0xff);
   public static final char ATTR_49 = (COLOR_BG_CYAN << 8 & 0xff00) |
   													(COLOR_FG_BLACK & 0xff);
   public static final char ATTR_50 = (COLOR_BG_BLACK << 8 & 0xff00) |
   													(COLOR_FG_YELLOW & 0xff);

   public static final char ATTR_51 = (COLOR_BG_YELLOW << 8 & 0xff00) |
   													(COLOR_FG_BLACK & 0xff);
   public static final char ATTR_52 = ( COLOR_BG_BLACK << 8 & 0xff00) |
   													( COLOR_FG_CYAN & 0xff);
   public static final char ATTR_53 = ( COLOR_BG_CYAN << 8 & 0xff00) |
   													( COLOR_FG_BLACK & 0xff);
   public static final char ATTR_54 = ( COLOR_BG_BLACK << 8 & 0xff00) |
   													( COLOR_FG_YELLOW & 0xff);
   public static final char ATTR_56 = ( COLOR_BG_BLACK << 8 & 0xff00) |
   													( COLOR_FG_MAGENTA & 0xff);
   public static final char ATTR_57 = ( COLOR_BG_MAGENTA << 8 & 0xff00) |
   													( COLOR_FG_BLACK & 0xff);
   public static final char ATTR_58 = ( COLOR_BG_BLACK << 8 & 0xff00) |
   													( COLOR_FG_BLUE & 0xff);
   public static final char ATTR_59 = ( COLOR_BG_BLUE << 8 & 0xff00) |
   													( COLOR_FG_BLACK & 0xff);
   public static final char ATTR_60 = ( COLOR_BG_BLACK << 8 & 0xff00) |
   													( COLOR_FG_MAGENTA & 0xff);
   public static final char ATTR_61 = ( COLOR_BG_MAGENTA << 8 & 0xff00) |
   													( COLOR_FG_BLACK & 0xff);
   public static final char ATTR_62 = ( COLOR_BG_BLACK << 8 & 0xff00) |
   													( COLOR_FG_BLUE & 0xff);

   public static final int NO_GUI = 0;
   public static final int UPPER_LEFT = 1;
   public static final int UPPER = 2;
   public static final int UPPER_RIGHT = 3;
   public static final int GUI_LEFT = 4;
   public static final int GUI_RIGHT = 5;
   public static final int LOWER_LEFT = 6;
   public static final int BOTTOM = 7;
   public static final int LOWER_RIGHT = 8;
   public static final int FIELD_LEFT = 9;
   public static final int FIELD_RIGHT = 10;
   public static final int FIELD_MIDDLE = 11;
   public static final int FIELD_ONE = 12;
   public static final int BUTTON_LEFT = 13;
   public static final int BUTTON_RIGHT = 14;
   public static final int BUTTON_MIDDLE = 15;
   public static final int BUTTON_ONE = 16;
   public static final int BUTTON_LEFT_UP = 17;
   public static final int BUTTON_RIGHT_UP = 18;
   public static final int BUTTON_MIDDLE_UP = 19;
   public static final int BUTTON_ONE_UP = 20;
   public static final int BUTTON_LEFT_DN = 21;
   public static final int BUTTON_RIGHT_DN = 22;
   public static final int BUTTON_MIDDLE_DN = 23;
   public static final int BUTTON_ONE_DN = 24;
   public static final int BUTTON_LEFT_EB = 25;
   public static final int BUTTON_RIGHT_EB = 26;
   public static final int BUTTON_MIDDLE_EB = 27;
   public static final int BUTTON_SB_UP = 28;
   public static final int BUTTON_SB_DN = 29;
   public static final int BUTTON_SB_GUIDE = 30;
   public static final int BUTTON_SB_THUMB = 31;
   public static final int BUTTON_LAST = 31;

}
