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

package com.tle.admin.itemdefinition;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JLabel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.fife.ui.rtextarea.RTextScrollPane;

import com.dytech.edge.common.Constants;
import com.dytech.gui.TableLayout;
import com.tle.admin.codeeditor.EquellaSyntaxTextArea;
import com.tle.beans.entity.itemdef.Wizard;
import com.tle.client.gui.StatusBarContainer;
import com.tle.common.i18n.CurrentLocale;

public class ExpertScriptingTab extends AbstractItemdefTab implements FocusListener, CaretListener
{
	private final StatusBarContainer itemEditor;
	private EquellaSyntaxTextArea redraft;
	private EquellaSyntaxTextArea save;
	private EquellaSyntaxTextArea focus;

	public ExpertScriptingTab(StatusBarContainer itemEditor)
	{
		this.itemEditor = itemEditor;
	}

	@Override
	public void init(Component parent)
	{
		setupGUI();
	}

	@Override
	public void validation()
	{
		// Nothing to validate
	}

	@Override
	public String getTitle()
	{
		return CurrentLocale.get("com.tle.admin.itemdefinition.expertscriptingtab.title"); //$NON-NLS-1$
	}

	@Override
	public void load()
	{
		Wizard wiz = state.getEntity().getWizard();
		if( wiz != null )
		{
			redraft.setText(wiz.getRedraftScript());
			save.setText(wiz.getSaveScript());
		}
	}

	@Override
	public void save()
	{
		Wizard wiz = state.getEntity().getWizard();
		wiz.setRedraftScript(redraft.getText());
		wiz.setSaveScript(save.getText());
	}

	@SuppressWarnings("deprecation")
	protected void setupGUI()
	{
		JLabel redraftLabel = new JLabel(CurrentLocale.get("com.tle.admin.itemdefinition.expertscriptingtab.new")); //$NON-NLS-1$
		JLabel saveLabel = new JLabel(CurrentLocale.get("com.tle.admin.itemdefinition.expertscriptingtab.save")); //$NON-NLS-1$

		redraft = new EquellaSyntaxTextArea(200, 2000);
		redraft.addFocusListener(this);
		redraft.addCaretListener(this);
		save = new EquellaSyntaxTextArea(200, 2000);
		save.addFocusListener(this);
		save.addCaretListener(this);

		RTextScrollPane redraftScroll = new RTextScrollPane(redraft);
		RTextScrollPane saveScroll = new RTextScrollPane(save);

		final int height = redraftLabel.getPreferredSize().height;
		final int[] rows = {height, TableLayout.FILL, height, TableLayout.FILL,};
		final int[] columns = {TableLayout.FILL,};

		setLayout(new TableLayout(rows, columns, 5, 5));

		add(redraftLabel, new Rectangle(0, 0, 1, 1));
		add(redraftScroll, new Rectangle(0, 1, 1, 1));
		add(saveLabel, new Rectangle(0, 2, 1, 1));
		add(saveScroll, new Rectangle(0, 3, 1, 1));
	}

	@Override
	public void focusGained(FocusEvent event)
	{
		focus = (EquellaSyntaxTextArea) event.getComponent();
	}

	@Override
	public void focusLost(FocusEvent event)
	{
		focus = null;
		itemEditor.getStatusBar().setMessage(Constants.BLANK);
	}

	@Override
	public void caretUpdate(CaretEvent event)
	{
		if( focus != null )
		{
			itemEditor
				.getStatusBar()
				.setMessage(
					CurrentLocale
						.get(
							"com.tle.admin.scripting.editor.lineandcolumn", focus.getCaretLineNumber() + 1, focus.getCaretOffsetFromLineStart())); //$NON-NLS-1$
		}
	}
}
