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

package com.tle.admin.gui.common;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.Changeable;

public abstract class AbstractChoiceList<STATE_TYPE, CHOICE_TYPE, CHOICE_COMPONENT_TYPE>
    extends JPanel implements Changeable {
  protected final Map<CHOICE_COMPONENT_TYPE, DynamicChoicePanel<STATE_TYPE>> choices =
      new HashMap<CHOICE_COMPONENT_TYPE, DynamicChoicePanel<STATE_TYPE>>();
  protected final ChangeDetector changeDetector = new ChangeDetector();

  private boolean removeStateForNonSelectedChoices = true;
  private boolean loadStateForNonSelectedChoices = false;

  public void setRemoveStateForNonSelectedChoices(boolean removeStateForNonSelectedChoices) {
    this.removeStateForNonSelectedChoices = removeStateForNonSelectedChoices;
  }

  public void setLoadStateForNonSelectedChoices(boolean loadStateForNonSelectedChoices) {
    this.loadStateForNonSelectedChoices = loadStateForNonSelectedChoices;
  }

  public boolean isRemoveStateForNonSelectedChoices() {
    return removeStateForNonSelectedChoices;
  }

  public boolean isLoadStateForNonSelectedChoices() {
    return loadStateForNonSelectedChoices;
  }

  @Override
  public boolean hasDetectedChanges() {
    return changeDetector.hasDetectedChanges();
  }

  @Override
  public void clearChanges() {
    changeDetector.clearChanges();
  }

  protected final void choiceSelected(CHOICE_COMPONENT_TYPE newSelection) {
    updateChoicePanels();
    choices.get(newSelection).choiceSelected();
  }

  protected final void choiceDeselected(CHOICE_COMPONENT_TYPE oldSelection) {
    updateChoicePanels();
    DynamicChoicePanel<STATE_TYPE> dcp = choices.get(oldSelection);
    if (dcp != null) {
      choices.get(oldSelection).choiceDeselected();
    }
  }

  @Override
  public void setEnabled(final boolean enabled) {
    if (enabled != isEnabled()) {
      super.setEnabled(enabled);
      setChoicesEnabled(enabled);
      updateChoicePanels();
    }
  }

  public Collection<DynamicChoicePanel<STATE_TYPE>> getChoicePanels() {
    return Collections.unmodifiableCollection(choices.values());
  }

  public DynamicChoicePanel<STATE_TYPE> getSelectedPanel() {
    for (Entry<CHOICE_COMPONENT_TYPE, DynamicChoicePanel<STATE_TYPE>> choice : choices.entrySet()) {
      final CHOICE_COMPONENT_TYPE cct = choice.getKey();
      final DynamicChoicePanel<STATE_TYPE> choicePanel = choice.getValue();

      if (isEnabledAndSelected(cct)) {
        return choicePanel;
      }
    }
    return null;
  }

  public void save(STATE_TYPE state) {
    clearSavedChoiceId(state);

    for (Entry<CHOICE_COMPONENT_TYPE, DynamicChoicePanel<STATE_TYPE>> choice : choices.entrySet()) {
      final CHOICE_COMPONENT_TYPE cct = choice.getKey();
      final DynamicChoicePanel<STATE_TYPE> choicePanel = choice.getValue();

      if (isEnabledAndSelected(cct)) {
        setSavedChoiceId(state, choicePanel.getId());
        choicePanel.save(state);
      } else if (removeStateForNonSelectedChoices) {
        choicePanel.removeSavedState(state);
      }
    }
  }

  public void load(STATE_TYPE state) {
    final String choiceId = getSavedChoiceId(state);
    for (Entry<CHOICE_COMPONENT_TYPE, DynamicChoicePanel<STATE_TYPE>> choice : choices.entrySet()) {
      final DynamicChoicePanel<STATE_TYPE> choicePanel = choice.getValue();
      if (choicePanel.getId().equals(choiceId)) {
        setSelectedChoiceComponent(choice.getKey());
        choicePanel.load(state);
      } else if (loadStateForNonSelectedChoices) {
        choicePanel.load(state);
      }
    }
  }

  protected void clearSavedChoiceId(STATE_TYPE state) {
    setSavedChoiceId(state, null);
  }

  public final void loadChoices(Iterable<CHOICE_TYPE> choiceList) {
    // Ignore old panels
    removeAndIgnoreOldComponents();
    for (DynamicChoicePanel<STATE_TYPE> dcp : choices.values()) {
      changeDetector.ignore(dcp);
    }

    // Clear any old choices
    choices.clear();

    if (choiceList != null) {
      _loadChoices(choiceList);

      for (DynamicChoicePanel<STATE_TYPE> dcp : choices.values()) {
        changeDetector.watch(dcp);
      }
    }
    updateChoicePanels();

    // Make sure we clear any changes that have occurred by old choice
    // panels, or by modifying the combobox state.
    changeDetector.clearChanges();
  }

  /**
   * Remove any old components from the UI, and ignore them in the <code>changedetector</code>
   * member.
   */
  protected abstract void removeAndIgnoreOldComponents();

  protected abstract void _loadChoices(Iterable<CHOICE_TYPE> choiceList);

  protected abstract boolean isEnabledAndSelected(CHOICE_COMPONENT_TYPE cct);

  protected abstract void setSelectedChoiceComponent(CHOICE_COMPONENT_TYPE cct);

  protected abstract void setChoicesEnabled(boolean enabled);

  protected abstract void updateChoicePanels();

  public abstract boolean isSelectionEmpty();

  public abstract void setSavedChoiceId(STATE_TYPE state, String choiceId);

  public abstract String getSavedChoiceId(STATE_TYPE state);

  public abstract String getChoiceId(CHOICE_TYPE choice);

  public abstract String getChoiceTitle(CHOICE_TYPE choice);

  public abstract DynamicChoicePanel<STATE_TYPE> getChoicePanel(CHOICE_TYPE choice);
}
