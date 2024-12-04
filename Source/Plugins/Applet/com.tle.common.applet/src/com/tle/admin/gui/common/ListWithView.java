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

package com.tle.admin.gui.common;

import com.dytech.edge.common.Constants;
import com.tle.admin.gui.common.actions.AddAction;
import com.tle.admin.gui.common.actions.DownAction;
import com.tle.admin.gui.common.actions.JTextlessButton;
import com.tle.admin.gui.common.actions.RemoveAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.admin.gui.common.actions.UpAction;
import com.tle.client.gui.popup.ListPopupListener;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.gui.models.GenericListModel;
import com.tle.common.i18n.CurrentLocale;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("nls")
public abstract class ListWithView<LIST_TYPE, VIEW_TYPE extends ListWithViewInterface<LIST_TYPE>>
    extends AbstractListWithView<
        LIST_TYPE, ListWithViewInterface<LIST_TYPE>, GenericListModel<LIST_TYPE>> {
  private final List<TLEAction> actions;
  private final boolean enableAddRemove;

  protected final JList<LIST_TYPE> list;

  private ListPopupListener listPopupListener;

  public ListWithView() {
    this(false);
  }

  public ListWithView(boolean enableUpDown) {
    this(enableUpDown, true);
  }

  public ListWithView(
      boolean enableUpDown, boolean enableAddRemove, TLEAction... additionalActions) {
    this.enableAddRemove = enableAddRemove;

    actions = new ArrayList<TLEAction>();
    for (TLEAction additionalAction : additionalActions) {
      actions.add(additionalAction);
    }
    actions.add(upAction);
    actions.add(downAction);
    actions.add(addAction);
    actions.add(removeAction);

    final JTextlessButton upButton = new JTextlessButton(upAction);
    final JTextlessButton downButton = new JTextlessButton(downAction);
    final JButton addButton = new JButton(addAction);
    final JButton removeButton = new JButton(removeAction);

    list = new JList<>(model);
    list.addMouseListener(new ListPopupListener(list, actions));
    list.addListSelectionListener(
        new ListSelectionListener() {
          @Override
          public void valueChanged(ListSelectionEvent e) {
            onListSelectionChange();
            update();
          }
        });

    final JPanel left =
        new JPanel(
            new MigLayout(
                "insets 0, fill, wrap 5, hidemode 2", "[][grow][][][grow]", "[grow][][][grow][]"));
    left.add(new JScrollPane(list), "skip, grow, span 4 4");
    left.add(upButton);
    left.add(downButton);
    left.add(addButton, "skip 3, align right");
    left.add(removeButton);

    if (!enableUpDown) {
      upButton.setVisible(false);
      downButton.setVisible(false);
    }

    if (!enableAddRemove) {
      addButton.setVisible(false);
      removeButton.setVisible(false);
    }

    // This makes the left-panel more "stable" and reliable in the splitter.
    left.setMinimumSize(new Dimension(200, 0));
    left.setPreferredSize(left.getMaximumSize());

    JSplitPane split = AppletGuiUtils.createSplitPane();
    split.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    split.setContinuousLayout(true);
    split.setResizeWeight(0.1);

    split.add(left, JSplitPane.LEFT);
    split.add(editorContainer, JSplitPane.RIGHT);

    setLayout(new GridLayout(1, 1));
    add(split);

    update();
  }

  protected void addAction(TLEAction action) {
    actions.add(action);
    list.removeMouseListener(listPopupListener);
    listPopupListener = new ListPopupListener(list, actions);
    list.addMouseListener(listPopupListener);
  }

  @Override
  protected GenericListModel<LIST_TYPE> createModel() {
    return new GenericListModel<>();
  }

  @SuppressWarnings("unchecked")
  public void setListCellRenderer(ListCellRenderer<?> renderer) {
    list.setCellRenderer((ListCellRenderer<? super LIST_TYPE>) renderer);
  }

  @Override
  protected int getSelectedIndex() {
    return list.getSelectedIndex();
  }

  @Override
  protected String getNoSelectionText() {
    return CurrentLocale.get(
        "com.tle.admin.gui.common.listwithview.choose"
            + (enableAddRemove ? Constants.BLANK : "NoAddRemove"));
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);

    if (!enabled) {
      list.clearSelection();
    }

    list.setEnabled(enabled);
    update();
  }

  private void update() {
    for (TLEAction action : actions) {
      action.update();
    }
  }

  protected void addEntry(LIST_TYPE entry) {
    model.add(entry);
    list.setSelectedIndex(model.getSize() - 1);
  }

  private final TLEAction addAction =
      new AddAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          LIST_TYPE entry = createElement();
          if (entry != null) {
            addEntry(entry);
          }
        }

        @Override
        public void update() {
          setEnabled(ListWithView.this.isEnabled());
        }
      };

  private final TLEAction removeAction =
      new RemoveAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          int result =
              JOptionPane.showConfirmDialog(
                  ListWithView.this,
                  CurrentLocale.get("com.tle.admin.gui.common.abstractlistwithview.confirm"),
                  CurrentLocale.get("com.tle.admin.gui.common.abstractlistwithview.delete"),
                  JOptionPane.YES_NO_OPTION);

          if (result == JOptionPane.YES_OPTION) {
            int[] indices = list.getSelectedIndices();
            for (int i = indices.length - 1; i > -1; i--) {
              model.remove(indices[i]);
            }
          }
        }

        @Override
        public void update() {
          setEnabled(!list.isSelectionEmpty());
        }
      };

  private final TLEAction upAction =
      new UpAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          int i = list.getSelectedIndex();
          model.add(i - 1, model.remove(i));
          list.setSelectedIndex(i - 1);
        }

        @Override
        public void update() {
          int[] i = list.getSelectedIndices();
          setEnabled(i.length == 1 && i[0] >= 1);
        }
      };

  private final TLEAction downAction =
      new DownAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          int i = list.getSelectedIndex();
          model.add(i + 1, model.remove(i));
          list.setSelectedIndex(i + 1);
        }

        @Override
        public void update() {
          int[] i = list.getSelectedIndices();
          setEnabled(i.length == 1 && i[0] < model.size() - 1);
        }
      };

  public JList<LIST_TYPE> getList() {
    return list;
  }

  /**
   * This method is invoked from the EDT. For fast operations or modal dialog displays, returning a
   * LIST_TYPE object will immediate add it as the last entry and select it. If a possibly
   * long-running operation needs to occur here, use a GlassSwing worker, invoke <code>
   * addEntry(LIST_TYPE)</code> in from <code>finished()</code>, and return null immediately.
   */
  protected abstract LIST_TYPE createElement();
}
