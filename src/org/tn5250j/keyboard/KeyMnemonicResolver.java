/*
 * Title: KeypadMnemonicResolver.java
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

import org.tn5250j.tools.LangTool;

import java.util.Arrays;

public class KeyMnemonicResolver {

  public int findMnemonicValue(String mnemonicStr) {
    for (KeyMnemonic mnemonic : KeyMnemonic.values()) {
      if (mnemonic.mnemonic.equals(mnemonicStr))
        return mnemonic.value;
    }
    return 0;
  }

  public KeyMnemonic findMnemonic(String mnemonicStr) {
    for (KeyMnemonic mnemonic : KeyMnemonic.values()) {
      if (mnemonic.mnemonic.equals(mnemonicStr))
        return mnemonic;
    }
    return null;
  }

  public String[] getMnemonics() {
    String[] result = new String[KeyMnemonic.values().length];
    int i = 0;
    for (KeyMnemonic keyMnemonic : KeyMnemonic.values()) {
      result[i++] = keyMnemonic.mnemonic;
    }
    return result;
  }

  public String[] getMnemonicsSorted() {
    String[] mnemonics = getMnemonics();
    Arrays.sort(mnemonics);
    return mnemonics;
  }

  public String[] getMnemonicDescriptions() {
    KeyMnemonic[] mnemonicData = KeyMnemonic.values();
    String[] result = new String[KeyMnemonic.values().length];
    int i = 0;
    for (KeyMnemonic mnemonic : mnemonicData) {
      result[i++] = getDescription(mnemonic);
    }
    return result;
  }

  public String getDescription(KeyMnemonic mnemonic) {
    return LangTool.getString("key." + mnemonic.mnemonic);
  }
}
