/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dytech.gui;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.tle.common.gui.models.GenericListModel;

/**
 * @author Nicholas Read
 * @created 13 June 2003 (Friday the 13th!!)
 */
public class JShuffleList<T> extends JComponent implements ActionListener, ListSelectionListener {
  public static final String UP_ICON = "icons/up.gif";
  public static final String DOWN_ICON = "icons/down.gif";
  public static final String DEFAULT_TEXT = "Enter an item";

  private boolean enabled;

  protected JButton add;
  protected JButton remove;
  protected JButton up;
  protected JButton down;
  protected JLabel text;
  protected ShuffleInterface<T> entry;
  protected JList list;
  protected GenericListModel<T> model;
  protected JScrollPane listScroll;

  public static JShuffleList<String> newDefaultShuffleList(String text, boolean allowUpDown) {
    return new JShuffleList<String>(new DefaultShuffleComponent(text), text, allowUpDown);
  }

  public static JShuffleList<String> newDefaultShuffleList(boolean allowUpDown) {
    return newDefaultShuffleList(DEFAULT_TEXT, allowUpDown);
  }

  public JShuffleList(ShuffleInterface<T> si, boolean allowUpDown) {
    setup(si, DEFAULT_TEXT, allowUpDown);
  }

  public JShuffleList(ShuffleInterface<T> si, String text, boolean allowUpDown) {
    setup(si, text, allowUpDown);
  }

  public void setListCellRenderer(ListCellRenderer renderer) {
    list.setCellRenderer(renderer);
  }

  public ListModel getModel() {
    return model;
  }

  @Override
  public void setEnabled(boolean b) {
    enabled = b;
    refreshButtons();
  }

  public void removeItemAt(int index) {
    model.remove(index);
  }

  public void removeItem(T item) {
    model.remove(item);
  }

  public void removeAllItems() {
    model.clear();
  }

  public Object getItemAt(int index) {
    return model.getElementAt(index);
  }

  public List<T> getItems() {
    return model;
  }

  public Object[] getItemsAsArray() {
    return model.toArray();
  }

  public int getItemCount() {
    return model.getSize();
  }

  public void addItem(T item) {
    model.add(item);
  }

  public void addItems(T[] items) {
    for (T item : items) {
      addItem(item);
    }
  }

  public void addItems(Collection<T> items) {
    model.addAll(items);
  }

  private void setup(ShuffleInterface<T> si, String labelText, boolean allowUpDown) {
    enabled = true;

    text = new JLabel(labelText);
    si.setParent(this);
    entry = si;

    add = new JButton("Add");
    remove = new JButton("Remove");

    add.addActionListener(this);
    remove.addActionListener(this);

    if (allowUpDown) {
      up = new JButton(new ImageIcon(getClass().getResource(UP_ICON)));
      down = new JButton(new ImageIcon(getClass().getResource(DOWN_ICON)));

      up.setEnabled(false);
      down.setEnabled(false);

      up.addActionListener(this);
      down.addActionListener(this);
    }

    model = new GenericListModel<T>();
    list = new JList(model);
    list.addListSelectionListener(this);

    listScroll = new JScrollPane(list);
    JComponent entryComponent = entry.getComponent();

    int height1 = entryComponent.getPreferredSize().height;
    int height2 = add.getPreferredSize().height;
    int width1 = remove.getPreferredSize().width;
    int width2 = text.getPreferredSize().width - width1;
    if (width2 < 0) {
      width2 = 0;
    }

    int[] rows = new int[] {height1, height2, height2, height2, height2, TableLayout.FILL};
    int[] columns = new int[] {width1, width2, TableLayout.FILL};
    TableLayout layout = new TableLayout(rows, columns, 5, 5);
    setLayout(layout);

    add(entryComponent, new Rectangle(0, 0, 3, 1));

    add(listScroll, new Rectangle(1, 1, 2, 5));

    add(add, new Rectangle(0, 1, 1, 1));
    add(remove, new Rectangle(0, 2, 1, 1));

    if (allowUpDown) {
      add(up, new Rectangle(0, 3, 1, 1));
      add(down, new Rectangle(0, 4, 1, 1));
    }

    refreshButtons();
  }

  public void refreshButtons() {
    entry.setEnabled(enabled);
    list.setEnabled(enabled);
    listScroll.setEnabled(enabled);

    add.setEnabled(enabled && entry.getObject() != null);

    boolean selection = !list.isSelectionEmpty();
    remove.setEnabled(enabled && selection);

    if (up != null && list.getSelectedIndices().length == 1) {
      int index = list.getSelectedIndex();
      up.setEnabled(enabled && index > 0);
      down.setEnabled(enabled && index < model.getSize() - 1);
    }
  }

  public void setAddButtonText(String text) {
    add.setText(text);
  }

  public void setRemoveButtonText(String text) {
    remove.setText(text);
  }

  // // EVENT HANDLERS //////////////////////////////////////////////////////

  @Override
  public void valueChanged(ListSelectionEvent e) {
    add.setEnabled(entry.getObject() != null);

    boolean selection = !list.isSelectionEmpty();
    remove.setEnabled(selection);

    if (up != null) {
      int index = list.getSelectedIndex();
      up.setEnabled(selection && index > 0);
      down.setEnabled(selection && index < model.getSize() - 1);
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == add) {
      T o = entry.getObject();
      if (o != null) {
        entry.clear();
        model.add(o);
        list.setSelectedIndex(model.getSize() - 1);
      }
    } else {
      int[] indices = list.getSelectedIndices();

      if (e.getSource() == remove) {
        for (int i = indices.length - 1; i >= 0; i--) {
          model.remove(indices[i]);
        }
      } else if (e.getSource() == up) {
        if (indices[0] > 0) {
          T item = model.get(indices[0]);
          model.remove(indices[0]);
          model.add(indices[0] - 1, item);
          list.updateUI();
          list.setSelectedIndex(indices[0] - 1);
        }
      } else if (e.getSource() == down) {
        if (indices[0] < model.getSize() - 1) {
          T item = model.get(indices[0]);
          model.remove(indices[0]);
          model.add(indices[0] + 1, item);
          list.updateUI();
          list.setSelectedIndex(indices[0] + 1);
        }
      }
    }
  }

  public interface ShuffleInterface<T> {
    void setParent(JComponent shuffle);

    void setEnabled(boolean enabled);

    JComponent getComponent();

    T getObject();

    boolean canAdd();

    void clear();
  }

  public static class DefaultShuffleComponent extends JPanel
      implements ShuffleInterface<String>, KeyListener {
    protected JShuffleList list;
    protected JLabel label;
    protected JTextField field;

    public DefaultShuffleComponent(String text) {
      label = new JLabel(text + " ");
      field = new JTextField();

      this.setLayout(new BorderLayout());
      this.add(label, BorderLayout.WEST);
      this.add(field, BorderLayout.CENTER);
    }

    /*
     * (non-Javadoc)
     * @see com.dytech.gui.JShuffleList.ShuffleInterface#canAdd()
     */
    @Override
    public boolean canAdd() {
      return field.getText().length() > 0;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.dytech.gui.JShuffleList.ShuffleInterface#setParent(com.dytech
     * .gui.JShuffleList)
     */
    @Override
    public void setParent(JComponent shuffle) {
      list = (JShuffleList) shuffle;
      field.addKeyListener(this);
    }

    /*
     * (non-Javadoc)
     * @see com.dytech.gui.JShuffleList.ShuffleInterface#getComponent()
     */
    @Override
    public JComponent getComponent() {
      return this;
    }

    /*
     * (non-Javadoc)
     * @see com.dytech.gui.JShuffleList.ShuffleInterface#getObject()
     */
    @Override
    public String getObject() {
      String text = field.getText();
      if (text.length() == 0) {
        text = null;
      }
      return text;
    }

    /*
     * (non-Javadoc)
     * @see com.dytech.gui.JShuffleList.ShuffleInterface#clear()
     */
    @Override
    public void clear() {
      field.setText("");
    }

    @Override
    public void keyReleased(KeyEvent e) {
      list.refreshButtons();
    }

    @Override
    public void keyPressed(KeyEvent e) {
      // We do not care about this event.
    }

    @Override
    public void keyTyped(KeyEvent e) {
      // We do not care about this event.
    }
  }
}
