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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.control.Html;
import com.tle.admin.gui.i18n.I18nTextArea;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.i18n.BundleCache;

public class RawHtmlEditor extends AbstractControlEditor<Html>
{
	private static final long serialVersionUID = 1L;

	private static final NameValue[] ELEMENTS = new NameValue[]{new NameValue(
		CurrentLocale.get("com.dytech.edge.admin.wizard.editor.rawhtmleditor.horiz"), "<hr>")}; //$NON-NLS-1$ //$NON-NLS-2$

	private JPanel prePanel;
	private JPanel userPanel;
	private JRadioButton preRadio;
	private JRadioButton userRadio;
	private JLabel userHelp;

	protected JComboBox preList;
	protected I18nTextArea userText;

	public RawHtmlEditor(Control control, int wizardType, SchemaModel schema)
	{
		super(control, wizardType, schema);
		setupGUI();
	}

	@Override
	protected void loadControl()
	{
		userRadio.doClick();
		userText.load(getWizardControl().getDescription());
	}

	@Override
	protected void saveControl()
	{
		LanguageBundle html;
		if( preRadio.isSelected() )
		{
			html = LangUtils.createTextTempLangugageBundle(((NameValue) preList.getSelectedItem()).getValue());
		}
		else
		{
			html = userText.save();
		}
		getWizardControl().setDescription(html);
	}

	private void setupGUI()
	{
		setShowScripting(true);

		createTop();
		createBottom();

		ButtonGroup group = new ButtonGroup();
		group.add(preRadio);
		group.add(userRadio);

		addSection(prePanel);
		addSection(userPanel);
	}

	protected void createTop()
	{
		preRadio = new JRadioButton(CurrentLocale.get("com.dytech.edge.admin.wizard.editor.rawhtmleditor.predefined")); //$NON-NLS-1$
		preRadio.addActionListener(new RawHtmlEditor.RadioHandler(false));
		preRadio.setSelected(true);

		preList = new JComboBox(ELEMENTS);

		prePanel = new JPanel(new BorderLayout(5, 5));
		prePanel.add(preRadio, BorderLayout.NORTH);
		prePanel.add(preList, BorderLayout.CENTER);
	}

	protected void createBottom()
	{
		userHelp = new JLabel(CurrentLocale.get(
			"com.dytech.edge.admin.wizard.editor.rawhtmleditor.select", "\"{/xpath/to/my/data}\"")); //$NON-NLS-1$ //$NON-NLS-2$
		userRadio = new JRadioButton(CurrentLocale.get("com.dytech.edge.admin.wizard.editor.rawhtmleditor.userdefined")); //$NON-NLS-1$
		userText = new I18nTextArea(BundleCache.getLanguages());

		Dimension d = new Dimension(0, 300);
		userText.setMinimumSize(d);
		userText.setPreferredSize(d);

		userText.setEnabled(false);
		userText.setEnabled(false);
		userHelp.setEnabled(false);
		userRadio.addActionListener(new RawHtmlEditor.RadioHandler(true));

		userPanel = new JPanel(new BorderLayout(5, 5));

		userPanel.add(userRadio, BorderLayout.NORTH);
		userPanel.add(userText, BorderLayout.CENTER);
		userPanel.add(userHelp, BorderLayout.SOUTH);
	}

	protected class RadioHandler implements ActionListener
	{
		private final boolean toUser;

		public RadioHandler(boolean toUser)
		{
			this.toUser = toUser;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			preList.setEnabled(!toUser);

			userText.setEnabled(toUser);
			userText.setEnabled(toUser);
			userHelp.setEnabled(toUser);
		}
	}
}
