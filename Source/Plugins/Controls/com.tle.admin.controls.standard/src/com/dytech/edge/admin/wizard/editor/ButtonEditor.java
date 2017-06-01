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

package com.dytech.edge.admin.wizard.editor;

import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.fife.ui.rtextarea.RTextScrollPane;

import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.control.Button;
import com.dytech.gui.TableLayout;
import com.tle.admin.codeeditor.EquellaSyntaxTextArea;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.i18n.BundleCache;

/**
 * @author Nicholas Read
 */
public class ButtonEditor extends AbstractControlEditor<Button>
{
	private static final long serialVersionUID = 1L;

	private I18nTextField buttonText;
	private EquellaSyntaxTextArea script;

	public ButtonEditor(Control control, int wizardType, SchemaModel schema)
	{
		super(control, wizardType, schema);
		setupGUI();
	}

	@Override
	protected void loadControl()
	{
		Button model = getWizardControl();
		buttonText.load(model.getTitle());
		script.setText(model.getAction());
	}

	@Override
	protected void saveControl()
	{
		Button model = getWizardControl();
		model.setTitle(buttonText.save());
		model.setAction(script.getText());
	}

	private void setupGUI()
	{
		setShowScripting(true);

		JLabel textLabel = new JLabel(CurrentLocale.get("wizard.controls.buttontext")); //$NON-NLS-1$
		JLabel actionLabel = new JLabel(CurrentLocale.get("wizard.controls.buttonaction")); //$NON-NLS-1$

		buttonText = new I18nTextField(BundleCache.getLanguages());
		script = new EquellaSyntaxTextArea(200, 2000);
		RTextScrollPane actionScroller = new RTextScrollPane(script);

		final int height1 = buttonText.getPreferredSize().height;
		final int height2 = actionLabel.getPreferredSize().height;
		final int width1 = textLabel.getPreferredSize().width;

		final int[] rows = {height1, height2, 400,};
		final int[] cols = {width1, TableLayout.FILL,};

		JPanel all = new JPanel(new TableLayout(rows, cols));

		all.add(textLabel, new Rectangle(0, 0, 1, 1));
		all.add(buttonText, new Rectangle(1, 0, 1, 1));

		all.add(actionLabel, new Rectangle(0, 1, 2, 1));
		all.add(actionScroller, new Rectangle(0, 2, 2, 1));

		addSection(all);
	}
}
