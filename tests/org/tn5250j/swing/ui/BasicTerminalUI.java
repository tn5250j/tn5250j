package org.tn5250j.swing.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.InputMapUIResource;
import javax.swing.plaf.UIResource;

import org.tn5250j.Session5250;
import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.event.SessionListener;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.swing.JTerminal;

/**
 * For testing purpose
 */
public class BasicTerminalUI {
   boolean graphicsDebugMode = false;

  public static void paintSubComponent(Graphics g, BasicSubUI component)
  {
    Rectangle  tr = new Rectangle();
    component.getBounds(tr);
    if (g.hitClip(tr.x, tr.y, tr.width, tr.height))
    {
      Graphics tg = g.create(tr.x, tr.y, tr.width, tr.height);
      try
      {
        component.paint(tg);
      }
      finally
      {
        tg.dispose();
      }
    }
  }

  public static ComponentUI createUI(JComponent c)
  {
    return new ComponentUI() {};
  }

  public BasicTerminalUI()
  {
    super();
  }

  public void paint(Graphics g, JComponent c)
  {
    if (session.isConnected())
      paintSubComponent(g, screen);

    paintSubComponent(g, oia);
  }

  //============================================================================
  //                     I n s t a l l   U I   b i t s
  //============================================================================
  public void installUI(JComponent c)
  {
    if (c instanceof JTerminal)
    {
      this.terminal = (JTerminal)c;
      if (graphicsDebugMode)
      {
        javax.swing.RepaintManager repaintManager = javax.swing.RepaintManager.currentManager(terminal);
        repaintManager.setDoubleBufferingEnabled(false);
        terminal.setDebugGraphicsOptions(javax.swing.DebugGraphics.FLASH_OPTION);
      }

      this.session  = terminal.getSession();
//      session.setRunningHeadless(true);
      installComponents();
      installListeners();
      installDefaults();
      installKeyboardActions();
    }
    else
      throw new Error("TerminalUI needs JTerminal");
  }

  public void uninstallUI(JComponent c)
  {
    uninstallKeyboardActions();
    uninstallListeners();
    uninstallDefaults();
    uninstallComponents();
  }



  protected void installComponents()
  {
    Screen5250 screen = this.session.getScreen();

    this.screen = new BasicScreen(screen);
    this.screen.setRepainter(this.repainter);
    this.screen.install();

    this.oia    = new BasicOIA(screen.getOIA());
    this.oia.setRepainter(this.repainter);
    this.oia.install();

    this.terminal.setLayout(new TerminalLayoutManager());
  }

  protected void installDefaults()
  {
    // common case is background painted... this can
    // easily be changed by subclasses or from outside
    // of the component.
    this.terminal.setOpaque(true);
    this.terminal.setFocusable(true);
    this.terminal.setFocusTraversalKeysEnabled(false);

    Color bg = terminal.getBackground();
    if ( (bg == null) || (bg instanceof UIResource) )
      terminal.setBackground(DFT_BACKGROUND);

    Color fg = terminal.getForeground();
    if ( (fg == null) || (fg instanceof UIResource) )
      terminal.setForeground(DFT_FOREGROUND);

    Font f = terminal.getFont();
    if ( (f == null) || (f instanceof UIResource) )
      terminal.setFont(DFT_FONT);

    String sizePolicy = (String)terminal.getClientProperty("size-policy");
    if (sizePolicy == null)
      terminal.putClientProperty("size-policy", "fixed");
  }

  protected void installListeners()
  {
     terminal.addPropertyChangeListener(this.propListener);
     terminal.addFocusListener(this.focusListener);
//     terminal.addMouseListener(this.mouselistener);
     //terminal.addMouseMotionListener(defaultDragRecognizer);

     session.addSessionListener(this.sessListener);
  }

  protected void installKeyboardActions()
  {
//    InputMap km = getInputMap();
//    if (km != null) {
//      SwingUtilities.replaceUIInputMap(terminal, JComponent.WHEN_FOCUSED, km);
//    }
//
//    ActionMap map = getActionMap();
//    if (map != null) {
//      SwingUtilities.replaceUIActionMap(terminal, map);
//    }
//      keyHandler = KeyboardHandler.getKeyboardHandlerInstance(session);

  }

  protected void uninstallComponents()
  {
    this.terminal.setLayout(null);

    this.screen.setRepainter(null);
    this.screen.uninstall();
    this.oia.setRepainter(null);
    this.oia.uninstall();
  }

  protected void uninstallDefaults()
  {
    if (terminal.getBackground() instanceof UIResource)
      terminal.setBackground(null);

    if (terminal.getForeground() instanceof UIResource)
      terminal.setForeground(null);

    if (terminal.getFont() instanceof UIResource)
      terminal.setFont(null);
  }

  protected void uninstallListeners()
  {
     terminal.removePropertyChangeListener(this.propListener);
     terminal.removeFocusListener(this.focusListener);
//     terminal.removeMouseListener(this.mouselistener);
     //terminal.removeMouseMotionListener(defaultDragRecognizer);

     session.removeSessionListener(this.sessListener);
  }

  protected void uninstallKeyboardActions()
  {
    SwingUtilities.replaceUIInputMap(terminal, JComponent.WHEN_FOCUSED, null);
    SwingUtilities.replaceUIActionMap(terminal, null);
  }

  protected InputMap getInputMap()
  {
    InputMap map = new InputMapUIResource();

    return map;
  }


  protected ActionMap getActionMap()
  {
    ActionMap componentMap = new ActionMapUIResource();
//
//    if (KEY_POL_AID.equals(terminal.getClientProperty("key-policy")))
//    {
//      componentMap.put(OS_OHIO_MNEMONIC_CLEAR, new SendAidAction(AID_CLEAR));
//      componentMap.put(OS_OHIO_MNEMONIC_ENTER, new SendAidAction(AID_ENTER));
//      componentMap.put(OS_OHIO_MNEMONIC_HELP, new SendAidAction(AID_HELP));
//
//      componentMap.put(OS_OHIO_MNEMONIC_PF1, new SendAidAction(AID_PF1));
//      componentMap.put(OS_OHIO_MNEMONIC_PF2, new SendAidAction(AID_PF2));
//      componentMap.put(OS_OHIO_MNEMONIC_PF3, new SendAidAction(AID_PF3));
//      componentMap.put(OS_OHIO_MNEMONIC_PF4, new SendAidAction(AID_PF4));
//      componentMap.put(OS_OHIO_MNEMONIC_PF5, new SendAidAction(AID_PF5));
//      componentMap.put(OS_OHIO_MNEMONIC_PF6, new SendAidAction(AID_PF6));
//      componentMap.put(OS_OHIO_MNEMONIC_PF7, new SendAidAction(AID_PF7));
//      componentMap.put(OS_OHIO_MNEMONIC_PF8, new SendAidAction(AID_PF8));
//      componentMap.put(OS_OHIO_MNEMONIC_PF9, new SendAidAction(AID_PF9));
//      componentMap.put(OS_OHIO_MNEMONIC_PF10, new SendAidAction(AID_PF10));
//      componentMap.put(OS_OHIO_MNEMONIC_PF11, new SendAidAction(AID_PF11));
//      componentMap.put(OS_OHIO_MNEMONIC_PF12, new SendAidAction(AID_PF12));
//      componentMap.put(OS_OHIO_MNEMONIC_PF13, new SendAidAction(AID_PF13));
//      componentMap.put(OS_OHIO_MNEMONIC_PF14, new SendAidAction(AID_PF14));
//      componentMap.put(OS_OHIO_MNEMONIC_PF15, new SendAidAction(AID_PF15));
//      componentMap.put(OS_OHIO_MNEMONIC_PF16, new SendAidAction(AID_PF16));
//      componentMap.put(OS_OHIO_MNEMONIC_PF17, new SendAidAction(AID_PF17));
//      componentMap.put(OS_OHIO_MNEMONIC_PF18, new SendAidAction(AID_PF18));
//      componentMap.put(OS_OHIO_MNEMONIC_PF19, new SendAidAction(AID_PF19));
//      componentMap.put(OS_OHIO_MNEMONIC_PF20, new SendAidAction(AID_PF20));
//      componentMap.put(OS_OHIO_MNEMONIC_PF21, new SendAidAction(AID_PF21));
//      componentMap.put(OS_OHIO_MNEMONIC_PF22, new SendAidAction(AID_PF22));
//      componentMap.put(OS_OHIO_MNEMONIC_PF23, new SendAidAction(AID_PF23));
//      componentMap.put(OS_OHIO_MNEMONIC_PF24, new SendAidAction(AID_PF24));
//
//      componentMap.put(OS_OHIO_MNEMONIC_PRINT, new SendAidAction(AID_PRINT));
//      componentMap.put(OS_OHIO_MNEMONIC_DOWN, new SendAidAction(AID_ROLL_DOWN));
//      componentMap.put(OS_OHIO_MNEMONIC_LEFT, new SendAidAction(AID_ROLL_LEFT));
//      componentMap.put(OS_OHIO_MNEMONIC_RIGHT, new SendAidAction(AID_ROLL_RIGHT));
//      componentMap.put(OS_OHIO_MNEMONIC_UP, new SendAidAction(AID_ROLL_UP));
//    }
//    else
//    {
//      componentMap.put(OS_OHIO_MNEMONIC_CLEAR, new SendStringAction(OS_OHIO_MNEMONIC_CLEAR));
//      componentMap.put(OS_OHIO_MNEMONIC_ENTER, new SendStringAction(OS_OHIO_MNEMONIC_ENTER));
//      componentMap.put(OS_OHIO_MNEMONIC_HELP, new SendStringAction(OS_OHIO_MNEMONIC_HELP));
//
//      componentMap.put(OS_OHIO_MNEMONIC_PF1, new SendStringAction(OS_OHIO_MNEMONIC_PF1));
//      componentMap.put(OS_OHIO_MNEMONIC_PF2, new SendStringAction(OS_OHIO_MNEMONIC_PF2));
//      componentMap.put(OS_OHIO_MNEMONIC_PF3, new SendStringAction(OS_OHIO_MNEMONIC_PF3));
//      componentMap.put(OS_OHIO_MNEMONIC_PF4, new SendStringAction(OS_OHIO_MNEMONIC_PF4));
//      componentMap.put(OS_OHIO_MNEMONIC_PF5, new SendStringAction(OS_OHIO_MNEMONIC_PF5));
//      componentMap.put(OS_OHIO_MNEMONIC_PF6, new SendStringAction(OS_OHIO_MNEMONIC_PF6));
//      componentMap.put(OS_OHIO_MNEMONIC_PF7, new SendStringAction(OS_OHIO_MNEMONIC_PF7));
//      componentMap.put(OS_OHIO_MNEMONIC_PF8, new SendStringAction(OS_OHIO_MNEMONIC_PF8));
//      componentMap.put(OS_OHIO_MNEMONIC_PF9, new SendStringAction(OS_OHIO_MNEMONIC_PF9));
//      componentMap.put(OS_OHIO_MNEMONIC_PF10, new SendStringAction(OS_OHIO_MNEMONIC_PF10));
//      componentMap.put(OS_OHIO_MNEMONIC_PF11, new SendStringAction(OS_OHIO_MNEMONIC_PF11));
//      componentMap.put(OS_OHIO_MNEMONIC_PF12, new SendStringAction(OS_OHIO_MNEMONIC_PF12));
//      componentMap.put(OS_OHIO_MNEMONIC_PF13, new SendStringAction(OS_OHIO_MNEMONIC_PF13));
//      componentMap.put(OS_OHIO_MNEMONIC_PF14, new SendStringAction(OS_OHIO_MNEMONIC_PF14));
//      componentMap.put(OS_OHIO_MNEMONIC_PF15, new SendStringAction(OS_OHIO_MNEMONIC_PF15));
//      componentMap.put(OS_OHIO_MNEMONIC_PF16, new SendStringAction(OS_OHIO_MNEMONIC_PF16));
//      componentMap.put(OS_OHIO_MNEMONIC_PF17, new SendStringAction(OS_OHIO_MNEMONIC_PF17));
//      componentMap.put(OS_OHIO_MNEMONIC_PF18, new SendStringAction(OS_OHIO_MNEMONIC_PF18));
//      componentMap.put(OS_OHIO_MNEMONIC_PF19, new SendStringAction(OS_OHIO_MNEMONIC_PF19));
//      componentMap.put(OS_OHIO_MNEMONIC_PF20, new SendStringAction(OS_OHIO_MNEMONIC_PF20));
//      componentMap.put(OS_OHIO_MNEMONIC_PF21, new SendStringAction(OS_OHIO_MNEMONIC_PF21));
//      componentMap.put(OS_OHIO_MNEMONIC_PF22, new SendStringAction(OS_OHIO_MNEMONIC_PF22));
//      componentMap.put(OS_OHIO_MNEMONIC_PF23, new SendStringAction(OS_OHIO_MNEMONIC_PF23));
//      componentMap.put(OS_OHIO_MNEMONIC_PF24, new SendStringAction(OS_OHIO_MNEMONIC_PF24));
//
//      componentMap.put(OS_OHIO_MNEMONIC_PRINT, new SendStringAction(OS_OHIO_MNEMONIC_PRINT));
//      componentMap.put(OS_OHIO_MNEMONIC_DOWN, new SendStringAction(OS_OHIO_MNEMONIC_DOWN));
//      componentMap.put(OS_OHIO_MNEMONIC_LEFT, new SendStringAction(OS_OHIO_MNEMONIC_LEFT));
//      componentMap.put(OS_OHIO_MNEMONIC_RIGHT, new SendStringAction(OS_OHIO_MNEMONIC_RIGHT));
//      componentMap.put(OS_OHIO_MNEMONIC_UP, new SendStringAction(OS_OHIO_MNEMONIC_UP));
//    }
//
    return componentMap;

  }

  //============================================================================
  //                      P r i v a t e   M e t h o d s
  //============================================================================
  private void initFontMap(Font f)
  {
    if (f == null)
    {
      this.widthMap = null;
      this.heightMap = null;
      return;
    }

    this.widthMap  = new int[MAX_POINT*2];
    this.heightMap = new int[MAX_POINT*2];

    this.fontName  = f.getName();
    this.fontStyle = f.getStyle();

    for (int i = 4, j = 0, tw = 0, th = 0; i < MAX_POINT; i++)
    {
      //Font        workFont = f.deriveFont((float)i);
      Font        workFont = new Font(this.fontName, this.fontStyle, i);
      FontMetrics metrics  = terminal.getFontMetrics(workFont);

      int         w        = metrics.charWidth('W');
      int         h        = metrics.getHeight();
      //int         h        = metrics.getAscent() + metrics.getDescent();

      this.widthMap[j] = w;
      this.widthMap[j+1] = i;
      this.heightMap[j] = h;
      this.heightMap[j+1] = i;

      if ( (tw == w) && (th == h) )
        break;

      tw = w;
      th = h;
      j += 2;
    }
  }

/* *** NEVER USED LOCALLY ************************************************ */
//  private int deriveFontSize(int width, int height)
//  {
//    int index = this.deriveScaleIndex(width, height);
//
//    return this.widthMap[index + 1];
//  }

  private int deriveScaleIndex(int width, int height)
  {
    int w = width/screen.columns;
    int h = height/screen.rows;

    int i;
    for (i = (widthMap.length - 2); (i > 0) && (this.widthMap[i] == 0); i -= 2);

    for(; (i != 0) && (widthMap[i] > w); i -= 2);
    for(; (i != 0) && (heightMap[i] > h); i -= 2);

    return i;
  }

  //============================================================================
  //                             L a y o u t
  //============================================================================
  public class TerminalLayoutManager implements LayoutManager
  {
    public void addLayoutComponent(String name, Component comp) {}
    public void removeLayoutComponent(Component comp)           {}

    public Dimension preferredLayoutSize(Container parent)
    {
      Dimension oiaD = oia.getPreferredSize();
      Dimension scrD = screen.getPreferredSize();

      return new Dimension(Math.max(oiaD.width, scrD.width), oiaD.height + scrD.height);
    }

    public Dimension minimumLayoutSize(Container parent)
    {
      return oia.getPreferredSize();
    }

    public Dimension maximumLayoutSize(Container parent)
    {
      return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
    }

    public void layoutContainer(Container parent)
    {
      if (parent != terminal)
         return;

      JTerminal target = (JTerminal) parent;
      Rectangle bounds = target.getBounds();
      Insets    insets = target.getInsets();
      int       top    = insets.top;
      int       bottom = bounds.height - insets.bottom;
      int       left   = insets.left;
      int       right  = bounds.width - insets.right;

//      boolean   ltr    = target.getComponentOrientation().isLeftToRight();
      Dimension d      = null;

      if (oia != null)
      {
        d = oia.getPreferredSize();
        oia.setBounds(left, bottom - d.height, right - left, d.height);
        bottom -= d.height;
      }

      if (screen != null)
      {
        int  width  = right - left;
        int  height = bottom - top;
        adjustScreen(left, top, width, height);
      }
    }
  }

  private void adjustScreen(int x, int y, int width, int height)
  {
    int  index  = deriveScaleIndex(width, height);
    Font font   = new Font(fontName, fontStyle, widthMap[index+1]);

    if (sizePolicy == SIZE_POL_FIXED)
    {
      int  cW     = widthMap[index];
      int  cH     = heightMap[index];

      screen.setFont(font, cW, cH);

      cW = cW * screen.columns;
      cH = cH * screen.rows;

      width = width - cW;
      height = height - cH;
      x += width / 2;
      y += height / 2;

      screen.setBounds(x, y, cW, cH);
    }
    else
    {
      int  cW     = width / screen.columns;
      int  cH     = height / screen.rows;

      screen.setFont(font, cW, cH);
      screen.setBounds(x, y, width, height);
    }
  }


  //============================================================================
  //                             L i s t e n e r s
  //============================================================================
  public class PropertyChangeHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent evt)
    {
      String name = evt.getPropertyName();
      if ("font".equals(name))
      {
        Font font = (Font)evt.getNewValue();
        initFontMap(font);

        int index  = (font.getSize() - 4)*2;
        screen.setFont(font, widthMap[index], heightMap[index]);
        oia.setFont(font, widthMap[index], heightMap[index]);
      }
      else if ("size-policy".equals(name))
      {
        String val = (String)evt.getNewValue();
        if (SIZE_POL_FIXED.equals(val))
          sizePolicy = SIZE_POL_FIXED;
        else
          sizePolicy = SIZE_POL_DYNAMIC;

        terminal.invalidate();
        terminal.repaint();
      }
    }
  }

  public class FocusHandler implements FocusListener
  {
    public void focusGained(FocusEvent e)
    {
//      System.out.println("Focus gained");
      screen.setCursorEnabled(true);
    }

    public void focusLost(FocusEvent e)
    {
//      System.out.println("Focus gained");
      screen.setCursorEnabled(false);
    }
  }

//     public class MouseHandler implements MouseListener
//     {
//       Rectangle bounds = new Rectangle();
//
//       public void mouseClicked(MouseEvent e)
//       {
//         if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK)
//         {
//           int       x = e.getX();
//           int       y = e.getY();
//   //        System.out.println("Mouse clicked "+x+","+y);
//
//           screen.getBounds(bounds);
//
//           if (bounds.contains(x, y))
//   //          screen.setCursor(364, 85);
//             screen.setCursor(x - bounds.x, y - bounds.y);
//         }
//       }
//
//       public void mousePressed(MouseEvent e) {};
//       public void mouseReleased(MouseEvent e) {};
//       public void mouseEntered(MouseEvent e) {};
//       public void mouseExited(MouseEvent e) {};
//     }

  public class RepaintHandler implements BasicSubUI.Repainter
  {
    public void addDirtyRectangle(BasicSubUI origin, int x, int y, int width, int height)
    {
      RepaintManager.currentManager(terminal).addDirtyRegion(terminal, x, y, width, height);
//      terminal.repaint();
    }
  }

  public class SessionHandler implements SessionListener
  {
   public void onSessionChanged(SessionChangeEvent event)
    {
      //terminal.invalidate();
      terminal.repaint();
    }
  }

//     //============================================================================
//     //                               A c t i o n s
//     //============================================================================
//     public class SendAidAction extends AbstractAction
//     {
//       public SendAidAction(int aidKey)
//       {
//         this.aidKey = aidKey;
//       }
//
//       public void actionPerformed(ActionEvent e)
//       {
//         session.getScreen().sendAid(this.aidKey);
//       }
//
//       public boolean isEnabled()
//       {
//         return session != null;
//       }
//
//       private int aidKey;
//     }
//
//     public class SendStringAction extends AbstractAction
//     {
//       public SendStringAction(String stringKey)
//       {
//         this.stringKey = stringKey;
//       }
//
//       public void actionPerformed(ActionEvent e)
//       {
//         session.getScreen().sendKeys(this.stringKey, null);
//       }
//
//       public boolean isEnabled()
//       {
//         return session != null;
//       }
//
//       private String stringKey;
//     }

  //============================================================================
  //                             V a r i a b l e s
  //============================================================================
  transient JTerminal              terminal;
  transient Session5250            session;

  transient BasicScreen            screen;
  transient BasicOIA               oia;

  transient String                 fontName;
  transient int                    fontStyle;
  transient int[]                  widthMap;
  transient int[]                  heightMap;

  transient String                 sizePolicy;

  public static final int MAX_POINT = 36;

  //============================================================================
  //                             L i s t e n e r s
  //============================================================================
  transient PropertyChangeListener propListener = new PropertyChangeHandler();
  transient FocusListener          focusListener= new FocusHandler();
  transient SessionListener   sessListener = new SessionHandler();
  transient RepaintHandler         repainter    = new RepaintHandler();
//  transient MouseHandler           mouselistener= new MouseHandler();

  //============================================================================
  //                              C o n s a n t s
  //============================================================================
  private static final String          SIZE_POL_FIXED   = "fixed";
  private static final String          SIZE_POL_DYNAMIC = "dynamic";

//  private static final String          KEY_POL_STRING   = "string";
//  private static final String          KEY_POL_AID      = "aid";

  public  static final ColorUIResource DFT_BACKGROUND   = new ColorUIResource(Color.black);
  public  static final ColorUIResource DFT_FOREGROUND   = new ColorUIResource(Color.green);
  public  static final Font            DFT_FONT         = new FontUIResource("Monospaced", Font.BOLD, 12);
}
