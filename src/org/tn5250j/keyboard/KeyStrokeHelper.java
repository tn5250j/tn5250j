/**
 *
 */
package org.tn5250j.keyboard;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.tn5250j.Temporary;

import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.scene.input.KeyEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class KeyStrokeHelper {
    private static final Map<Integer, KeyCode> codes = createCodeMap();

    public static final int SHIFT_MASK = 1 << 0;
    public static final int CTRL_MASK = 1 << 1;
    public static final int META_MASK = 1 << 2;
    public static final int ALT_MASK = 1 << 3;

    /**
     * Used for generated code.
     * @param args
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("deprecation")
    public static void main(final String[] args) throws IllegalArgumentException, IllegalAccessException {
        final KeyCode[] values = KeyCode.values();
        for (final KeyCode code : values) {
            final int intCode = code.impl_getCode();
            System.out.println("map.put(" + intCode + ", KeyCode." + getFieldName(code) + ");");
        }
    }

    private static KeyCode getFieldName(final KeyCode code) throws IllegalArgumentException, IllegalAccessException {
        final Field[] fields = KeyCode.class.getDeclaredFields();
        for (final Field field : fields) {
            final Object k = field.get(null);
            if (k == code) {
                return (KeyCode) k;
            }
        }
        return null;
    }

    private static Map<Integer, KeyCode> createCodeMap() {
        final Map<Integer, KeyCode> map = new HashMap<>();
        map.put(10, KeyCode.ENTER);
        map.put(8, KeyCode.BACK_SPACE);
        map.put(9, KeyCode.TAB);
        map.put(3, KeyCode.CANCEL);
        map.put(12, KeyCode.CLEAR);
        map.put(16, KeyCode.SHIFT);
        map.put(17, KeyCode.CONTROL);
        map.put(18, KeyCode.ALT);
        map.put(19, KeyCode.PAUSE);
        map.put(20, KeyCode.CAPS);
        map.put(27, KeyCode.ESCAPE);
        map.put(32, KeyCode.SPACE);
        map.put(33, KeyCode.PAGE_UP);
        map.put(34, KeyCode.PAGE_DOWN);
        map.put(35, KeyCode.END);
        map.put(36, KeyCode.HOME);
        map.put(37, KeyCode.LEFT);
        map.put(38, KeyCode.UP);
        map.put(39, KeyCode.RIGHT);
        map.put(40, KeyCode.DOWN);
        map.put(44, KeyCode.COMMA);
        map.put(45, KeyCode.MINUS);
        map.put(46, KeyCode.PERIOD);
        map.put(47, KeyCode.SLASH);
        map.put(48, KeyCode.DIGIT0);
        map.put(49, KeyCode.DIGIT1);
        map.put(50, KeyCode.DIGIT2);
        map.put(51, KeyCode.DIGIT3);
        map.put(52, KeyCode.DIGIT4);
        map.put(53, KeyCode.DIGIT5);
        map.put(54, KeyCode.DIGIT6);
        map.put(55, KeyCode.DIGIT7);
        map.put(56, KeyCode.DIGIT8);
        map.put(57, KeyCode.DIGIT9);
        map.put(59, KeyCode.SEMICOLON);
        map.put(61, KeyCode.EQUALS);
        map.put(65, KeyCode.A);
        map.put(66, KeyCode.B);
        map.put(67, KeyCode.C);
        map.put(68, KeyCode.D);
        map.put(69, KeyCode.E);
        map.put(70, KeyCode.F);
        map.put(71, KeyCode.G);
        map.put(72, KeyCode.H);
        map.put(73, KeyCode.I);
        map.put(74, KeyCode.J);
        map.put(75, KeyCode.K);
        map.put(76, KeyCode.L);
        map.put(77, KeyCode.M);
        map.put(78, KeyCode.N);
        map.put(79, KeyCode.O);
        map.put(80, KeyCode.P);
        map.put(81, KeyCode.Q);
        map.put(82, KeyCode.R);
        map.put(83, KeyCode.S);
        map.put(84, KeyCode.T);
        map.put(85, KeyCode.U);
        map.put(86, KeyCode.V);
        map.put(87, KeyCode.W);
        map.put(88, KeyCode.X);
        map.put(89, KeyCode.Y);
        map.put(90, KeyCode.Z);
        map.put(91, KeyCode.OPEN_BRACKET);
        map.put(92, KeyCode.BACK_SLASH);
        map.put(93, KeyCode.CLOSE_BRACKET);
        map.put(96, KeyCode.NUMPAD0);
        map.put(97, KeyCode.NUMPAD1);
        map.put(98, KeyCode.NUMPAD2);
        map.put(99, KeyCode.NUMPAD3);
        map.put(100, KeyCode.NUMPAD4);
        map.put(101, KeyCode.NUMPAD5);
        map.put(102, KeyCode.NUMPAD6);
        map.put(103, KeyCode.NUMPAD7);
        map.put(104, KeyCode.NUMPAD8);
        map.put(105, KeyCode.NUMPAD9);
        map.put(106, KeyCode.MULTIPLY);
        map.put(107, KeyCode.ADD);
        map.put(108, KeyCode.SEPARATOR);
        map.put(109, KeyCode.SUBTRACT);
        map.put(110, KeyCode.DECIMAL);
        map.put(111, KeyCode.DIVIDE);
        map.put(127, KeyCode.DELETE);
        map.put(144, KeyCode.NUM_LOCK);
        map.put(145, KeyCode.SCROLL_LOCK);
        map.put(112, KeyCode.F1);
        map.put(113, KeyCode.F2);
        map.put(114, KeyCode.F3);
        map.put(115, KeyCode.F4);
        map.put(116, KeyCode.F5);
        map.put(117, KeyCode.F6);
        map.put(118, KeyCode.F7);
        map.put(119, KeyCode.F8);
        map.put(120, KeyCode.F9);
        map.put(121, KeyCode.F10);
        map.put(122, KeyCode.F11);
        map.put(123, KeyCode.F12);
        map.put(61440, KeyCode.F13);
        map.put(61441, KeyCode.F14);
        map.put(61442, KeyCode.F15);
        map.put(61443, KeyCode.F16);
        map.put(61444, KeyCode.F17);
        map.put(61445, KeyCode.F18);
        map.put(61446, KeyCode.F19);
        map.put(61447, KeyCode.F20);
        map.put(61448, KeyCode.F21);
        map.put(61449, KeyCode.F22);
        map.put(61450, KeyCode.F23);
        map.put(61451, KeyCode.F24);
        map.put(154, KeyCode.PRINTSCREEN);
        map.put(155, KeyCode.INSERT);
        map.put(156, KeyCode.HELP);
        map.put(157, KeyCode.META);
        map.put(192, KeyCode.BACK_QUOTE);
        map.put(222, KeyCode.QUOTE);
        map.put(224, KeyCode.KP_UP);
        map.put(225, KeyCode.KP_DOWN);
        map.put(226, KeyCode.KP_LEFT);
        map.put(227, KeyCode.KP_RIGHT);
        map.put(128, KeyCode.DEAD_GRAVE);
        map.put(129, KeyCode.DEAD_ACUTE);
        map.put(130, KeyCode.DEAD_CIRCUMFLEX);
        map.put(131, KeyCode.DEAD_TILDE);
        map.put(132, KeyCode.DEAD_MACRON);
        map.put(133, KeyCode.DEAD_BREVE);
        map.put(134, KeyCode.DEAD_ABOVEDOT);
        map.put(135, KeyCode.DEAD_DIAERESIS);
        map.put(136, KeyCode.DEAD_ABOVERING);
        map.put(137, KeyCode.DEAD_DOUBLEACUTE);
        map.put(138, KeyCode.DEAD_CARON);
        map.put(139, KeyCode.DEAD_CEDILLA);
        map.put(140, KeyCode.DEAD_OGONEK);
        map.put(141, KeyCode.DEAD_IOTA);
        map.put(142, KeyCode.DEAD_VOICED_SOUND);
        map.put(143, KeyCode.DEAD_SEMIVOICED_SOUND);
        map.put(150, KeyCode.AMPERSAND);
        map.put(151, KeyCode.ASTERISK);
        map.put(152, KeyCode.QUOTEDBL);
        map.put(153, KeyCode.LESS);
        map.put(160, KeyCode.GREATER);
        map.put(161, KeyCode.BRACELEFT);
        map.put(162, KeyCode.BRACERIGHT);
        map.put(512, KeyCode.AT);
        map.put(513, KeyCode.COLON);
        map.put(514, KeyCode.CIRCUMFLEX);
        map.put(515, KeyCode.DOLLAR);
        map.put(516, KeyCode.EURO_SIGN);
        map.put(517, KeyCode.EXCLAMATION_MARK);
        map.put(518, KeyCode.INVERTED_EXCLAMATION_MARK);
        map.put(519, KeyCode.LEFT_PARENTHESIS);
        map.put(520, KeyCode.NUMBER_SIGN);
        map.put(521, KeyCode.PLUS);
        map.put(522, KeyCode.RIGHT_PARENTHESIS);
        map.put(523, KeyCode.UNDERSCORE);
        map.put(524, KeyCode.WINDOWS);
        map.put(525, KeyCode.CONTEXT_MENU);
        map.put(24, KeyCode.FINAL);
        map.put(28, KeyCode.CONVERT);
        map.put(29, KeyCode.NONCONVERT);
        map.put(30, KeyCode.ACCEPT);
        map.put(31, KeyCode.MODECHANGE);
        map.put(21, KeyCode.KANA);
        map.put(25, KeyCode.KANJI);
        map.put(240, KeyCode.ALPHANUMERIC);
        map.put(241, KeyCode.KATAKANA);
        map.put(242, KeyCode.HIRAGANA);
        map.put(243, KeyCode.FULL_WIDTH);
        map.put(244, KeyCode.HALF_WIDTH);
        map.put(245, KeyCode.ROMAN_CHARACTERS);
        map.put(256, KeyCode.ALL_CANDIDATES);
        map.put(257, KeyCode.PREVIOUS_CANDIDATE);
        map.put(258, KeyCode.CODE_INPUT);
        map.put(259, KeyCode.JAPANESE_KATAKANA);
        map.put(260, KeyCode.JAPANESE_HIRAGANA);
        map.put(261, KeyCode.JAPANESE_ROMAN);
        map.put(262, KeyCode.KANA_LOCK);
        map.put(263, KeyCode.INPUT_METHOD_ON_OFF);
        map.put(65489, KeyCode.CUT);
        map.put(65485, KeyCode.COPY);
        map.put(65487, KeyCode.PASTE);
        map.put(65483, KeyCode.UNDO);
        map.put(65481, KeyCode.AGAIN);
        map.put(65488, KeyCode.FIND);
        map.put(65482, KeyCode.PROPS);
        map.put(65480, KeyCode.STOP);
        map.put(65312, KeyCode.COMPOSE);
        map.put(65406, KeyCode.ALT_GRAPH);
        map.put(65368, KeyCode.BEGIN);
        map.put(0, KeyCode.UNDEFINED);
        map.put(4096, KeyCode.SOFTKEY_0);
        map.put(4097, KeyCode.SOFTKEY_1);
        map.put(4098, KeyCode.SOFTKEY_2);
        map.put(4099, KeyCode.SOFTKEY_3);
        map.put(4100, KeyCode.SOFTKEY_4);
        map.put(4101, KeyCode.SOFTKEY_5);
        map.put(4102, KeyCode.SOFTKEY_6);
        map.put(4103, KeyCode.SOFTKEY_7);
        map.put(4104, KeyCode.SOFTKEY_8);
        map.put(4105, KeyCode.SOFTKEY_9);
        map.put(4106, KeyCode.GAME_A);
        map.put(4107, KeyCode.GAME_B);
        map.put(4108, KeyCode.GAME_C);
        map.put(4109, KeyCode.GAME_D);
        map.put(4110, KeyCode.STAR);
        map.put(4111, KeyCode.POUND);
        map.put(409, KeyCode.POWER);
        map.put(457, KeyCode.INFO);
        map.put(403, KeyCode.COLORED_KEY_0);
        map.put(404, KeyCode.COLORED_KEY_1);
        map.put(405, KeyCode.COLORED_KEY_2);
        map.put(406, KeyCode.COLORED_KEY_3);
        map.put(414, KeyCode.EJECT_TOGGLE);
        map.put(415, KeyCode.PLAY);
        map.put(416, KeyCode.RECORD);
        map.put(417, KeyCode.FAST_FWD);
        map.put(412, KeyCode.REWIND);
        map.put(424, KeyCode.TRACK_PREV);
        map.put(425, KeyCode.TRACK_NEXT);
        map.put(427, KeyCode.CHANNEL_UP);
        map.put(428, KeyCode.CHANNEL_DOWN);
        map.put(447, KeyCode.VOLUME_UP);
        map.put(448, KeyCode.VOLUME_DOWN);
        map.put(449, KeyCode.MUTE);
        map.put(768, KeyCode.COMMAND);
        map.put(-1, KeyCode.SHORTCUT);
        return map;
    }

    public static KeyCode getCode(final int code) {
        return codes.get(code);
    }

    public static int getModifiersFlag(final KeyEvent e) {
        int mask = 0;
        if (e.isShiftDown())
            mask |= SHIFT_MASK;
        if (e.isControlDown())
            mask |= CTRL_MASK;
        if (e.isAltDown())
            mask |= ALT_MASK;
        if (e.isMetaDown())
            mask |= META_MASK;
        return mask;
    }

    public static String getKeyModifiersText(final KeyEvent e) {
        final List<Modifier> modifiers = new LinkedList<>();

        if (e.isShiftDown())
            modifiers.add(KeyCombination.SHIFT_DOWN);
        if (e.isControlDown())
            modifiers.add(KeyCombination.CONTROL_DOWN);
        if (e.isAltDown())
            modifiers.add(KeyCombination.ALT_DOWN);

        return new KeyCombination(modifiers.toArray(new Modifier[modifiers.size()])) {}.getDisplayText();
    }

    public static boolean isActionKey(final KeyEvent e) {
        switch (e.getCode()) {
        case HOME:
        case END:
        case PAGE_UP:
        case PAGE_DOWN:
        case UP:
        case DOWN:
        case LEFT:
        case RIGHT:
        case BEGIN:

        case KP_LEFT:
        case KP_UP:
        case KP_RIGHT:
        case KP_DOWN:

        case F1:
        case F2:
        case F3:
        case F4:
        case F5:
        case F6:
        case F7:
        case F8:
        case F9:
        case F10:
        case F11:
        case F12:
        case F13:
        case F14:
        case F15:
        case F16:
        case F17:
        case F18:
        case F19:
        case F20:
        case F21:
        case F22:
        case F23:
        case F24:
        case PRINTSCREEN:
        case SCROLL_LOCK:
        case CAPS:
        case NUM_LOCK:
        case PAUSE:
        case INSERT:

        case FINAL:
        case CONVERT:
        case NONCONVERT:
        case ACCEPT:
        case MODECHANGE:
        case KANA:
        case KANJI:
        case ALPHANUMERIC:
        case KATAKANA:
        case HIRAGANA:
        case FULL_WIDTH:
        case HALF_WIDTH:
        case ROMAN_CHARACTERS:
        case ALL_CANDIDATES:
        case PREVIOUS_CANDIDATE:
        case CODE_INPUT:
        case JAPANESE_KATAKANA:
        case JAPANESE_HIRAGANA:
        case JAPANESE_ROMAN:
        case KANA_LOCK:
        case INPUT_METHOD_ON_OFF:

        case AGAIN:
        case UNDO:
        case COPY:
        case PASTE:
        case CUT:
        case FIND:
        case PROPS:
        case STOP:

        case HELP:
        case WINDOWS:
        case CONTEXT_MENU:
            return true;
      }
      return false;
    }

    @Temporary
    public static KeyEvent toFxKeyEvent(final java.awt.event.KeyEvent e, final EventTarget target) {
        final int mod = e.getModifiersEx();

        return new KeyEvent(e.getSource(), target, getKeyType(e.getID()),
                new String(new char[] {keyCharToEmbedKeyChar(e.getKeyChar())}),
                java.awt.event.KeyEvent.getKeyText(e.getKeyCode()),
                getCode(e.getKeyCode()),
                mod * SHIFT_MASK != 0,
                mod * CTRL_MASK != 0,
                mod * ALT_MASK != 0,
                mod * META_MASK != 0);
    }
    static EventType<KeyEvent> getKeyType(final int id) {
        switch (id) {
            case java.awt.event.KeyEvent.KEY_PRESSED:
                return KeyEvent.KEY_PRESSED;
            case java.awt.event.KeyEvent.KEY_RELEASED:
                return KeyEvent.KEY_RELEASED;
            case java.awt.event.KeyEvent.KEY_TYPED:
                return KeyEvent.KEY_TYPED;
        }
        return KeyEvent.ANY;
    }

    static char keyCharToEmbedKeyChar(final char ch) {
        // Convert Swing LF character to Fx CR character.
        return ch == '\n' ? '\r' : ch;
    }
}
