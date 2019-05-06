/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
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

import com.tle.common.gui.models.GenericListModel;
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
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author Nicholas Read
 * @created 13 June 2003 (Friday the 13th!!)
 */
public class JShuffleEditList<T> extends JComponent
    implements ActionListener, ListSelectionListener, KeyListener {
  public static final String UP_ICON = "icons/up.gif";
  public static final String DOWN_ICON = "icons/down.gif";
  public static final String DEFAULT_TEXT = "Enter an item";

  private boolean enabled;

  protected JButton add;
  protected JButton remove;
  protected JButton up;
  protected JButton down;
  protected JButton edit;

  protected ShuffleInterface<T> entry;
  protected JList list;
  protected GenericListModel<T> model;
  protected JScrollPane listScroll;

  public JShuffleEditList(ShuffleInterface<T> si, String text, boolean allowUpDown) {
    setup(si, text, allowUpDown);
  }

  public JShuffleEditList(ShuffleInterface<T> si, boolean allowUpDown) {
    this(si, DEFAULT_TEXT, allowUpDown);
  }

  public void setListCellRenderer(ListCellRenderer renderer) {
    list.setCellRenderer(renderer);
  }

  public GenericListModel<T> getModel() {
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

  public T getItemAt(int index) {
    return model.get(index);
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

    if (labelText == null) {
      labelText = DEFAULT_TEXT;
    }

    JLabel text = new JLabel(labelText);
    si.setParent(this);
    entry = si;

    add = new JButton("Add");
    remove = new JButton("Remove");
    edit = new JButton("Update");
    edit.addActionListener(this);
    add.addActionListener(this);
    remove.addActionListener(this);
    edit.setEnabled(false);
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

    int[] rows = new int[] {height1, height2, height2, height2, height2, height2, TableLayout.FILL};
    int[] columns = new int[] {width1, width2, TableLayout.FILL};
    TableLayout layout = new TableLayout(rows, columns, 5, 5);
    setLayout(layout);

    add(entryComponent, new Rectangle(0, 0, 3, 1));

    add(listScroll, new Rectangle(1, 1, 2, 5));

    add(add, new Rectangle(0, 1, 1, 1));
    add(remove, new Rectangle(0, 2, 1, 1));
    add(edit, new Rectangle(0, 3, 1, 1));

    if (allowUpDown) {
      add(up, new Rectangle(0, 4, 1, 1));
      add(down, new Rectangle(0, 5, 1, 1));
    }

    refreshButtons();
  }

  public void refreshButtons() {
    entry.setEnabled(enabled);
    list.setEnabled(enabled);
    listScroll.setEnabled(enabled);

    add.setEnabled(enabled && entry.canAdd());

    boolean selection = !list.isSelectionEmpty();
    remove.setEnabled(enabled && selection);
    edit.setEnabled(enabled && selection && entry.canUpdate());

    if (up != null) {
      int index = list.getSelectedIndex();
      up.setEnabled(enabled && selection && index > 0);
      down.setEnabled(enabled && selection && index < model.getSize() - 1);
    }
  }

  // // EVENT HANDLERS //////////////////////////////////////////////////////

  @Override
  public void valueChanged(ListSelectionEvent e) {
    add.setEnabled(entry.canAdd());

    boolean selection = !list.isSelectionEmpty();
    remove.setEnabled(selection);
    if (selection) {
      entry.editObject(model.get(list.getSelectedIndex()));
    }
    edit.setEnabled(selection);

    if (up != null) {
      int index = list.getSelectedIndex();
      up.setEnabled(selection && index > 0);
      down.setEnabled(selection && index < model.getSize() - 1);
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == add) {
      T o = entry.getObject();
      if (o != null) {
        model.add(o);
        list.clearSelection();
        entry.clear();
      }
    } else if (e.getSource() == edit) {
      T o = entry.updateObject();
      if (o != null) {
        int index = list.getSelectedIndex();
        model.set(index, o);
        list.clearSelection();
        entry.clear();
      }
    } else {
      if (!list.isSelectionEmpty()) {
        int index = list.getSelectedIndex();

        if (e.getSource() == remove) {
          model.remove(index);
          // clear the edit box
          entry.clear();
        } else if (e.getSource() == up) {
          if (index > 0) {
            T item = model.get(index);
            model.remove(index);
            model.add(index - 1, item);
            list.updateUI();
            list.setSelectedIndex(index - 1);
          }
        } else if (e.getSource() == down) {
          if (index < model.getSize() - 1) {
            T item = model.get(index);
            model.remove(index);
            model.add(index + 1, item);
            list.updateUI();
            list.setSelectedIndex(index + 1);
          }
        }
      }
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
    refreshButtons();
  }

  @Override
  public void keyPressed(KeyEvent e) {
    // We do not care about this event.
  }

  @Override
  public void keyTyped(KeyEvent e) {
    // We do not care about this event.
  }

  public interface ShuffleInterface<T> extends JShuffleList.ShuffleInterface<T> {
    void editObject(T o);

    T updateObject();

    boolean canUpdate();
  }

  public static class DefaultShuffleComponent extends JShuffleList.DefaultShuffleComponent
      implements ShuffleInterface<String> {
    private JShuffleEditList editList;

    public DefaultShuffleComponent(String text) {
      super(text);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.dytech.gui.JShuffleEditList.ShuffleInterface#editObject(java.
     * lang.Object)
     */
    @Override
    public void editObject(String o) {
      field.setText(o);
    }

    @Override
    public void setParent(JComponent shuffle) {
      editList = (JShuffleEditList) shuffle;
      field.addKeyListener(this);
    }

    @Override
    public void keyReleased(KeyEvent e) {
      editList.refreshButtons();
    }

    @Override
    public boolean canUpdate() {
      return canAdd();
    }

    /*
     * (non-Javadoc)
     * @see com.dytech.gui.JShuffleEditList.ShuffleInterface#updateObject()
     */
    @Override
    public String updateObject() {
      return getObject();
    }
  }
}
