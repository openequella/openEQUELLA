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

import com.dytech.common.collections.CombinedCollection;
import com.dytech.gui.TableLayout;
import com.tle.admin.gui.common.actions.DownAction;
import com.tle.admin.gui.common.actions.JTextlessButton;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.admin.gui.common.actions.UpAction;
import com.tle.client.gui.popup.TreePopupListener;
import com.tle.common.applet.gui.AppletGuiUtils;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public abstract class AbstractTreeWithView<
        NODE_TYPE extends DefaultMutableTreeNode,
        VIEW_TYPE extends TreeWithViewInterface<NODE_TYPE>>
    extends JPanel {
  private final GenericTreeModel<NODE_TYPE> model;

  private JTree tree;
  private JPanel editorContainer;
  private List<TLEAction> upDownActions;
  private List<TLEAction> additionalActions;

  private NODE_TYPE currentSelection;
  private VIEW_TYPE currentEditor;

  public AbstractTreeWithView() {
    this(new GenericTreeModel<NODE_TYPE>(null));
  }

  public AbstractTreeWithView(GenericTreeModel<NODE_TYPE> model) {
    this.model = model;
  }

  public void initialise() {
    initialise(false, -1);
  }

  public void initialise(boolean allowReordering, int fixedEditorWidth) {
    additionalActions = getAdditionalActions();
    if (additionalActions == null) {
      additionalActions = Collections.emptyList();
    }

    upDownActions = new ArrayList<TLEAction>();
    if (allowReordering) {
      upDownActions.add(upAction);
      upDownActions.add(downAction);
    }

    editorContainer = new JPanel(new GridLayout(1, 1));
    editorContainer.setBorder(AppletGuiUtils.DEFAULT_BORDER);
    editorContainer.add(getNoSelectionComponent());

    tree = new JTree(model.getUnderlyingTreeModel());
    tree.addMouseListener(
        new TreePopupListener(
            tree, new CombinedCollection<TLEAction>(additionalActions, upDownActions)));
    tree.addTreeSelectionListener(
        new TreeSelectionListener() {
          @Override
          public void valueChanged(TreeSelectionEvent e) {
            NODE_TYPE newSelection = getCurrentTreeSelection();
            if (!Objects.equals(currentSelection, newSelection)) {
              saveCurrentSelection();
              loadCurrentSelection();

              update();
            }
          }
        });

    JScrollPane treeScroller = new JScrollPane(tree);

    JComponent buttonsPanel = getButtonsPanel();

    JButton upButton = new JTextlessButton(upAction);
    JButton downButton = new JTextlessButton(downAction);

    final int height1 = upButton.getPreferredSize().height;
    final int height2 = buttonsPanel.getPreferredSize().height;
    final int width1 = upButton.getPreferredSize().width;
    final int width2 =
        fixedEditorWidth >= 0 ? TableLayout.FILL : buttonsPanel.getPreferredSize().width;
    final int width3 = fixedEditorWidth >= 0 ? fixedEditorWidth : TableLayout.FILL;

    final int[] rows = {
      TableLayout.FILL, height1, height1, TableLayout.FILL, height2,
    };
    final int[] cols =
        allowReordering
            ? new int[] {
              width1, width2, width3,
            }
            : new int[] {
              width2, width3,
            };

    setLayout(new TableLayout(rows, cols));

    int columnStart = 0;
    if (allowReordering) {
      columnStart = 1;
      add(upButton, new Rectangle(0, 1, 1, 1));
      add(downButton, new Rectangle(0, 2, 1, 1));
    }
    add(treeScroller, new Rectangle(columnStart, 0, 1, 4));
    add(buttonsPanel, new Rectangle(columnStart, 4, 1, 1));
    add(editorContainer, new Rectangle(columnStart + 1, 0, 1, 4));

    update();
  }

  protected JComponent getButtonsPanel() {
    int largestWidth = 0;
    int largestHeight = 0;

    List<JButton> buttons = new ArrayList<JButton>();
    for (TLEAction action : additionalActions) {
      JButton button = new JButton(action);
      largestHeight = Math.max(largestHeight, button.getPreferredSize().height);
      largestWidth = Math.max(largestWidth, button.getPreferredSize().width);
      buttons.add(button);
    }

    int[] rows = {
      largestHeight,
    };
    int[] cols = new int[buttons.size() + 2];
    Arrays.fill(cols, largestWidth);
    cols[0] = TableLayout.FILL;
    cols[buttons.size() + 1] = TableLayout.FILL;

    JPanel panel = new JPanel(new TableLayout(rows, cols));
    for (ListIterator<JButton> iter = buttons.listIterator(); iter.hasNext(); ) {
      int index = iter.nextIndex();
      JButton button = iter.next();
      panel.add(button, new Rectangle(index + 1, 0, 1, 1));
    }
    return panel;
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    update();
  }

  public void setTreeCellRenderer(TreeCellRenderer renderer) {
    tree.setCellRenderer(renderer);
  }

  public void load(NODE_TYPE root) {
    model.setRoot(root);
  }

  public NODE_TYPE save() {
    saveCurrentSelection();
    return model.getRoot();
  }

  private void loadCurrentSelection() {
    currentSelection = getCurrentTreeSelection();
    currentEditor = getEditor(currentSelection);

    editorContainer.removeAll();
    if (currentEditor != null) {
      currentEditor.setup();
      currentEditor.addNameListener(
          new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
              updateSelectedName();
            }
          });
      currentEditor.load(currentSelection);

      editorContainer.add(currentEditor.getComponent());
    } else {
      editorContainer.add(getNoSelectionComponent());
    }
    updateUI();
  }

  private void saveCurrentSelection() {
    if (currentSelection != null) {
      currentEditor.save(currentSelection);
    }
  }

  @SuppressWarnings("unchecked")
  protected NODE_TYPE getCurrentTreeSelection() {
    return (NODE_TYPE) tree.getLastSelectedPathComponent();
  }

  protected void setTreeSelection(NODE_TYPE node) {
    tree.clearSelection();
    if (node != null) {
      tree.setSelectionPath(new TreePath(node.getPath()));
    }
  }

  public void setTreeSelectionMode(int selectionMode) {
    tree.getSelectionModel().setSelectionMode(selectionMode);
  }

  private void update() {
    for (TLEAction action : upDownActions) {
      action.update();
    }

    for (TLEAction action : additionalActions) {
      action.update();
    }
  }

  private void updateSelectedName() {
    saveCurrentSelection();
    model.nodeChanged(currentSelection);
  }

  private TLEAction upAction =
      new UpAction() {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          NODE_TYPE node = getCurrentTreeSelection();
          model.moveUp(node);
          setTreeSelection(node);
        }

        @Override
        public void update() {
          setEnabled(
              AbstractTreeWithView.this.isEnabled() && model.canMoveUp(getCurrentTreeSelection()));
        }
      };

  private TLEAction downAction =
      new DownAction() {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          NODE_TYPE node = getCurrentTreeSelection();
          model.moveDown(node);
          setTreeSelection(node);
        }

        @Override
        public void update() {
          setEnabled(
              AbstractTreeWithView.this.isEnabled()
                  && model.canMoveDown(getCurrentTreeSelection()));
        }
      };

  protected Component getNoSelectionComponent() {
    return new JLabel();
  }

  protected abstract VIEW_TYPE getEditor(NODE_TYPE currentSelection);

  protected abstract List<TLEAction> getAdditionalActions();
}
