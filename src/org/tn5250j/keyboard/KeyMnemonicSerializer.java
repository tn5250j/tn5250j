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

public class KeyMnemonicSerializer {

  private final KeyMnemonicResolver keyMnemonicResolver = new KeyMnemonicResolver();

  public String serialize(KeyMnemonic[] keyMnemonics) {
    StringBuilder sb = new StringBuilder();
    if (keyMnemonics != null) {
      for (int i = 0; i < keyMnemonics.length; i++) {
        if (i > 0) sb.append(',');
        sb.append(keyMnemonics[i].mnemonic);
      }
    }
    return sb.toString();
  }

  public KeyMnemonic[] deserialize(String keypadMnemonics) {
    if (keypadMnemonics == null) return new KeyMnemonic[0];
    String[] parts = keypadMnemonics.split(",");
    List<KeyMnemonic> result = new ArrayList<KeyMnemonic>();
    for (String part : parts) {
      KeyMnemonic mnemonic = keyMnemonicResolver.findMnemonic(part.trim());
      if (mnemonic != null) {
        result.add(mnemonic);
      }
    }
    return result.toArray(new KeyMnemonic[result.size()]);
  }

}
