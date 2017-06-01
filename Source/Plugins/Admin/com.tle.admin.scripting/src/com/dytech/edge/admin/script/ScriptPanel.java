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

package com.dytech.edge.admin.script;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.dytech.edge.common.Constants;
import com.dytech.gui.Changeable;
import com.dytech.gui.TableLayout;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.tle.admin.Driver;
import com.tle.admin.codeeditor.EquellaSyntaxTextArea;
import com.tle.client.gui.StatusBarContainer;
import com.tle.common.Check;
import com.tle.common.applet.client.DialogUtils;
import com.tle.common.applet.client.DialogUtils.DialogResult;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 * @created 1 April 2003
 */
public class ScriptPanel extends JTabbedPane
	implements
		ActionListener,
		ListSelectionListener,
		ChangeListener,
		Changeable,
		FocusListener,
		CaretListener
{
	private static final long serialVersionUID = 1L;

	private static final Log LOGGER = LogFactory.getLog(ScriptPanel.class);
	private static final int BASIC_TAB = 0;
	private static final int ADVANCED_TAB = 1;

	private static final int CODE_HEIGHT_LINES = 500;
	private static final int CODE_WIDTH_COLUMNS = 300;

	private int currentTab = BASIC_TAB;

	private final StatusBarContainer statusHolder;

	private EquellaSyntaxTextArea advanced;

	private ScriptView view;
	private ScriptModel model;

	private JButton deleteButton;
	private JButton importButton;
	private JButton exportButton;

	private String originalScript;

	public ScriptPanel(ScriptModel model, StatusBarContainer statusHolder)
	{
		this.statusHolder = statusHolder;
		setup(model);
	}

	public void importScript(String script)
	{
		model.clearScript();
		currentTab = BASIC_TAB;
		setSelectedIndex(BASIC_TAB);

		if( script != null && script.length() > 0 )
		{
			originalScript = script.trim();
			try
			{
				model.importScript(new StringReader(script));
			}
			catch( InvalidScriptException ex )
			{
				currentTab = ADVANCED_TAB;
				setSelectedIndex(ADVANCED_TAB);
				advanced.setText(script);
			}
		}
		else
		{
			originalScript = ""; //$NON-NLS-1$
		}

		clearChanges();
	}

	public String getScript()
	{
		if( getSelectedIndex() == BASIC_TAB )
		{
			return model.toScript();
		}
		else
		{
			return advanced.getText();
		}
	}

	@Override
	public boolean hasDetectedChanges()
	{
		if( getSelectedIndex() == BASIC_TAB )
		{
			String newScript = model.toScript();
			if( newScript == null )
			{
				newScript = ""; //$NON-NLS-1$
			}
			newScript = newScript.trim();
			return !newScript.equals(originalScript);
		}
		else
		{
			return !advanced.getText().trim().equals(originalScript);
		}
	}

	@Override
	public void clearChanges()
	{
		originalScript = Check.nullToEmpty(getScript()).trim();
	}

	private void setup(ScriptModel model)
	{
		this.model = model;

		addTab(CurrentLocale.get("com.dytech.edge.admin.script.scriptpanel.basic"), createBasic()); //$NON-NLS-1$
		addTab(CurrentLocale.get("com.dytech.edge.admin.script.scriptpanel.advanced"), //$NON-NLS-1$
			createAdvanced());

		addChangeListener(this);
	}

	private JPanel createAdvanced()
	{
		JLabel titleLabel = new JLabel(CurrentLocale.get("com.dytech.edge.admin.script.scriptpanel.javascript")); //$NON-NLS-1$

		advanced = new EquellaSyntaxTextArea(CODE_HEIGHT_LINES, CODE_WIDTH_COLUMNS);
		advanced.addCaretListener(this);
		advanced.addFocusListener(this);
		RTextScrollPane advancedScroll = new RTextScrollPane(advanced);

		final int height = titleLabel.getPreferredSize().height;
		final int[] rows = {height, TableLayout.FILL};
		final int[] cols = {TableLayout.FILL};

		JPanel all = new JPanel(new TableLayout(rows, cols, 5, 5));
		all.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		all.add(titleLabel, new Rectangle(0, 0, 1, 1));
		all.add(advancedScroll, new Rectangle(0, 1, 1, 1));

		return all;
	}

	private JPanel createBasic()
	{
		JLabel statementLabel = new JLabel(CurrentLocale.get("com.dytech.edge.admin.script.scriptpanel.statement")); //$NON-NLS-1$
		JLabel scriptLabel = new JLabel(CurrentLocale.get("com.dytech.edge.admin.script.scriptpanel.script")); //$NON-NLS-1$

		deleteButton = new JButton(CurrentLocale.get("com.dytech.edge.admin.script.scriptpanel.deleteline")); //$NON-NLS-1$
		importButton = new JButton(CurrentLocale.get("com.dytech.edge.admin.script.scriptpanel.importscript")); //$NON-NLS-1$
		exportButton = new JButton(CurrentLocale.get("com.dytech.edge.admin.script.scriptpanel.exportscript")); //$NON-NLS-1$

		importButton.addActionListener(this);
		deleteButton.addActionListener(this);
		exportButton.addActionListener(this);

		deleteButton.setEnabled(false);

		view = new ScriptView(model);
		view.addListSelectionListener(this);
		JScrollPane viewScroll = new JScrollPane(view);

		JComponent statement = model.getStatementEditor();

		final int height1 = statementLabel.getPreferredSize().height;
		final int height2 = statement.getPreferredSize().height;
		final int height3 = exportButton.getPreferredSize().height;
		final int width1 = Math.max(exportButton.getPreferredSize().width, importButton.getPreferredSize().width);

		final int[] rows = {height1, height2, height1, TableLayout.FILL, height3};
		final int[] cols = {width1, width1, width1, TableLayout.FILL};

		JPanel all = new JPanel(new TableLayout(rows, cols, 5, 5));
		all.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		all.add(statementLabel, new Rectangle(0, 0, 4, 1));
		all.add(statement, new Rectangle(0, 1, 4, 1));
		all.add(scriptLabel, new Rectangle(0, 2, 4, 1));
		all.add(viewScroll, new Rectangle(0, 3, 4, 1));
		all.add(deleteButton, new Rectangle(0, 4, 1, 1));
		all.add(importButton, new Rectangle(1, 4, 1, 1));
		all.add(exportButton, new Rectangle(2, 4, 1, 1));

		return all;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event
	 * .ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		Row r = (Row) view.getSelectedValue();
		deleteButton.setEnabled(model.allowRemoval(r));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	@Override
	public void stateChanged(ChangeEvent e)
	{
		int index = getSelectedIndex();
		if( index != currentTab )
		{
			if( index == BASIC_TAB )
			{
				try
				{
					String script = advanced.getText();
					if( script.trim().length() > 0 )
					{
						model.importScript(new StringReader(script));
					}
					else
					{
						model.clearScript();
					}
					currentTab = BASIC_TAB;
				}
				catch( InvalidScriptException ex )
				{
					Driver.displayInformation(this,
						CurrentLocale.get("com.dytech.edge.admin.script.scriptpanel.customised")); //$NON-NLS-1$
					LOGGER.warn("Error switching from advanced to basic scripting modes", ex); //$NON-NLS-1$
					setSelectedIndex(ADVANCED_TAB);
				}
			}
			else
			{
				advanced.setText(model.toScript());
				currentTab = ADVANCED_TAB;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == deleteButton )
		{
			if( !view.isSelectionEmpty() )
			{
				Row r = (Row) view.getSelectedValue();
				model.remove(r);
				model.rowSelected(null, -1);
				view.updateUI();
			}
		}
		else if( e.getSource() == exportButton )
		{
			final DialogResult result = DialogUtils.saveDialog(this, null);
			if( result.isOkayed() )
			{
				boolean write = true;
				File f = result.getFile();

				if( f.exists() )
				{
					int confirm = JOptionPane.showConfirmDialog(this,
						CurrentLocale.get("com.dytech.edge.admin.script.scriptpanel.overwrite"), //$NON-NLS-1$
						CurrentLocale.get("com.dytech.edge.admin.script.scriptpanel.sure"), //$NON-NLS-1$
						JOptionPane.YES_NO_OPTION);

					if( confirm == JOptionPane.NO_OPTION )
					{
						write = false;
					}
				}

				if( write )
				{
					try
					{
						Files.asCharSink(f, Charsets.UTF_8).write(model.toScript());
					}
					catch( IOException ex )
					{
						Driver.displayError(this, "script/exporting", ex); //$NON-NLS-1$
						LOGGER.error("Error exporting script", ex); //$NON-NLS-1$
					}
				}
			}
		}
		else if( e.getSource() == importButton )
		{
			final int confirm = JOptionPane.showConfirmDialog(this,
				CurrentLocale.get("com.dytech.edge.admin.script.scriptpanel.replace"), //$NON-NLS-1$
				CurrentLocale.get("com.dytech.edge.admin.script.scriptpanel.sure"), //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION);

			if( confirm == JOptionPane.YES_OPTION )
			{
				final DialogResult result = DialogUtils.openDialog(this, null);
				if( result.isOkayed() )
				{
					File f = result.getFile();
					try( Reader reader = new FileReader(f) )
					{
						;
						model.importScript(reader);
					}
					catch( IOException ex )
					{
						Driver.displayError(this, "script/importing", ex); //$NON-NLS-1$
						LOGGER.error("Error importing script", ex); //$NON-NLS-1$
					}
					catch( InvalidScriptException ex )
					{
						Driver.displayError(this, "script/invalid", ex); //$NON-NLS-1$
						LOGGER.error("Error importing script", ex); //$NON-NLS-1$
					}
				}
			}
		}
	}

	@Override
	public void focusGained(FocusEvent event)
	{
		// nothing
	}

	@Override
	public void focusLost(FocusEvent event)
	{
		statusHolder.getStatusBar().setMessage(Constants.BLANK);
	}

	@Override
	public void caretUpdate(CaretEvent event)
	{
		statusHolder
			.getStatusBar()
			.setMessage(
				CurrentLocale
					.get(
						"com.tle.admin.scripting.editor.lineandcolumn", advanced.getCaretLineNumber() + 1, advanced.getCaretOffsetFromLineStart())); //$NON-NLS-1$
	}
}
