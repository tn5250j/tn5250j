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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;

public class Gui5250Frame extends JFrame {

   BorderLayout borderLayout1 = new BorderLayout();
   My5250 me;
   JTabbedPane sessionPane = new JTabbedPane();
   private SessionManager manager;

   //Construct the frame
   public Gui5250Frame(My5250 m) {
      me = m;
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      try  {
         jbInit();
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }

   //Component initialization
  private void jbInit() throws Exception  {
      this.getContentPane().setLayout(borderLayout1);

      this.setTitle("tn5250j - 0.5.2");

      sessionPane.setBorder(BorderFactory.createEtchedBorder());
      sessionPane.setBounds(new Rectangle(78, 57, 5, 5));
      sessionPane.setOpaque(false);
      sessionPane.setRequestFocusEnabled(false);
      sessionPane.setDoubleBuffered(true);
      this.getContentPane().add(sessionPane, BorderLayout.CENTER);
   }


   //Overridden so we can exit on System Close
   protected void processWindowEvent(WindowEvent e) {
      super.processWindowEvent(e);
      if(e.getID() == WindowEvent.WINDOW_CLOSING) {
         me.closingDown(this);
      }
   }


   public void update(Graphics g) {
      paint(g);
   }
}
