package org.tn5250j.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.tn5250j.TN5250jConstants;

class AidNamesTest {

    @Test
    void enter() {
        assertEquals("[enter]", AidNames.nameOf(TN5250jConstants.AID_ENTER));
    }

    @Test
    void functionKeysAcrossBothRanges() {
        assertEquals("[pf1]", AidNames.nameOf(TN5250jConstants.AID_PF1));
        assertEquals("[pf3]", AidNames.nameOf(TN5250jConstants.AID_PF3));
        assertEquals("[pf12]", AidNames.nameOf(TN5250jConstants.AID_PF12));
        assertEquals("[pf13]", AidNames.nameOf(TN5250jConstants.AID_PF13));
        assertEquals("[pf24]", AidNames.nameOf(TN5250jConstants.AID_PF24));
    }

    @Test
    void pagingKeysUseTheMnemonicThatSendsThatAid() {
        assertEquals("[pgup]", AidNames.nameOf(TN5250jConstants.AID_ROLL_UP));
        assertEquals("[pgdown]", AidNames.nameOf(TN5250jConstants.AID_ROLL_DOWN));
    }

    @Test
    void otherAidKeys() {
        assertEquals("[clear]", AidNames.nameOf(TN5250jConstants.AID_CLEAR));
        assertEquals("[help]", AidNames.nameOf(TN5250jConstants.AID_HELP));
        assertEquals("[hostprint]", AidNames.nameOf(TN5250jConstants.AID_PRINT));
    }

    @Test
    void unknownAidIsReportedInHex() {
        assertEquals("[aid:0x99]", AidNames.nameOf(0x99));
    }
}
