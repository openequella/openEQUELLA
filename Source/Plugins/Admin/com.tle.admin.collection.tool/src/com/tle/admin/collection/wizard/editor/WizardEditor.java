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

package com.tle.admin.collection.wizard.editor;

import java.awt.Rectangle;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.dytech.edge.admin.wizard.editor.Editor;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.gui.TableLayout;
import com.tle.admin.itemdefinition.PagedWizardModel;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.itemdef.Wizard;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author aholland
 */
public class WizardEditor extends Editor
{
	private static final long serialVersionUID = 8609815614841392333L;

	private JCheckBox allowNonSequential;
	private JCheckBox showPageTitlesNextPrev;
	private JTextField additionalCssClass;
	private JTextArea accessibilityHelp;
	private JLabel accessibilityHelpTitle;

	public WizardEditor(Control control, int wizardType, SchemaModel schema)
	{
		super(control, wizardType, schema);
		setupGUI();
	}

	@SuppressWarnings("nls")
	private void setupGUI()
	{
		JPanel all = new JPanel();

		allowNonSequential = new JCheckBox(
			CurrentLocale.get("com.tle.admin.collection.tool.wizard.editor.allowNonSequential.label"));

		showPageTitlesNextPrev = new JCheckBox(
			CurrentLocale.get("com.tle.admin.collection.tool.wizard.editor.showPageTitlesNextPrev.label"));

		JLabel cssLabel = new JLabel(
			CurrentLocale.get("com.tle.admin.collection.tool.wizard.editor.additionalCssClass.label"));
		additionalCssClass = new JTextField();
		JLabel cssHelp = new JLabel(
			CurrentLocale.get("com.tle.admin.collection.tool.wizard.editor.additionalCssClass.help"));
		cssHelp.setFont(all.getFont());
		accessibilityHelpTitle = new JLabel(
			CurrentLocale.get("com.tle.admin.collection.tool.wizard.editor.accesshelp.title"));
		accessibilityHelp = new JTextArea(
			CurrentLocale.get("com.tle.admin.collection.tool.wizard.editor.accesshelp.text"));
		accessibilityHelp.setEditable(false);
		accessibilityHelp.setBackground(all.getBackground());
		accessibilityHelp.setFont(all.getFont());

		JSeparator separator1 = new JSeparator();
		JSeparator separator2 = new JSeparator();
		int sh = separator1.getPreferredSize().height;

		final int columns[] = {cssLabel.getPreferredSize().width, TableLayout.FILL};
		final int rows[] = {allowNonSequential.getPreferredSize().height,
				showPageTitlesNextPrev.getPreferredSize().height, sh, additionalCssClass.getPreferredSize().height,
				cssHelp.getPreferredSize().height, sh, accessibilityHelpTitle.getPreferredSize().height,
				accessibilityHelp.getPreferredSize().height};

		all.setLayout(new TableLayout(rows, columns, 5, 5));
		all.add(allowNonSequential, new Rectangle(0, 0, 2, 1));
		all.add(showPageTitlesNextPrev, new Rectangle(0, 1, 2, 1));
		all.add(separator1, new Rectangle(0, 2, 2, 1));
		all.add(cssLabel, new Rectangle(0, 3, 1, 1));
		all.add(additionalCssClass, new Rectangle(1, 3, 1, 1));
		all.add(cssHelp, new Rectangle(0, 4, 2, 1));
		all.add(separator2, new Rectangle(0, 5, 2, 1));
		all.add(accessibilityHelpTitle, new Rectangle(0, 6, 2, 1));
		all.add(accessibilityHelp, new Rectangle(0, 7, 2, 1));

		addSection(all);
	}

	@Override
	protected void loadControl()
	{
		PagedWizardModel wizModel = (PagedWizardModel) getControl();
		Wizard wizard = wizModel.getWizard();
		allowNonSequential.setSelected(wizard.isAllowNonSequentialNavigation());
		showPageTitlesNextPrev.setSelected(wizard.isShowPageTitlesNextPrev());
		additionalCssClass.setText(wizard.getAdditionalCssClass());
	}

	@Override
	protected void saveControl()
	{
		PagedWizardModel wizModel = (PagedWizardModel) getControl();
		Wizard wizard = wizModel.getWizard();
		wizard.setAllowNonSequentialNavigation(allowNonSequential.isSelected());
		wizard.setShowPageTitlesNextPrev(showPageTitlesNextPrev.isSelected());
		wizard.setAdditionalCssClass(additionalCssClass.getText());
	}
}
