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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.google.common.collect.Maps;
import com.tle.common.Format;
import com.tle.common.NameValue;

/**
 * Choice bro
 *
 * @author Aaron
 */
@SuppressWarnings("nls")
public abstract class CheckboxChoiceList<STATE_TYPE, CHOICE_TYPE>
    extends AbstractChoiceList<STATE_TYPE, CHOICE_TYPE, NameValue> implements ActionListener {
  private final Map<NameValue, JCheckBox> checkboxes = new HashMap<NameValue, JCheckBox>();
  private final JPanel checkboxContainer;
  private final JPanel choiceSink;
  private boolean enabled = true;

  public CheckboxChoiceList(String labelText, int columns) {
    setLayout(new MigLayout("wrap 1, insets 0, hidemode 3", "[grow, fill]"));

    add(new JLabel(labelText));

    checkboxContainer = new JPanel(new MigLayout("fillx, wrap " + columns));
    add(checkboxContainer);

    choiceSink = new JPanel(new MigLayout("wrap 1, insets 0, gap 0 0", "[grow, fill]"));
    add(choiceSink);
  }

  @Override
  protected void removeAndIgnoreOldComponents() {
    for (JCheckBox box : checkboxes.values()) {
      changeDetector.ignore(box);
    }
    checkboxes.clear();
    checkboxContainer.removeAll();
  }

  @Override
  protected void _loadChoices(Iterable<CHOICE_TYPE> choiceList) {
    final List<NameValue> nvs = new ArrayList<NameValue>();
    final Map<String, CHOICE_TYPE> choiceMap = Maps.newHashMap();
    for (CHOICE_TYPE choice : choiceList) {
      final String choiceId = getChoiceId(choice);
      final NameValue nv = new NameValue(getChoiceTitle(choice), choiceId);
      final DynamicChoicePanel<STATE_TYPE> choicePanel = getChoicePanel(choice);
      choiceMap.put(choiceId, choice);
      choicePanel.setId(choiceId);

      nvs.add(nv);
      choices.put(nv, choicePanel);
    }

    Collections.sort(nvs, Format.NAME_VALUE_COMPARATOR);
    for (NameValue nv : nvs) {
      JCheckBox box = new JCheckBox(nv.getName());
      box.addActionListener(this);
      box.addActionListener(getChoicePanel(choiceMap.get(nv.getValue())));
      checkboxes.put(nv, box);
      checkboxContainer.add(box);
      changeDetector.watch(box);
    }
  }

  @Override
  protected void updateChoicePanels() {
    if (enabled) {
      for (Map.Entry<NameValue, DynamicChoicePanel<STATE_TYPE>> choice : choices.entrySet()) {
        NameValue nv = choice.getKey();
        DynamicChoicePanel<STATE_TYPE> dcp = choice.getValue();
        List<Component> current = Arrays.asList(choiceSink.getComponents());

        JCheckBox box = checkboxes.get(nv);

        // it can be null. i don't know how.
        if (box != null) {
          if (box.isSelected()) {
            if (!current.contains(dcp)) {
              // box.addActionListener(dcp);
              choiceSink.add(dcp.getSeparator());
              choiceSink.add(dcp);
            }
          } else {
            if (current.contains(dcp)) {
              choiceSink.remove(dcp.getSeparator());
              choiceSink.remove(dcp);
            }
          }
        }
      }
      choiceSink.setVisible(true);
      choiceSink.revalidate();
    } else {
      choiceSink.setVisible(false);
    }
  }

  @Override
  public void save(STATE_TYPE state) {
    clearSavedChoiceId(state);

    Set<String> choiceIds = new HashSet<String>();
    for (Entry<NameValue, DynamicChoicePanel<STATE_TYPE>> choice : choices.entrySet()) {
      final NameValue cct = choice.getKey();
      final DynamicChoicePanel<STATE_TYPE> choicePanel = choice.getValue();

      if (isEnabledAndSelected(cct)) {
        choiceIds.add(choicePanel.getId());
        choicePanel.save(state);
      } else if (isRemoveStateForNonSelectedChoices()) {
        choicePanel.removeSavedState(state);
      }
    }
    setSavedChoiceIds(state, choiceIds);
  }

  @Override
  public void load(STATE_TYPE state) {
    final Collection<String> choiceIds = getSavedChoiceIds(state);
    for (Entry<NameValue, DynamicChoicePanel<STATE_TYPE>> choice : choices.entrySet()) {
      final DynamicChoicePanel<STATE_TYPE> choicePanel = choice.getValue();
      if (choiceIds.contains(choicePanel.getId())) {
        setSelectedChoiceComponent(choice.getKey());
        choicePanel.load(state);
      } else if (isLoadStateForNonSelectedChoices()) {
        choicePanel.load(state);
      }
    }
    // since checkboxes don't fire events when setSelected() is called:
    updateChoicePanels();
  }

  public void afterLoad(STATE_TYPE state) {
    final Collection<String> choiceIds = getSavedChoiceIds(state);
    for (Entry<NameValue, DynamicChoicePanel<STATE_TYPE>> choice : choices.entrySet()) {
      final DynamicChoicePanel<STATE_TYPE> choicePanel = choice.getValue();
      if (choiceIds.contains(choicePanel.getId())) {
        choicePanel.afterLoad(state);
      }
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() instanceof JCheckBox) {
      JCheckBox box = (JCheckBox) e.getSource();

      if (checkboxes.values().contains(e.getSource())) {
        // need to reverse lookup the NameValue...
        for (Map.Entry<NameValue, JCheckBox> entry : checkboxes.entrySet()) {
          if (entry.getValue() == box) {
            NameValue nv = entry.getKey();
            if (box.isSelected()) {
              choiceSelected(nv);
            } else {
              choiceDeselected(nv);
            }
          }
        }
      }
    }
  }

  @Override
  public String getSavedChoiceId(STATE_TYPE state) {
    return null;
  }

  @Override
  public void setSavedChoiceId(STATE_TYPE state, String choiceId) {
    // Nothing to get
  }

  public abstract void setSavedChoiceIds(STATE_TYPE state, Collection<String> choiceIds);

  public abstract Collection<String> getSavedChoiceIds(STATE_TYPE state);

  @Override
  protected boolean isEnabledAndSelected(NameValue cct) {
    JCheckBox box = checkboxes.get(cct);
    return enabled && box.isSelected();
  }

  @Override
  public boolean isSelectionEmpty() {
    for (JCheckBox box : checkboxes.values()) {
      if (box.isSelected()) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected void setChoicesEnabled(boolean enabled) {
    this.enabled = enabled;
    for (JCheckBox box : checkboxes.values()) {
      box.setEnabled(enabled);
    }
  }

  @Override
  protected void setSelectedChoiceComponent(NameValue cct) {
    checkboxes.get(cct).setSelected(true);
  }
}
