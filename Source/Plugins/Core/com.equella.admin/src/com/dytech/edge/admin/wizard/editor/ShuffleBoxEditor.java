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

package com.dytech.edge.admin.wizard.editor;

import com.dytech.edge.admin.wizard.DualShuffleList;
import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.control.ShuffleBox;
import com.dytech.gui.TableLayout;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.MultiTargetChooser;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.i18n.BundleCache;
import java.awt.Rectangle;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ShuffleBoxEditor extends AbstractPowerSearchControlEditor<ShuffleBox> {
  private static final long serialVersionUID = 1L;
  private I18nTextField title;
  private I18nTextField description;
  private JCheckBox mandatory;
  private JCheckBox reload;

  private MultiTargetChooser picker;
  private DualShuffleList choices;

  /** Constructs a new ShuffleBoxEditor. */
  public ShuffleBoxEditor(Control control, int wizardType, SchemaModel schema) {
    super(control, wizardType, schema);
  }

  @Override
  protected void loadControl() {
    ShuffleBox control = getWizardControl();

    title.load(control.getTitle());
    description.load(control.getDescription());
    mandatory.setSelected(control.isMandatory());
    reload.setSelected(control.isReload());
    choices.setItems(control.getItems());

    WizardHelper.loadSchemaChooser(picker, control);

    super.loadControl();
  }

  @Override
  protected void saveControl() {
    ShuffleBox control = getWizardControl();

    control.setTitle(title.save());
    control.setDescription(description.save());
    control.setMandatory(mandatory.isSelected());
    control.setReload(reload.isSelected());

    control.getItems().clear();
    control.getItems().addAll(choices.getItems());

    WizardHelper.saveSchemaChooser(picker, control);

    super.saveControl();
  }

  @Override
  protected void setupGUI() {
    setShowScripting(true);

    picker = WizardHelper.createMultiTargetChooser(this);
    choices =
        new DualShuffleList(
            CurrentLocale.get("com.dytech.edge.admin.wizard.editor.shuffleboxeditor.name"),
            CurrentLocale //$NON-NLS-1$
                .get("com.dytech.edge.admin.wizard.editor.shuffleboxeditor.value")); // $NON-NLS-1$

    addSection(createDetails());
    addSection(WizardHelper.createMetaData(picker));
    addSection(
        WizardHelper.createItems(
            choices,
            CurrentLocale.get(
                "com.dytech.edge.admin.wizard.editor.shuffleboxeditor.add"))); //$NON-NLS-1$

    super.setupGUI();
  }

  private JComponent createDetails() {
    JLabel titleLabel = new JLabel(CurrentLocale.get("wizard.controls.title")); // $NON-NLS-1$
    JLabel descriptionLabel =
        new JLabel(CurrentLocale.get("wizard.controls.description")); // $NON-NLS-1$

    title = new I18nTextField(BundleCache.getLanguages());
    description = new I18nTextField(BundleCache.getLanguages());
    mandatory = new JCheckBox(CurrentLocale.get("wizard.controls.mandatory")); // $NON-NLS-1$
    reload = new JCheckBox(CurrentLocale.get("wizard.controls.reload")); // $NON-NLS-1$

    final int height1 = title.getPreferredSize().height;
    final int width1 = descriptionLabel.getPreferredSize().width;

    final int[] rows = {height1, height1, height1, height1};
    final int[] cols = {width1, TableLayout.FILL, TableLayout.DOUBLE_FILL};

    JPanel all = new JPanel(new TableLayout(rows, cols, 5, 5));

    all.add(titleLabel, new Rectangle(0, 0, 1, 1));
    all.add(title, new Rectangle(1, 0, 2, 1));

    all.add(descriptionLabel, new Rectangle(0, 1, 1, 1));
    all.add(description, new Rectangle(1, 1, 2, 1));

    all.add(mandatory, new Rectangle(0, 2, 3, 1));

    all.add(reload, new Rectangle(0, 3, 3, 1));

    return all;
  }
}
