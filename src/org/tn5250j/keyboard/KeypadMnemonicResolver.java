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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class KeypadMnemonicResolver {

  public int getMnemonicValue(String mnemonicStr) {
    KeypadMnemonic[] mnemonics = KeypadMnemonic.values();
    for (KeypadMnemonic mnemonic : mnemonics) {
      if (mnemonic.mnemonic.equals(mnemonicStr))
        return mnemonic.value;
    }
    return 0;
  }

  public String[] getMnemonicsSorted() {
    String[] mnemonics = KeypadMnemonic.mnemonics();
    Arrays.sort(mnemonics);
    return mnemonics;
  }

  public List<String> getOptionDescriptions() {
    KeypadMnemonic[] mnemonicData = KeypadMnemonic.values();
    List<String> result = new ArrayList<String>(mnemonicData.length);
    for (KeypadMnemonic mnemonic : mnemonicData) {
      result.add(LangTool.getString("key." + mnemonic.mnemonic));
    }
    Collections.sort(result);
    return result;
  }
}
