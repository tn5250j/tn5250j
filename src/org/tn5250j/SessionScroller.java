/**
 * Title: tn5250J
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
package org.tn5250j;

import java.lang.reflect.*;

import org.tn5250j.interfaces.SessionScrollerInterface;
import org.tn5250j.tools.system.OperatingSystem;

/**
 * Session Scroller to allow the use of the mouse wheel to move the list on the
 * screen up and down.
 */
public class SessionScroller implements SessionScrollerInterface, TN5250jConstants {

   Screen5250 screen;
   Session session;
   static boolean useJava14;

   SessionScrollerInterface _instance;

   /**
    * String value for the jdk 1.4 version of Scroller
    */
   private static final String      SCROLLER_NAME14 = "org.tn5250j.SessionScroller14";

   static {

      useJava14 = OperatingSystem.hasJava14() && !OperatingSystem.isMacOS();

   }

	public SessionScroller() {

	}


   protected SessionScrollerInterface getScrollerInstance(Session ses) {

      if (_instance != null)
         return _instance;

      if (!useJava14) {
         _instance = this;
         return _instance;
      }

      screen = ses.getScreen();
      session = ses;

      Class       scroller_class;

      Constructor constructor1;

      try {

         ClassLoader loader = SessionScroller.class.getClassLoader();

         if (loader == null)
           loader = ClassLoader.getSystemClassLoader();

         scroller_class       = loader.loadClass(SCROLLER_NAME14);

         constructor1 = scroller_class.getConstructor(new Class[] {Session.class});

         try {
            Object obj= constructor1.newInstance(new Object[] {session});
            _instance = (SessionScrollerInterface)obj;

         }
         catch (Throwable crap) {
            _instance = this;
         }

//
//         constructor2 = stroker_class.getConstructor(new Class[] {KeyEvent.class,
//                                                                  boolean.class});
//
//         constructor3 = stroker_class.getConstructor(new Class[] {int.class,
//                                                                  boolean.class,
//                                                                  boolean.class,
//                                                                  boolean.class,
//                                                                  boolean.class,
//                                                                  int.class});
//
      }
      catch (Throwable t) {
         _instance = this;
         scroller_class = null;
         constructor1  = null;
      }

      _instance.addMouseWheelListener(session);
      return _instance;
//      NEW_SCROLLER = constructor1;
//      NEW_STROKER2 = constructor2;
//      NEW_STROKER3 = constructor3;
   }

   public void addMouseWheelListener(Session ses) {


   }

   public void removeMouseWheelListener(Session ses) {


   }

}
