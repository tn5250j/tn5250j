package org.tn5250j.mcp;

import java.util.HashMap;
import java.util.Map;

import org.tn5250j.keyboard.KeyMnemonic;

/**
 * Maps a 5250 AID byte to the sendKeys mnemonic that produces it, e.g. 0xF1 to "[enter]".
 * Built from tn5250j's KeyMnemonic table, where AID-generating keys use the AID byte as their value.
 */
public final class AidNames {

    private static final int MAX_AID_BYTE = 0xFF;
    private static final Map<Integer, String> NAMES_BY_AID = buildNames();

    private AidNames() {
    }

    public static String nameOf(int aid) {
        String name = NAMES_BY_AID.get(aid);
        return name != null ? name : String.format("[aid:0x%02x]", aid);
    }

    private static Map<Integer, String> buildNames() {
        Map<Integer, String> names = new HashMap<>();
        for (KeyMnemonic key : KeyMnemonic.values()) {
            if (key.value <= MAX_AID_BYTE) {
                names.put(key.value, key.mnemonic);
            }
        }
        return Map.copyOf(names);
    }
}
