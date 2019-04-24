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
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.admin.schema.SchemaModel;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class UnavailableControlEditor extends AbstractControlEditor<WizardControl> {

  public UnavailableControlEditor(Control control, int wizardType, SchemaModel schema) {
    super(control, wizardType, schema);
    setupGUI();
  }

  @Override
  protected void saveControl() {}

  @Override
  protected void loadControl() {}

  private void setupGUI() {
    JPanel body = new JPanel(new BorderLayout());
    JLabel text = new JLabel("This control is not avaliable now.");
    text.setForeground(Color.RED);
    body.add(text, BorderLayout.WEST);
    addSection(body);
  }
}
