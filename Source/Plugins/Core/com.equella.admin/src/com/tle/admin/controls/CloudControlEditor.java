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

package com.tle.admin.controls;

import com.dytech.edge.admin.wizard.editor.AbstractControlEditor;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.cloudproviders.CloudControlDefinition;
import com.tle.common.i18n.CurrentLocale;
import com.tle.i18n.BundleCache;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CloudControlEditor extends AbstractControlEditor<CustomControl> {

  private I18nTextField title;
  private final CloudControlDefinition definition;

  public CloudControlEditor(
      CloudControlDefinition definition, Control control, int wizardType, SchemaModel schema) {
    super(control, wizardType, schema);
    this.definition = definition;
    setupGUI();
  }

  @Override
  protected void saveControl() {
    CustomControl control = getWizardControl();

    control.setTitle(title.save());
  }

  @Override
  protected void loadControl() {
    CustomControl control = getWizardControl();
    title.load(control.getTitle());
  }

  protected void setupGUI() {
    addSection(defaultFields());
  }

  private JComponent defaultFields() {
    JLabel titleLabel = new JLabel(CurrentLocale.get("wizard.controls.title")); // $NON-NLS-1$
    title = new I18nTextField(BundleCache.getLanguages());
    JPanel all = new JPanel();
    all.add(titleLabel);
    all.add(title);
    definition
        .configDefinition()
        .foreach(
            d ->
                all.add(
                    new JLabel(
                        d.controlType().toString() + " " + d.name() + " " + d.description())));
    return all;
  }
}
