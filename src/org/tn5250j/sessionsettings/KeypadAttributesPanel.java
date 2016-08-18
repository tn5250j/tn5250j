package org.tn5250j.sessionsettings;
/*
 * Title: KeypadAttributesPanel
 * Copyright:   Copyright (c) 2001
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.5
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

import org.tn5250j.SessionConfig;
import org.tn5250j.tools.LangTool;

import javax.swing.*;
import java.awt.*;

class KeypadAttributesPanel extends AttributesPanel {

  private static final String YES = "Yes";
  private static final String NO = "No";
  private static final String KEYPAD = "keypad";

  private static final long serialVersionUID = 1L;
  private JCheckBox kpCheck;

  KeypadAttributesPanel(SessionConfig config) {
    super(config, "KP");
  }

  /**
   * Component initialization
   */
  public void initPanel() throws Exception {

    setLayout(new BorderLayout());
    contentPane = new JPanel();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    add(contentPane, BorderLayout.NORTH);

    // define Key Pad panel
    JPanel kpp = new JPanel();
    kpp.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.kpp")));

    kpCheck = new JCheckBox(LangTool.getString("sa.kpCheck"));
    kpCheck.setSelected(YES.equals(getStringProperty(KEYPAD)));
    kpp.add(kpCheck);

    contentPane.add(kpp);
  }

  @Override
  public void applyAttributes() {
    final String newValue = kpCheck.isSelected() ? YES : NO;
    changes.firePropertyChange(this, KEYPAD, getStringProperty(KEYPAD), newValue);
    setProperty(KEYPAD, newValue);
  }
}