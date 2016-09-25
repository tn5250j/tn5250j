/*
 * Title: KeypadMnemonicSerializer.java
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

import java.util.ArrayList;
import java.util.List;

public class KeypadMnemonicSerializer {

  private final KeypadMnemonicResolver keypadMnemonicResolver = new KeypadMnemonicResolver();

  public String serialize(KeypadMnemonic[] keypadMnemonics) {
    StringBuilder sb = new StringBuilder();
    if (keypadMnemonics != null) {
      for (int i = 0; i < keypadMnemonics.length; i++) {
        if (i > 0) sb.append(',');
        sb.append(keypadMnemonics[i].mnemonic);
      }
    }
    return sb.toString();
  }

  public KeypadMnemonic[] deserialize(String keypadMnemonics) {
    if (keypadMnemonics == null) return new KeypadMnemonic[0];
    String[] parts = keypadMnemonics.split(",");
    List<KeypadMnemonic> result = new ArrayList<KeypadMnemonic>();
    for (String part : parts) {
      KeypadMnemonic mnemonic = keypadMnemonicResolver.findMnemonic(part.trim());
      if (mnemonic != null) {
        result.add(mnemonic);
      }
    }
    return result.toArray(new KeypadMnemonic[result.size()]);
  }

}
