/*
 * Title: tn5250J
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
package org.tn5250j.connectdialog;

import org.tn5250j.tools.FixedCenterLayout;
import org.tn5250j.tools.LangTool;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Vector;

/**
 * A multiselection list component.  It is a proxy class wrapping a JList
 * component that controls two internal JLists for the selection of what is
 * to be included and not included on the two sides of a list.
 */
class MultiSelectListComponent extends JComponent {

  private static final long serialVersionUID = 1L;

  // Button types
  private static final String SELECT_ITEM = ">";
  private static final String SELECT_ALL = ">>|";
  private static final String DESELECT_ITEM = "<";
  private static final String DESELECT_ALL = "|<<";
  private static final Dimension BUTTON_SIZE = new Dimension(48, 32);

  // Internal components
  private JList mainList = null;
  private JList sourceList = null;
  private JList selectionList = null;
  private JScrollPane sourcePane = null;
  private JScrollPane selectionPane = null;
  private SelectionButton selectItemButton = null;
  private SelectionButton selectAllButton = null;
  private SelectionButton deselectItemButton = null;
  private SelectionButton deselectAllButton = null;
  private JPanel buttonPanel = null;
  private EventHandler eventHandler = new EventHandler();
  private Dimension defaultListSize = new Dimension(100, 200);
  private FontMetrics lastFontMetrics = null;
  private JPanel sourcePanel;
  private JPanel selectionPanel;

  /**
   * Constructs a <code>MultiSelectListComponent</code> that displays the elements in the specified non-null model.
   * All <code>MultiSelectListComponent</code> constructors delegate to this one.
   */
  public MultiSelectListComponent(ListModel dataModel) {
    this();
    mainList.setModel(dataModel);
    init();
  }

  /**
   * Constructs a <code>MultiSelectListComponent</code> that displays the elements in the specified array.
   * This constructor just delegates to the <code>ListModel</code> constructor.
   */
  public MultiSelectListComponent(Object[] listData) {
    this();
    mainList.setListData(listData);
    init();
  }

  /**
   * Constructs a <code>MultiSelectListComponent</code> that displays the elements in the specified Vector.
   * This constructor just delegates to the ListModel constructor.
   */
  public MultiSelectListComponent(Vector listData) {
    this();
    mainList.setListData(listData);
    init();
  }

  /**
   * Creates a new <code>MultiSelectListComponent</code> component with an empty model.
   */
  public MultiSelectListComponent() {
    this(4);
  }

  /**
   * Creates a new <code>MultiSelectListComponent</code> component with an empty model and
   * the specified horizontal gap between components.
   */
  public MultiSelectListComponent(int hgap) {
    super();

    mainList = new JList();
    mainList.addListSelectionListener(eventHandler);

    sourceList = new JList();
    sourceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    sourceList.addListSelectionListener(eventHandler);
    sourceList.addMouseListener(
        new MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
              selectItem();
            }
          }
        }
    );

    selectionList = new JList();
    selectionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    selectionList.addListSelectionListener(eventHandler);
    selectionList.addMouseListener(
        new MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
              deselectItem();
            }
          }
        }
    );

    buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout());
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(11, 0, 0, 0));
    selectItemButton = initButton(new SelectionButton(SELECT_ITEM),
        LangTool.getString("oaa.AddSelected"));
    selectAllButton = initButton(new SelectionButton(SELECT_ALL),
        LangTool.getString("oaa.AddAll"));
    deselectItemButton = initButton(new SelectionButton(DESELECT_ITEM),
        LangTool.getString("oaa.RemoveSelected"));
    deselectAllButton = initButton(new SelectionButton(DESELECT_ALL),
        LangTool.getString("oaa.RemoveAll"));

    Dimension dimButton = BUTTON_SIZE;
    buttonPanel.setPreferredSize(new Dimension(dimButton.width, 4 * dimButton.height));

    // setup our layout to center the source buttons and selections
    setLayout(new FixedCenterLayout(hgap));

    // create our source panel
    sourcePanel = new JPanel();
    sourcePanel.setLayout(new BorderLayout());
    sourcePane = new JScrollPane(sourceList);
    sourcePane.setPreferredSize(defaultListSize);
    sourcePanel.add(sourcePane, BorderLayout.CENTER);


    // create our selection panel
    selectionPanel = new JPanel();
    selectionPanel.setLayout(new BorderLayout());
    selectionPane = new JScrollPane(selectionList);
    selectionPane.setPreferredSize(defaultListSize);
    selectionPanel.add(selectionPane, BorderLayout.CENTER);

    add(sourcePanel, BorderLayout.WEST);
    add(selectionPanel, BorderLayout.EAST);
    add(buttonPanel, BorderLayout.CENTER);

    setUI(new MultiSelectListUI());
  }

  /**
   * Updates the look and feel.
   */
  public void updateUI() {
    setUI(new MultiSelectListUI());
  }


  /**
   * Returns the prototypical cell value.
   */
  public Object getPrototypeCellValue() {
    return mainList.getPrototypeCellValue();
  }

  /**
   * Sets the prototypical cell value.
   */
  public void setPrototypeCellValue(Object prototypeCellValue) {
    mainList.setPrototypeCellValue(prototypeCellValue);
    sourceList.setPrototypeCellValue(prototypeCellValue);
    selectionList.setPrototypeCellValue(prototypeCellValue);
  }

  /**
   * Returns the fixed cell width.
   */
  public int getFixedCellWidth() {
    return mainList.getFixedCellWidth();
  }

  /**
   * Sets the fixed cell width.
   */
  public void setFixedCellWidth(int width) {
    mainList.setFixedCellWidth(width);
    sourceList.setFixedCellWidth(width);
    selectionList.setFixedCellWidth(width);
  }

  /**
   * Returns the fixed cell height.
   */
  public int getFixedCellHeight() {
    return mainList.getFixedCellHeight();
  }

  /**
   * Sets the fixed cell height.
   */
  public void setFixedCellHeight(int height) {
    mainList.setFixedCellHeight(height);
    sourceList.setFixedCellHeight(height);
    selectionList.setFixedCellHeight(height);
  }

  /**
   * Returns the renderer that's used to paint each cell.
   */
  public ListCellRenderer getCellRenderer() {
    return mainList.getCellRenderer();
  }

  /**
   * Sets the renderer that's used to paint each cell.
   */
  public void setCellRenderer(ListCellRenderer cellRenderer) {
    mainList.setCellRenderer(cellRenderer);
    sourceList.setCellRenderer(cellRenderer);
    selectionList.setCellRenderer(cellRenderer);
  }

  /**
   * Returns the foreground color for selected cells.
   */
  public Color getSelectionForeground() {
    return mainList.getSelectionForeground();
  }

  /**
   * Sets the foreground color for selected cells.
   */
  public void setSelectionForeground(Color selectionForeground) {
    mainList.setSelectionForeground(selectionForeground);
    sourceList.setSelectionForeground(selectionForeground);
    selectionList.setSelectionForeground(selectionForeground);
  }

  /**
   * Returns the background color for selected cells.
   */
  public Color getSelectionBackground() {
    return mainList.getSelectionBackground();
  }

  /**
   * Sets the background color for selected cells.
   */
  public void setSelectionBackground(Color selectionBackground) {
    mainList.setSelectionBackground(selectionBackground);
    sourceList.setSelectionBackground(selectionBackground);
    selectionList.setSelectionBackground(selectionBackground);
  }

  /**
   * Sets the column header of the source list
   *
   * @param header
   */
  public void setSourceColumnHeader(String header) {
    JViewport jvp = new JViewport();
    jvp.setView(new JLabel(header));
    sourcePane.setColumnHeader(jvp);

  }

  /**
   * Sets the column header of the selection list
   *
   * @param header
   */
  public void setSelectionColumnHeader(String header) {
    JViewport jvp = new JViewport();
    jvp.setView(new JLabel(header));
    selectionPane.setColumnHeader(jvp);

  }

  /**
   * Sets the header of the source list
   *
   * @param header
   */
  public void setSourceHeader(String header) {

    setSourceHeader(header, JLabel.LEFT);

  }

  /**
   * Sets the header of the source list
   *
   * @param header
   * @param horizontalAlignment
   */
  void setSourceHeader(String header, int horizontalAlignment) {
    sourcePanel.add(new JLabel(header, horizontalAlignment), BorderLayout.NORTH);

  }

  /**
   * Sets the header of the selection list
   *
   * @param header
   */
  public void setSelectionHeader(String header) {
    setSelectionHeader(header, JLabel.LEFT);
  }

  /**
   * Sets the header of the selection list
   *
   * @param header
   * @param horizontalAlignment
   */
  void setSelectionHeader(String header, int horizontalAlignment) {
    selectionPanel.add(new JLabel(header, horizontalAlignment), BorderLayout.NORTH);

  }

  //================== ListModel interface methods ==================

  /**
   * Returns the list's data model.
   */
  public ListModel getModel() {
    return mainList.getModel();
  }

  /**
   * Sets the list's data model.
   */
  public void setModel(ListModel model) {
    mainList.setModel(model);
    updateView();
  }

  /**
   * Constructs the list's data model from an array of Objects.
   */
  void setListData(final Object[] listData) {
    mainList.setListData(listData);
    updateView();
  }


  /**
   * Constructs the list's data model from a Vector.
   */
  public void setListData(final Vector listData) {
    mainList.setListData(listData);
    updateView();
  }


  //================== ListSelectionModel interface methods ==================

  /**
   * Returns the current selection model.
   */
  public ListSelectionModel getSelectionModel() {
    return mainList.getSelectionModel();
  }

  /**
   * Adds a listener to the ListSelectionListeners list.
   */
  public void addListSelectionListener(ListSelectionListener listener) {
    listenerList.add(ListSelectionListener.class, listener);
  }

  /**
   * Removes a listener from the the ListSelectionListeners list.
   */
  public void removeListSelectionListener(ListSelectionListener listener) {
    listenerList.remove(ListSelectionListener.class, listener);
  }


  /**
   * Forwards the given notification event to all registered
   * listeners.
   */
  private void fireSelectionValueChanged(int firstIndex,
                                         int lastIndex, boolean isAdjusting) {
    // Guaranteed to return a non-null array
    Object[] listeners = listenerList.getListenerList();
    ListSelectionEvent e = null;

    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ListSelectionListener.class) {
        if (e == null) {
          e = new ListSelectionEvent(this, firstIndex,
              lastIndex, isAdjusting);
        }
        ((ListSelectionListener) listeners[i + 1]).valueChanged(e);
      }
    }
  }


  /**
   * Returns the <code>selectionMode</code> property value.
   */
  public int getSelectionMode() {
    return mainList.getSelectionMode();
  }

  /**
   * Returns the first index argument from the most recent an interval selection.
   */
  public int getAnchorSelectionIndex() {
    return getSelectionModel().getAnchorSelectionIndex();
  }

  /**
   * Returns the second index argument from the most recent an interval selection.
   */
  public int getLeadSelectionIndex() {
    return mainList.getLeadSelectionIndex();
  }


  /**
   * Returns the minimal selected cell index.
   */
  public int getMinSelectionIndex() {
    return mainList.getMinSelectionIndex();
  }


  /**
   * Returns the maximal selected cell index.
   */
  public int getMaxSelectionIndex() {
    return mainList.getMaxSelectionIndex();
  }


  /**
   * Returns true if the specified index is selected.
   */
  public boolean isSelectedIndex(int index) {
    return mainList.isSelectedIndex(index);
  }


  /**
   * Returns true if nothing is selected.
   */
  public boolean isSelectionEmpty() {
    return mainList.isSelectionEmpty();
  }


  /**
   * Clears the selection.
   */
  public void clearSelection() {
    mainList.clearSelection();
    updateView();
  }


  /**
   * Selects the specified interval.
   */
  public void setSelectionInterval(int anchor, int lead) {
    mainList.setSelectionInterval(anchor, lead);
    updateView();
  }


  /**
   * Adds the specified interval to the current selection.
   */
  public void addSelectionInterval(int anchor, int lead) {
    mainList.addSelectionInterval(anchor, lead);
    updateView();
  }

  /**
   * Removes the specified interval to the current selection.
   */
  public void removeSelectionInterval(int index0, int index1) {
    mainList.removeSelectionInterval(index0, index1);
    updateView();
  }

  /**
   * Returns the first selected cell index.
   */
  public int getSelectedIndex() {
    return mainList.getSelectedIndex();
  }

  /**
   * Selects a cell by the index.
   */
  public void setSelectedIndex(int index) {
    mainList.setSelectedIndex(index);
    updateView();
  }

  /**
   * Returns the selected indices array.
   */
  public int[] getSelectedIndices() {
    return mainList.getSelectedIndices();
  }

  /**
   * Selects the specified cells.
   */
  void setSelectedIndices(int[] indices) {
    mainList.setSelectedIndices(indices);
    updateView();
  }

  /**
   * Returns an array of the selected cell values.
   */
  Object[] getSelectedValues() {
    return mainList.getSelectedValues();
  }

  /**
   * Returns the first selected cell value.
   */
  public Object getSelectedValue() {
    return mainList.getSelectedValue();
  }

  /**
   * Selects the specified object.
   */
  public void setSelectedValue(Object anObject, boolean shouldScroll) {
    mainList.setSelectedValue(anObject, shouldScroll);
    updateView();
  }

  /**
   * Returns a string representation of the object.
   */
  protected String paramString() {
    return super.paramString() + ", the list is: " + mainList;
  }

  /**
   * Initializes and adds a button to the component.
   */
  private SelectionButton initButton(SelectionButton button, String toolTipText) {
    if (button != null) {
      buttonPanel.add(button);
      button.addActionListener(eventHandler);
      button.setToolTipText(toolTipText);
    }
    return button;
  }

  /**
   * Initializes the component after it is created.
   */
  protected void init() {
    updateView();
  }

  /**
   * Initializes and adds a button to the component.
   */
  private void updateView() {
    ListModel mainModel = mainList.getModel();
    int[] selected = mainList.getSelectedIndices();

    Vector sourceVector = new Vector(mainModel.getSize() - selected.length);
    Vector selectionVector = new Vector(selected.length);
    for (int i = 0; i < mainModel.getSize(); i++) {
      if (mainList.isSelectedIndex(i)) {
        selectionVector.addElement(mainModel.getElementAt(i));
      } else {
        sourceVector.addElement(mainModel.getElementAt(i));
      }
    }
    sourceList.setListData(sourceVector);
    selectionList.setListData(selectionVector);
    updateButtons();
  }

  /*
   * Enables (or disables) the buttons.
   */
  private void updateButtons() {

    if (!sourceList.isEnabled()) {
      selectItemButton.setEnabled(false);
      selectAllButton.setEnabled(false);
      deselectItemButton.setEnabled(false);
      deselectAllButton.setEnabled(false);
    } else {
      selectItemButton.setEnabled(!sourceList.isSelectionEmpty());
      selectAllButton.setEnabled(sourceList.getModel().getSize() > 0);
      deselectItemButton.setEnabled(!selectionList.isSelectionEmpty());
      deselectAllButton.setEnabled(selectionList.getModel().getSize() > 0);
    }
  }

  /**
   * Returns the preferred size of the component.
   */
  public Dimension getPreferredSize() {
    Dimension dimSource = sourcePane.getPreferredSize();
    Dimension dimSelection = selectionPane.getPreferredSize();

    Dimension dimButtons = buttonPanel.getPreferredSize();
    int w = dimButtons.width + 2 * getListPreferredWidth();
    int h = Math.max(dimButtons.height, Math.max(dimSource.height, dimSelection.height));
    return new Dimension(w, h);
  }

  /**
   * Returns the minimum size of the component.
   */
  public Dimension getMinimumSize() {
    Dimension dimSource = sourceList.getMinimumSize();
    Dimension dimSelection = selectionList.getMinimumSize();
    Dimension dimButton = buttonPanel.getMinimumSize();
    int w = dimButton.width + dimSource.width + dimSelection.width;
    int h = Math.max(dimButton.height, Math.max(dimSource.height, dimSelection.height));
    return new Dimension(w, h);
  }

  /**
   * Returns the maximum size of the component.
   */
  public Dimension getMaximumSize() {
    Dimension dimSource = sourceList.getMaximumSize();
    Dimension dimSelection = selectionList.getMaximumSize();
    Dimension dimButton = buttonPanel.getMaximumSize();
    int w = dimButton.width + dimSource.width + dimSelection.width;
    int h = Math.max(dimButton.height, Math.max(dimSource.height, dimSelection.height));
    return new Dimension(w, h);
  }

  public void paint(Graphics g) {
    lastFontMetrics = g.getFontMetrics();
    super.paint(g);
  }

  /**
   * Returns the preferred width of the list.
   */
  private int getListPreferredWidth() {
    ListModel mainModel = mainList.getModel();
    int maxWidth = 0;
    if (lastFontMetrics != null) {
      for (int i = 0; i < mainModel.getSize(); i++) {
        Object item = mainModel.getElementAt(i);
        if (item instanceof String) {
          Math.max(lastFontMetrics.stringWidth((String) item), maxWidth);
        }
      }
    }
    return maxWidth == 0 ? defaultListSize.width : maxWidth;

  }

  /**
   * Moves the items selected in the left list to the right list.
   */
  private void selectItem() {
    int[] indices = mainList.getSelectedIndices();
    Object[] values = sourceList.getSelectedValues();

    int newIndices[] = new int[indices.length + values.length];
    System.arraycopy(indices, 0, newIndices, 0, indices.length);

    ListModel mainModel = mainList.getModel();
    for (int i = 0, last = indices.length; i < mainModel.getSize(); i++) {
      Object value = mainModel.getElementAt(i);
      for (int j = 0; j < values.length; j++) {
        if (value == values[j]) {
          newIndices[last++] = i;
        }
      }
    }

    // move the selected items
    doUpdate(sourceList, values, newIndices);
  }

  /**
   * Moves all items from the left list to the right list.
   */
  private void selectAll() {
    addSelectionInterval(0, mainList.getModel().getSize() - 1);
    updateView();
  }

  /**
   * Moves the items selected in the right list to the left list.
   */
  private void deselectItem() {
    int[] indices = mainList.getSelectedIndices();
    Object[] values = selectionList.getSelectedValues();

    int newIndices[] = new int[indices.length - values.length];

    ListModel mainModel = mainList.getModel();
    for (int i = 0, last = 0; i < indices.length; i++) {
      Object value = mainModel.getElementAt(indices[i]);
      boolean found = false;
      for (int j = 0; j < values.length; j++) {
        if (value == values[j]) {
          found = true;
          break;
        }
      }
      if (!found) {
        newIndices[last++] = indices[i];
      }
    }

    // move the selected items
    doUpdate(selectionList, values, newIndices);
  }

  /**
   * Move the selected items from one list and
   * also select the next item in the list.
   *
   * @param list       the list which is to be changed
   * @param values     the selected values
   * @param newIndices the indeces of the selected items
   */
  private void doUpdate(JList list, Object values[], int newIndices[]) {
    if (values != null && values.length > 0) {
      // Order is important. This must come before the updateView since
      // it recreates the selection/source lists.
      int nextIndex = getIndexFromItem(list, values[values.length - 1]);

      // account for the number of items being removed
      nextIndex -= (values.length - 1);

      // recreate the lists based on the selected items
      mainList.setSelectedIndices(newIndices);
      updateView();

      // ready the next item in the list for selection
      if (nextIndex < list.getModel().getSize()) {
        list.setSelectedIndex(nextIndex);
      } else {
        // select last item
        list.setSelectedIndex(list.getModel().getSize() - 1);
      }
    }
  }

  /**
   * Given an Object finds its index in the list.
   *
   * @param list the JList to be searched.
   * @param item the Object to search for.
   * @return the item's index or -1 if not found
   */
  private int getIndexFromItem(JList list, Object item) {
    ListModel lm = list.getModel();
    for (int i = 0; i < lm.getSize(); i++) {
      Object o = lm.getElementAt(i);
      if (o.equals(item)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Moves all items from the right list to the left list.
   */
  private void deselectAll() {
    mainList.clearSelection();
    updateView();
  }

  public void setEnabled(boolean enabled) {
    sourceList.setEnabled(enabled);
    selectionList.setEnabled(enabled);
    updateView();
  }

  /**
   * Internal event handler class.
   */
  private class EventHandler implements ActionListener, ListSelectionListener, Serializable {

    //================== ActionListener interface methods ==================

    private static final long serialVersionUID = 1L;

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent event) {
      if (event.getSource() instanceof SelectionButton) {
        SelectionButton btn = (SelectionButton) event.getSource();
        if (btn.equals(selectItemButton)) {
          MultiSelectListComponent.this.selectItem();
        } else if (btn.equals(selectAllButton)) {
          MultiSelectListComponent.this.selectAll();
        } else if (btn.equals(deselectItemButton)) {
          MultiSelectListComponent.this.deselectItem();
        } else if (btn.equals(deselectAllButton)) {
          MultiSelectListComponent.this.deselectAll();
        }
      }
    }

    //================== ListSelectionListener interface methods ==================

    /*
     * Called whenever the value of the selection changes.
     */
    public void valueChanged(ListSelectionEvent e) {
      if (e.getSource() == MultiSelectListComponent.this.mainList) {
        MultiSelectListComponent.this.fireSelectionValueChanged(e.getFirstIndex(),
            e.getLastIndex(), e.getValueIsAdjusting());
      }
      MultiSelectListComponent.this.updateButtons();
    }

  } // EventHandler


  /**
   * Internal selection button class.
   */
  private static class SelectionButton extends JButton {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new SelectionButton component.
     */
    SelectionButton(String text) {
      super(text);
      this.setEnabled(false);
      setMargin(new Insets(8, 8, 8, 8));
    }

    /**
     * Returns the preferred size of the component.
     */
    public Dimension getPreferredSize() {
      return BUTTON_SIZE;
    }

    /**
     * Returns the minimum size of the component.
     */
    public Dimension getMinimumSize() {
      return getPreferredSize();
    }

    /**
     * Returns the minimum size of the component.
     */
    public Dimension getMaximumSize() {
      return getPreferredSize();
    }

  } // SelectionButton


  // dummy class to handle Look and Feel and proper serialization
  private static class MultiSelectListUI extends ComponentUI {

  }

}
