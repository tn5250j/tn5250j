package org.tn5250j.settings;
/**
 * Title: HotspotAttributesPanel
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.5
 *
 * Description:
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

import java.awt.*;
import javax.swing.*;

import org.tn5250j.tools.*;
import org.tn5250j.SessionConfig;

public class HotspotAttributesPanel extends AttributesPanel {

   private static final long serialVersionUID = 1L;
JCheckBox hsCheck;
   JTextField hsMore;
   JTextField hsBottom;

   public HotspotAttributesPanel(SessionConfig config ) {
      super(config,"HS");
   }

   /**Component initialization*/
   public void initPanel() throws Exception  {

      setLayout(new BorderLayout());
      contentPane = new JPanel();
      contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
      add(contentPane,BorderLayout.NORTH);

      // define hsPanel panel
      JPanel hsp = new JPanel();
      hsp.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.hsp")));
      hsCheck = new JCheckBox(LangTool.getString("sa.hsCheck"));

      if (getStringProperty("hotspots").equals("Yes"))
         hsCheck.setSelected(true);

      hsp.add(hsCheck);

      // define assignment panel
      JPanel hsap = new JPanel();
      hsap.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.hsap")));
      hsap.setLayout(new AlignLayout(2,5,5));

      JLabel moreLabel = new JLabel(LangTool.getString("sa.hsMore"));
      JLabel bottomLabel = new JLabel(LangTool.getString("sa.hsBottom"));
      hsMore = new JTextField(getStringProperty("hsMore"),20);
      hsBottom = new JTextField(getStringProperty("hsBottom"),20);

      hsap.add(moreLabel);
      hsap.add(hsMore);
      hsap.add(bottomLabel);
      hsap.add(hsBottom);

      contentPane.add(hsp);
      contentPane.add(hsap);

   }

   public void save() {

   }

   public void applyAttributes() {

      if (hsCheck.isSelected()) {
         changes.firePropertyChange(this,"hotspots",
                           getStringProperty("hotspots"),
                           "Yes");
         setProperty("hotspots","Yes");
      }
      else {
         changes.firePropertyChange(this,"hotspots",
                           getStringProperty("hotspots"),
                           "No");
         setProperty("hotspots","No");
      }

      changes.firePropertyChange(this,"hsMore",
                        getStringProperty("hsMore"),
                        hsMore.getText());
      setProperty("hsMore",hsMore.getText());

      changes.firePropertyChange(this,"hsBottom",
                        getStringProperty("hsBottom"),
                        hsBottom.getText());
      setProperty("hsBottom",hsBottom.getText());

   }
}