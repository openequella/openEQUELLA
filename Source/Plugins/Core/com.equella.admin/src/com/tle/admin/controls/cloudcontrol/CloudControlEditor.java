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

package com.tle.admin.controls.cloudcontrol;

import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.editor.AbstractControlEditor;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.gui.ChangeDetector;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.MultiTargetChooser;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.cloudproviders.CloudControlConfig;
import com.tle.beans.cloudproviders.CloudControlDefinition;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.wizard.controls.cloud.CloudControl;
import com.tle.i18n.BundleCache;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class CloudControlEditor extends AbstractControlEditor<CloudControl> {
  private final CloudControl control = getWizardControl();
  private final ChangeDetector changeDetector = getChangeDetector();
  private List<CloudControlConfig> cloudControlConfigs;
  private Map<String, CloudControlConfigControl> cloudControlConfigMap = new HashMap<>();
  private JPanel mainPanel;
  private GridBagConstraints gridBagConstraints;
  private I18nTextField title;
  private Control cloudControlModel;

  public CloudControlEditor(
      CloudControlDefinition definition, Control control, int wizardType, SchemaModel schema) {
    super(control, wizardType, schema);
    this.cloudControlConfigs = definition.configDefinition();
    this.cloudControlModel = control;
    setupGUI();
  }

  @Override
  protected void saveControl() {
    control.setTitle(title.save());
    saveOrLoadConfigs(true);
  }

  @Override
  protected void loadControl() {
    title.load(control.getTitle());
    saveOrLoadConfigs(false);
  }

  @Override
  public boolean hasDetectedChanges() {
    saveControl();
    return super.hasDetectedChanges();
  }

  private void setupGUI() {
    mainPanel = new JPanel(new GridBagLayout());
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.weightx = 1;
    // title and each config is accommodated in a sub panel
    renderTitle();
    renderBody();
    addSection(mainPanel);
  }

  private JPanel createSubPanel() {
    JPanel configPanel = new JPanel();
    BorderLayout subPanelLayout = new BorderLayout();
    subPanelLayout.setHgap(20);
    subPanelLayout.setVgap(10);
    configPanel.setLayout(subPanelLayout);
    return configPanel;
  }

  private void renderTitle() {
    JPanel titlePanel = createSubPanel();
    JLabel titleLabel =
        new JLabel(
            CurrentLocale.get("wizard.controls.title")
                + CurrentLocale.get("wizard.controls.isrequired"));
    title = new I18nTextField(BundleCache.getLanguages());
    changeDetector.watch(title);
    titlePanel.add(titleLabel, BorderLayout.WEST);
    titlePanel.add(title, BorderLayout.CENTER);
    mainPanel.add(titlePanel, gridBagConstraints);
  }

  private void renderBody() {
    int gridY = 1;
    for (CloudControlConfig cloudControlConfig : cloudControlConfigs) {
      gridBagConstraints.gridy = gridY++;
      JPanel configPanel = createSubPanel();
      String description = Optional.ofNullable(cloudControlConfig.description()).orElse("");
      configPanel.setToolTipText(description);
      String configType = cloudControlConfig.configType().toString();
      String labelPosition = (configType.equals("XPath") ? BorderLayout.NORTH : BorderLayout.WEST);
      configPanel.add(
          new JLabel(
              cloudControlConfig.name()
                  + (cloudControlConfig.isConfigMandatory()
                      ? CurrentLocale.get("wizard.controls.isrequired")
                      : "")),
          labelPosition);
      switch (configType) {
        case "Textfield":
          JTextField textField = new JTextField();
          changeDetector.watch(textField);
          configPanel.add(textField, BorderLayout.CENTER);
          cloudControlConfigMap.put(
              cloudControlConfig.id(),
              new CloudControlConfigControl() {
                @Override
                public void saveConfig(CloudControl control) {
                  control.getAttributes().put(cloudControlConfig.id(), textField.getText());
                }

                @Override
                public void loadConfig(CloudControl control) {
                  if (control.getAttributes().containsKey(cloudControlConfig.id())) {
                    textField.setText(
                        control.getAttributes().get(cloudControlConfig.id()).toString());
                  }
                }
              });
          break;
        case "XPath":
          MultiTargetChooser picker = WizardHelper.createMultiTargetChooser(this);
          changeDetector.watch(picker);
          configPanel.add(picker, BorderLayout.CENTER);
          cloudControlConfigMap.put(
              cloudControlConfig.id(),
              new CloudControlConfigControl() {
                @Override
                public void saveConfig(CloudControl control) {
                  control.getAttributes().put(cloudControlConfig.id(), picker.getTargets());
                }

                @Override
                @SuppressWarnings("unchecked")
                public void loadConfig(CloudControl control) {
                  if (control.getAttributes().containsKey(cloudControlConfig.id())) {
                    picker.setTargets(
                        (List<String>) control.getAttributes().get(cloudControlConfig.id()));
                  }
                }
              });
          break;
        case "Dropdown":
          JComboBox<String> comboBox = new JComboBox<>();
          changeDetector.watch(comboBox);
          cloudControlConfig
              .options()
              .forEach(cloudConfigOption -> comboBox.addItem(cloudConfigOption.name()));
          configPanel.add(comboBox);
          cloudControlConfigMap.put(
              cloudControlConfig.id(),
              new CloudControlConfigControl() {
                @Override
                public void saveConfig(CloudControl control) {
                  control.getAttributes().put(cloudControlConfig.id(), comboBox.getSelectedIndex());
                }

                @Override
                public void loadConfig(CloudControl control) {
                  if (control.getAttributes().containsKey(cloudControlConfig.id())) {
                    comboBox.setSelectedIndex(
                        (int) control.getAttributes().get(cloudControlConfig.id()));
                  }
                }
              });
          break;
        case "Radio":
          ArrayList<JRadioButton> radioButtons = new ArrayList<>();
          ButtonGroup radioButtonGroup = new ButtonGroup();
          JPanel radioButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
          cloudControlConfig
              .options()
              .forEach(
                  cloudConfigOption -> {
                    JRadioButton radioButton = new JRadioButton(cloudConfigOption.name());
                    radioButtonGroup.add(radioButton);
                    radioButtonPanel.add(radioButton);
                    radioButtons.add(radioButton);
                  });
          changeDetector.watch(radioButtonGroup);
          configPanel.add(radioButtonPanel, BorderLayout.CENTER);
          cloudControlConfigMap.put(
              cloudControlConfig.id(),
              new CloudControlConfigControl() {
                @Override
                public void saveConfig(CloudControl control) {
                  for (int i = 0; i < radioButtons.size(); i++) {
                    if (radioButtons.get(i).isSelected()) {
                      control.getAttributes().put(cloudControlConfig.id(), i);
                      return;
                    }
                  }
                }

                @Override
                public void loadConfig(CloudControl control) {
                  if (control.getAttributes().containsKey(cloudControlConfig.id())) {
                    radioButtons
                        .get((int) control.getAttributes().get(cloudControlConfig.id()))
                        .setSelected(true);
                  }
                }
              });

          break;
        case "Check":
          ArrayList<JCheckBox> checkBoxes = new ArrayList<>();
          JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
          cloudControlConfig
              .options()
              .forEach(
                  cloudConfigOption -> {
                    JCheckBox checkBox = new JCheckBox(cloudConfigOption.name());
                    changeDetector.watch(checkBox);
                    checkBoxPanel.add(checkBox);
                    checkBoxes.add(checkBox);
                  });
          configPanel.add(checkBoxPanel, BorderLayout.CENTER);
          cloudControlConfigMap.put(
              cloudControlConfig.id(),
              new CloudControlConfigControl() {
                @Override
                public void saveConfig(CloudControl control) {
                  boolean[] checkBoxSelections = new boolean[checkBoxes.size()];
                  for (int i = 0; i < checkBoxes.size(); i++) {
                    checkBoxSelections[i] = checkBoxes.get(i).isSelected();
                  }
                  control.getAttributes().put(cloudControlConfig.id(), checkBoxSelections);
                }

                @Override
                public void loadConfig(CloudControl control) {
                  if (control.getAttributes().containsKey(cloudControlConfig.id())) {
                    boolean[] checkBoxSelections =
                        (boolean[]) control.getAttributes().get(cloudControlConfig.id());
                    for (int i = 0; i < checkBoxes.size(); i++) {
                      checkBoxes.get(i).setSelected(checkBoxSelections[i]);
                    }
                  }
                }
              });

          break;
        default:
          try {
            configPanel.removeAll();
            cloudControlModel.setErrorMessage(
                CurrentLocale.get("wizard.cloudcontrol.unknownconfig.message", configType));
            throw new EditorException(
                CurrentLocale.get("wizard.cloudcontrol.unknownconfig.message"));
          } catch (EditorException e) {
            e.printStackTrace();
          }
          break;
      }
      mainPanel.add(configPanel, gridBagConstraints);
    }
  }

  private void saveOrLoadConfigs(boolean isSaving) {
    cloudControlConfigMap
        .values()
        .forEach(
            config -> {
              if (isSaving) {
                config.saveConfig(control);
              } else {
                config.loadConfig(control);
              }
            });
  }
}
