package org.tn5250j;
/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.4
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

import static org.tn5250j.TN5250jConstants.*;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.tn5250j.tools.LangTool;

public class KeyPad extends JPanel {

	private static final long serialVersionUID = -7460283401326716314L;
	
   BorderLayout borderLayout1 = new BorderLayout();
   JButton[] buttons = new JButton[30];
   int bSize = 0;
   JPanel keyPadTop;
   JPanel keyPadBottom;
   GridLayout gridLayout1 = new GridLayout();
   int numPad = 1;

   public KeyPad() {
      try {
         jbInit();
      }
      catch(Exception ex) {
         ex.printStackTrace();
      }
   }

   void jbInit() throws Exception {
      this.setLayout(borderLayout1);
      keyPadTop = new JPanel();
      keyPadBottom = new JPanel();
      keyPadTop.setLayout(gridLayout1);
      keyPadBottom.setLayout(gridLayout1);
      this.setBorder(BorderFactory.createRaisedBevelBorder());
      this.setBorder(BorderFactory.createLoweredBevelBorder());
      bSize = buttons.length;
      Insets noMargin = new Insets(0,0,0,0);

      for (int x = 0; x < bSize; x++) {

         buttons[x] = new JButton();
         buttons[x].setMargin(noMargin);
      }

      setButtonText(1);
      setButtonTop();
      setButtonBottom();

      this.add(keyPadTop, BorderLayout.NORTH);
      this.add(keyPadBottom,BorderLayout.SOUTH);

   }

   public void addActionListener(ActionListener actionlistener) {
      for(int x = 0; x < bSize; x++)
         buttons[x].addActionListener(actionlistener);

   }

   public void nextPad() {

      if (numPad == 1)
         setButtonText(2);
      else
         setButtonText(1);
   }

   private void setButtonText(int which) {
      numPad = which;

      switch (which) {

//         case 1:
//            buttons[0].setLabel("PF1 ");
//            buttons[0].setActionCommand("PF1");
//            buttons[1].setLabel("PF2 ");
//            buttons[1].setActionCommand("PF2");
//            buttons[2].setLabel("PF3 ");
//            buttons[2].setActionCommand("PF3");
//            buttons[3].setLabel("PF4 ");
//            buttons[3].setActionCommand("PF4");
//            buttons[4].setLabel("PF5 ");
//            buttons[4].setActionCommand("PF5");
//            buttons[5].setLabel("PF6 ");
//            buttons[5].setActionCommand("PF6");
//            buttons[6].setLabel("Enter");
//            buttons[6].setActionCommand("ENTER");
//            buttons[7].setLabel("PgUp");
//            buttons[7].setActionCommand("PGUP");
//            buttons[8].setLabel("Clear");
//            buttons[8].setActionCommand("CLEAR");
//
//            buttons[9].setLabel("PF7 ");
//            buttons[9].setActionCommand("PF7");
//            buttons[10].setLabel("PF8 ");
//            buttons[10].setActionCommand("PF8");
//            buttons[11].setLabel("PF9 ");
//            buttons[11].setActionCommand("PF9");
//            buttons[12].setLabel("PF10");
//            buttons[12].setActionCommand("PF10");
//            buttons[13].setLabel("PF11");
//            buttons[13].setActionCommand("PF11");
//            buttons[14].setLabel("PF12");
//            buttons[14].setActionCommand("PF12");
//            buttons[15].setLabel("SyRq");
//            buttons[15].setActionCommand("SR");
//            buttons[16].setLabel("PgDn");
//            buttons[16].setActionCommand("PGDN");
//
//            break;
//         case 2:
//
//            buttons[0].setLabel("PF13");
//            buttons[0].setActionCommand("PF13");
//            buttons[1].setLabel("PF14");
//            buttons[1].setActionCommand("PF14");
//            buttons[2].setLabel("PF15");
//            buttons[2].setActionCommand("PF15");
//            buttons[3].setLabel("PF16");
//            buttons[3].setActionCommand("PF16");
//            buttons[4].setLabel("PF17");
//            buttons[4].setActionCommand("PF17");
//            buttons[5].setLabel("PF18");
//            buttons[5].setActionCommand("PF18");
//            buttons[6].setLabel("Enter");
//            buttons[6].setActionCommand("ENTER");
//            buttons[7].setLabel("Pg Up");
//            buttons[7].setActionCommand("PGUP");
//            buttons[8].setLabel("Help");
//            buttons[8].setActionCommand("HELP");
//
//
//            buttons[9].setLabel("PF19");
//            buttons[9].setActionCommand("PF19");
//            buttons[10].setLabel("PF20");
//            buttons[10].setActionCommand("PF20");
//            buttons[11].setLabel("PF21");
//            buttons[11].setActionCommand("PF21");
//            buttons[12].setLabel("PF22");
//            buttons[12].setActionCommand("PF22");
//            buttons[13].setLabel("PF23");
//            buttons[13].setActionCommand("PF23");
//            buttons[14].setLabel("PF24");
//            buttons[14].setActionCommand("PF24");
//            buttons[15].setLabel("SyRq");
//            buttons[15].setActionCommand("SR");
//            buttons[16].setLabel("PgDn");
//            buttons[16].setActionCommand("PGDN");
//
//            break;
//      }
//      buttons[17].setLabel("Next Pad");
//      buttons[17].setActionCommand("NXTPAD");

         case 1:
            buttons[0].setText(LangTool.getString("KP_F1",  "PF1"));
            buttons[0].setActionCommand(MNEMONIC_PF1);
            buttons[1].setText(LangTool.getString("KP_F2","PF2"));
            buttons[1].setActionCommand(MNEMONIC_PF2);
            buttons[2].setText(LangTool.getString("KP_F3","PF3"));
            buttons[2].setActionCommand(MNEMONIC_PF3);
            buttons[3].setText(LangTool.getString("KP_F4","PF4"));
            buttons[3].setActionCommand(MNEMONIC_PF4);
            buttons[4].setText(LangTool.getString("KP_F5","PF5"));
            buttons[4].setActionCommand(MNEMONIC_PF5);
            buttons[5].setText(LangTool.getString("KP_F6","PF6"));
            buttons[5].setActionCommand(MNEMONIC_PF6);
            buttons[6].setText(LangTool.getString("KP_F7","PF7"));
            buttons[6].setActionCommand(MNEMONIC_PF7);
            buttons[7].setText(LangTool.getString("KP_F8","PF8"));
            buttons[7].setActionCommand(MNEMONIC_PF8);
            buttons[8].setText(LangTool.getString("KP_F9","PF9"));
            buttons[8].setActionCommand(MNEMONIC_PF9);
            buttons[9].setText(LangTool.getString("KP_F10","PF10"));
            buttons[9].setActionCommand(MNEMONIC_PF10);
            buttons[10].setText(LangTool.getString("KP_F11","PF11"));
            buttons[10].setActionCommand(MNEMONIC_PF11);
            buttons[11].setText(LangTool.getString("KP_F12","PF12"));
            buttons[11].setActionCommand(MNEMONIC_PF12);
            buttons[12].setText(LangTool.getString("KP_ENTER","Enter"));
            buttons[12].setActionCommand(MNEMONIC_ENTER);
            buttons[13].setText(LangTool.getString("KP_PGUP","PgUp"));
            buttons[13].setActionCommand(MNEMONIC_PAGE_UP);
            buttons[14].setText(LangTool.getString("KP_CLEAR","Clear"));
            buttons[14].setActionCommand(MNEMONIC_CLEAR);

            buttons[15].setText(LangTool.getString("KP_F13","PF13"));
            buttons[15].setActionCommand(MNEMONIC_PF13);
            buttons[16].setText(LangTool.getString("KP_F14","PF14"));
            buttons[16].setActionCommand(MNEMONIC_PF14);
            buttons[17].setText(LangTool.getString("KP_F15","PF15"));
            buttons[17].setActionCommand(MNEMONIC_PF15);
            buttons[18].setText(LangTool.getString("KP_F16","PF16"));
            buttons[18].setActionCommand(MNEMONIC_PF16);
            buttons[19].setText(LangTool.getString("KP_F17","PF17"));
            buttons[19].setActionCommand(MNEMONIC_PF17);
            buttons[20].setText(LangTool.getString("KP_F18","PF18"));
            buttons[20].setActionCommand(MNEMONIC_PF18);
            buttons[21].setText(LangTool.getString("KP_F19","PF19"));
            buttons[21].setActionCommand(MNEMONIC_PF19);
            buttons[22].setText(LangTool.getString("KP_F20","PF20"));
            buttons[22].setActionCommand(MNEMONIC_PF20);
            buttons[23].setText(LangTool.getString("KP_F21","PF21"));
            buttons[23].setActionCommand(MNEMONIC_PF21);
            buttons[24].setText(LangTool.getString("KP_F22","PF22"));
            buttons[24].setActionCommand(MNEMONIC_PF22);
            buttons[25].setText(LangTool.getString("KP_F23","PF23"));
            buttons[25].setActionCommand(MNEMONIC_PF23);
            buttons[26].setText(LangTool.getString("KP_F24","PF24"));
            buttons[26].setActionCommand(MNEMONIC_PF24);
            buttons[27].setText(LangTool.getString("KP_SR","SysReq"));
            buttons[27].setActionCommand(MNEMONIC_SYSREQ);
            buttons[28].setText(LangTool.getString("KP_PGDN","PgDn"));
            buttons[28].setActionCommand(MNEMONIC_PAGE_DOWN);

            break;

         case 2:

            buttons[0].setText(LangTool.getString("KP_F1",  "PF1"));
            buttons[0].setActionCommand(MNEMONIC_PF1);
            buttons[1].setText(LangTool.getString("KP_F2","PF2"));
            buttons[1].setActionCommand(MNEMONIC_PF2);
            buttons[2].setText(LangTool.getString("KP_F3","PF3"));
            buttons[2].setActionCommand(MNEMONIC_PF3);
            buttons[3].setText(LangTool.getString("KP_F4","PF4"));
            buttons[3].setActionCommand(MNEMONIC_PF4);
            buttons[4].setText(LangTool.getString("KP_F5","PF5"));
            buttons[4].setActionCommand(MNEMONIC_PF5);
            buttons[5].setText(LangTool.getString("KP_F6","PF6"));
            buttons[5].setActionCommand(MNEMONIC_PF6);
            buttons[6].setText(LangTool.getString("KP_F7","PF7"));
            buttons[6].setActionCommand(MNEMONIC_PF7);
            buttons[7].setText(LangTool.getString("KP_F8","PF8"));
            buttons[7].setActionCommand(MNEMONIC_PF8);
            buttons[8].setText(LangTool.getString("KP_F9","PF9"));
            buttons[8].setActionCommand(MNEMONIC_PF9);
            buttons[9].setText(LangTool.getString("KP_F10","PF10"));
            buttons[9].setActionCommand(MNEMONIC_PF10);
            buttons[10].setText(LangTool.getString("KP_F11","PF11"));
            buttons[10].setActionCommand(MNEMONIC_PF11);
            buttons[11].setText(LangTool.getString("KP_F12","PF12"));
            buttons[11].setActionCommand(MNEMONIC_PF12);
            buttons[12].setText(LangTool.getString("KP_ENTER","Enter"));
            buttons[12].setActionCommand(MNEMONIC_ENTER);
            buttons[13].setText(LangTool.getString("KP_PGUP","PgUp"));
            buttons[13].setActionCommand(MNEMONIC_PAGE_UP);
            buttons[14].setText(LangTool.getString("KP_HELP","Help"));
            buttons[14].setActionCommand(MNEMONIC_HELP);

            buttons[15].setText(LangTool.getString("KP_F13","PF13"));
            buttons[15].setActionCommand(MNEMONIC_PF13);
            buttons[16].setText(LangTool.getString("KP_F14","PF14"));
            buttons[16].setActionCommand(MNEMONIC_PF14);
            buttons[17].setText(LangTool.getString("KP_F15","PF15"));
            buttons[17].setActionCommand(MNEMONIC_PF15);
            buttons[18].setText(LangTool.getString("KP_F16","PF16"));
            buttons[18].setActionCommand(MNEMONIC_PF16);
            buttons[19].setText(LangTool.getString("KP_F17","PF17"));
            buttons[19].setActionCommand(MNEMONIC_PF17);
            buttons[20].setText(LangTool.getString("KP_F18","PF18"));
            buttons[20].setActionCommand(MNEMONIC_PF18);
            buttons[21].setText(LangTool.getString("KP_F19","PF19"));
            buttons[21].setActionCommand(MNEMONIC_PF19);
            buttons[22].setText(LangTool.getString("KP_F20","PF20"));
            buttons[22].setActionCommand(MNEMONIC_PF20);
            buttons[23].setText(LangTool.getString("KP_F21","PF21"));
            buttons[23].setActionCommand(MNEMONIC_PF21);
            buttons[24].setText(LangTool.getString("KP_F22","PF22"));
            buttons[24].setActionCommand(MNEMONIC_PF22);
            buttons[25].setText(LangTool.getString("KP_F23","PF23"));
            buttons[25].setActionCommand(MNEMONIC_PF23);
            buttons[26].setText(LangTool.getString("KP_F24","PF24"));
            buttons[26].setActionCommand(MNEMONIC_PF24);
            buttons[27].setText(LangTool.getString("KP_SR","SysReq"));
            buttons[27].setActionCommand(MNEMONIC_SYSREQ);
            buttons[28].setText(LangTool.getString("KP_PGDN","PgDn"));
            buttons[28].setActionCommand(TN5250jConstants.MNEMONIC_PAGE_DOWN);

            break;
      }

      buttons[29].setText(LangTool.getString("KP_NXTPAD","Next Pad"));
      buttons[29].setActionCommand("NXTPAD");

   }

   private void setButtonTop() {
      for (int x = 0; x < bSize / 2; x++) {

         keyPadTop.add(buttons[x]);

      }
   }

   private void setButtonBottom() {
      for (int x = bSize / 2; x < bSize; x++) {

         keyPadBottom.add(buttons[x]);

      }

   }

   /* These rectangles/insets are allocated once for all
     * calls.  Re-using rectangles rather than allocating them in each paint
     * call substantially reduced the time it took paint to run.
     */
   private static Rectangle viewRect = new Rectangle();
   private static Rectangle textRect = new Rectangle();
   private static Rectangle iconRect = new Rectangle();
   private static String endsWithSuffix = "...";
   private static int minSize = 3;

   /**
    */
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      Font k = buttons[0].getFont();
      float fs = 12; // start size
      k = k.deriveFont(fs);

      FontMetrics fm = buttons[0].getFontMetrics(k);

      // initializ the viewRect which is the visible rectangle
      viewRect = buttons[0].getVisibleRect();

      Insets i = buttons[0].getInsets();

      // we now subtract the insets which include the border insets as well
      viewRect.x = i.left;
      viewRect.y = i.top;
      viewRect.width = buttons[0].getWidth() - (i.right + viewRect.x);
      viewRect.height = buttons[0].getHeight() - (i.bottom + viewRect.y);

      // initialize the textRect and iconRect to 0 so they will be calculated
      textRect.x = textRect.y = textRect.width = textRect.height = 0;
      iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;

      // now compute the text that will be displayed until we run do not get
      //    elipses or we go passes the minimum of our text size that we want
      while (SwingUtilities.layoutCompoundLabel(fm,buttons[bSize-1].getText(),null,
                           buttons[0].getVerticalAlignment(),
                           buttons[0].getHorizontalAlignment(),
                           buttons[0].getVerticalTextPosition(),
                           buttons[0].getHorizontalTextPosition(),
                           viewRect,
                           textRect,
                           iconRect,
                           0).endsWith(endsWithSuffix)
                   && fs > (minSize - 1)) {
         k = k.deriveFont(--fs);
         fm = buttons[0].getFontMetrics(k);

      }

      if (fs >= minSize) {

         for (int x = 0; x < bSize; x++ ) {
            buttons[x].setFont(k);
         }
      }
   }

}
