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

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.Changeable;
import com.tle.common.gui.models.GenericListModel;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public abstract class AbstractListWithView<
        LIST_TYPE,
        VIEW_TYPE extends ListWithViewInterface<LIST_TYPE>,
        MODEL_TYPE extends GenericListModel<LIST_TYPE>>
    extends JPanel implements Changeable {
  protected final MODEL_TYPE model;
  protected final JPanel editorContainer;
  protected final ChangeDetector changeDetector;

  private LIST_TYPE currentSelection;
  private VIEW_TYPE currentEditor;

  public AbstractListWithView() {
    model = createModel();

    editorContainer = new JPanel(new GridLayout(1, 1));
    editorContainer.add(getNoSelectionComponent());

    changeDetector = new ChangeDetector();
    changeDetector.watch(model);
  }

  public void load(Collection<LIST_TYPE> newElements) {
    model.clear();
    if (newElements != null) {
      model.addAll(newElements);
    }
    changeDetector.clearChanges();
  }

  public List<LIST_TYPE> save() {
    saveCurrentSelection();
    return new ArrayList<LIST_TYPE>(model);
  }

  public int getListSize() {
    return model.size();
  }

  /** Return a new model for you list. */
  protected abstract MODEL_TYPE createModel();

  protected void onListSelectionChange() {
    LIST_TYPE newSelection = getSelectedListValue();
    if (!listTypeEquals(currentSelection, newSelection)) {
      saveCurrentSelection();
      loadCurrentSelection();
    }
  }

  protected boolean listTypeEquals(LIST_TYPE lhs, LIST_TYPE rhs) {
    return Objects.equals(lhs, rhs);
  }

  private LIST_TYPE getSelectedListValue() {
    int index = getSelectedIndex();
    if (index >= 0) {
      return model.get(index);
    } else {
      return null;
    }
  }

  private void loadCurrentSelection() {
    // Ignore any changes the old editor is making
    if (currentEditor != null) {
      changeDetector.ignore(currentEditor);
      if (currentEditor.hasDetectedChanges()) {
        changeDetector.forceChange(this);
      }
    }

    currentSelection = getSelectedListValue();
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

      // Watch the editor for changes
      currentEditor.clearChanges();
      changeDetector.watch(currentEditor);

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

  private void updateSelectedName() {
    saveCurrentSelection();
    model.set(getSelectedIndex(), currentSelection);
  }

  @Override
  public boolean hasDetectedChanges() {
    return changeDetector.hasDetectedChanges();
  }

  @Override
  public void clearChanges() {
    changeDetector.clearChanges();
  }

  protected Component getNoSelectionComponent() {
    JLabel label = new JLabel(getNoSelectionText());

    // Centre it!
    label.setVerticalAlignment(SwingConstants.CENTER);
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setVerticalTextPosition(SwingConstants.CENTER);
    label.setHorizontalTextPosition(SwingConstants.CENTER);

    return label;
  }

  protected abstract String getNoSelectionText();

  protected abstract int getSelectedIndex();

  protected abstract VIEW_TYPE getEditor(LIST_TYPE currentSelection);
}
