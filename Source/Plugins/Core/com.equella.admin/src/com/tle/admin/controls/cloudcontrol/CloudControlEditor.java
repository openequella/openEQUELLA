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

import com.dytech.edge.admin.wizard.editor.AbstractControlEditor;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.gui.ChangeDetector;
import com.tle.admin.controls.cloudcontrol.configcomponent.CheckboxConfig;
import com.tle.admin.controls.cloudcontrol.configcomponent.DropdownConfig;
import com.tle.admin.controls.cloudcontrol.configcomponent.RadiobuttonConfig;
import com.tle.admin.controls.cloudcontrol.configcomponent.TextFieldConfig;
import com.tle.admin.controls.cloudcontrol.configcomponent.XpathConfig;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.i18n.Lookup;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.cloudproviders.CloudControlConfig;
import com.tle.beans.cloudproviders.CloudControlDefinition;
import com.tle.common.wizard.controls.cloud.CloudControl;
import com.tle.i18n.BundleCache;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CloudControlEditor extends AbstractControlEditor<CloudControl> {
  private final CloudControl control = getWizardControl();
  private final ChangeDetector changeDetector = getChangeDetector();
  private static final int SUB_PANEL_HORIZONTAL_GAP = 20;
  private static final int SUB_PANEL_VERTICAL_GAP = 10;
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
    renderTitle();
    renderBody();
    addSection(mainPanel);
  }

  private JPanel createSubPanel() {
    JPanel configPanel = new JPanel();
    BorderLayout subPanelLayout = new BorderLayout();
    subPanelLayout.setHgap(SUB_PANEL_HORIZONTAL_GAP);
    subPanelLayout.setVgap(SUB_PANEL_VERTICAL_GAP);
    configPanel.setLayout(subPanelLayout);
    return configPanel;
  }

  private void renderTitle() {
    JPanel titlePanel = createSubPanel();
    JLabel titleLabel =
        new JLabel(
            Lookup.lookup.text("cloudcontrol.title") + Lookup.lookup.text("cloudcontrol.required"));
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
                      ? Lookup.lookup.text("cloudcontrol.required")
                      : "")),
          labelPosition);
      switch (configType) {
        case "Textfield":
          cloudControlConfigMap.put(
              cloudControlConfig.id(),
              new TextFieldConfig(cloudControlConfig, configPanel, changeDetector));
          break;
        case "XPath":
          cloudControlConfigMap.put(
              cloudControlConfig.id(),
              new XpathConfig(cloudControlConfig, configPanel, changeDetector, this));
          break;
        case "Dropdown":
          cloudControlConfigMap.put(
              cloudControlConfig.id(),
              new DropdownConfig(cloudControlConfig, configPanel, changeDetector));
          break;
        case "Radio":
          cloudControlConfigMap.put(
              cloudControlConfig.id(),
              new RadiobuttonConfig(cloudControlConfig, configPanel, changeDetector));
          break;
        case "Check":
          cloudControlConfigMap.put(
              cloudControlConfig.id(),
              new CheckboxConfig(cloudControlConfig, configPanel, changeDetector));
          break;
        default:
          configPanel.removeAll();
          cloudControlModel.setErrorMessage(
              Lookup.lookup.text("cloudcontrol.unknownconfig.message", configType));
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
