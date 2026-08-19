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

package com.tle.admin.harvester.standard;

import com.dytech.devlib.PropBagEx;
import com.tle.admin.gui.EditorException;
import com.tle.admin.i18n.Lookup;
import com.tle.common.NameValue;
import com.tle.common.harvester.AbstractTLFHarvesterSettings;
import com.tle.common.i18n.StringLookup;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * The Le@rning Federation (TLF) was a collaborative initiative between Australian state, territory,
 * and New Zealand governments to create high-quality, online educational resources for schools. The
 * TLF produced a vast collection of digital learning objects, all of which were tagged with
 * metadata to make them discoverable. The content created under this initiative is known as TLF
 * content. The LORAX protocol was developed specifically to allow libraries, schools, and other
 * educational platforms to access and integrate this content into their own systems.
 */
public abstract class AbstractTLFPlugin<T extends AbstractTLFHarvesterSettings>
    extends HarvesterPlugin<T> {
  protected static final StringLookup loraxPluginStrings = Lookup.withPrefix("loraxplugin");

  private JTextField userField;
  private JPasswordField passField;

  private JCheckBox liveOnly;
  private JCheckBox harvestLearningObjects;
  private JCheckBox harvestResources;

  public AbstractTLFPlugin(Class<T> clazz) {
    super(clazz);
  }

  protected abstract String getPluginsFieldString();

  @Override
  public void initGUI() {
    userField = new JTextField();
    passField = new JPasswordField();

    liveOnly = new JCheckBox();
    harvestLearningObjects = new JCheckBox();
    harvestResources = new JCheckBox();

    panel.addComponent(new JLabel(strings.text(getPluginsFieldString())));
    panel.addNameAndComponent(strings.text("detailstab.user"), userField);
    panel.addNameAndComponent(strings.text("detailstab.pass"), passField);

    panel.addNameAndComponent(loraxPluginStrings.text("live"), liveOnly);
    panel.addNameAndComponent(loraxPluginStrings.text("harvestlo"), harvestLearningObjects);
    panel.addNameAndComponent(loraxPluginStrings.text("harvestre"), harvestResources);
  }

  @Override
  public void load(T settings) {

    userField.setText(settings.getUser());
    passField.setText(settings.getPass());

    liveOnly.setSelected(settings.getLiveOnly());
    harvestLearningObjects.setSelected(settings.getHarvestLearningObjects());
    harvestResources.setSelected(settings.getHarvestResources());
  }

  @Override
  public void save(T settings) {
    settings.setUser(userField.getText());
    settings.setPass(new String(passField.getPassword()));

    settings.setLiveOnly(liveOnly.isSelected());
    settings.setHarvestLearningObjects(harvestLearningObjects.isSelected());
    settings.setHarvestResources(harvestResources.isSelected());
  }

  @Override
  public void validation() throws EditorException {
    if (userField.getText().isEmpty()) {
      throw new EditorException(loraxPluginStrings.text("userfield"));
    }

    if (!harvestLearningObjects.isSelected() && !harvestResources.isSelected()) {
      throw new EditorException(loraxPluginStrings.text("harvest"));
    }
  }

  @Override
  public void validateSchema(JComboBox<NameValue> collections) {
    PropBagEx definition = getSchemaDefinition(collections);

    if (!hasTLFNode(definition)) {
      JOptionPane.showMessageDialog(panel.getComponent(), loraxPluginStrings.text("schema"));
    }
  }

  private static boolean hasTLFNode(PropBagEx definition) {
    String nodeLoc = "item/itembody/tlfid";
    if (definition.nodeExists(nodeLoc)) {
      Map<String, String> attributesForNode = definition.getAttributesForNode(nodeLoc);

      return attributesForNode != null && isIndexedForAdvancedSearch(attributesForNode);
    }

    return false;
  }

  private static boolean isIndexedForAdvancedSearch(Map<String, String> attributesForNode) {
    return "true".equalsIgnoreCase(attributesForNode.get("field"));
  }
}
