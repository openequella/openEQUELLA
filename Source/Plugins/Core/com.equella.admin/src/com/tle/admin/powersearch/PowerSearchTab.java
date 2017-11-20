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

package com.tle.admin.powersearch;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.java.plugin.registry.Extension;

import com.dytech.edge.admin.script.options.DefaultScriptOptions;
import com.dytech.edge.admin.wizard.Contexts;
import com.dytech.edge.admin.wizard.ControlDialog;
import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.editor.Editor;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.admin.wizard.walkers.ValidateControls;
import com.dytech.edge.wizard.beans.DefaultWizardPage;
import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.PluginServiceImpl;
import com.tle.admin.controls.EditorFactory;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.admin.gui.EditorException;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;

public class PowerSearchTab extends AbstractPowerSearchTab implements ActionListener, ListSelectionListener
{
	private static final String TITLE = CurrentLocale.get("com.tle.admin.powersearch.powersearchtab.title"); //$NON-NLS-1$

	private PowerSearchList list;
	private JPanel editor;
	private JPanel blankEditor;
	private Editor currentEditor;

	private DefaultScriptOptions scriptOptions;

	@Override
	public void init(Component parent)
	{
		setupTab();
		scriptOptions = new DefaultScriptOptions()
		{
			@Override
			public boolean hasItemStatus()
			{
				return false;
			}

			@Override
			public boolean hasUserIsModerator()
			{
				return false;
			}
		};
	}

	@Override
	public String getTitle()
	{
		return TITLE;
	}

	@Override
	public void validation() throws EditorException
	{
		if( currentEditor != null )
		{
			currentEditor.saveToControl();
		}

		ValidateControls walker = new ValidateControls(clientService);
		walker.execute(list.getRootControl());

		if( walker.errorDetected() )
		{
			list.clearSelection();
			list.setSelectedControl(walker.getInvalidControl());
			throw new EditorException(CurrentLocale.get("com.tle.admin.powersearch.powersearchtab.notvalid")); //$NON-NLS-1$
		}
		else
		{
			if( currentEditor != null )
			{
				currentEditor.loadFromControl();
			}
		}
	}

	@Override
	public void load()
	{
		DefaultWizardPage page = state.getEntity().getWizard();
		if( page == null )
		{
			page = new DefaultWizardPage();
		}

		// set the title of the page so validation doesn't complain
		page.setTitle(LangUtils.createTempLangugageBundle("com.tle.admin.powersearch.powersearcheditor.name")); //$NON-NLS-1$

		list.load(page);
	}

	@Override
	public void save()
	{
		state.getEntity().setWizard(list.getSearchPage());
	}

	protected void setupTab()
	{
		list = new PowerSearchList(Driver.instance().getControlRepository());
		list.addListSelectionListener(this);
		list.setAddControlListener(this);

		JLabel message = new JLabel(CurrentLocale.get("com.tle.admin.powersearch.powersearchtab.noeditor")); //$NON-NLS-1$
		blankEditor = new JPanel();
		blankEditor.add(message);

		editor = new JPanel();
		editor.setLayout(new GridLayout(1, 1));
		editor.add(blankEditor);

		JScrollPane editorScroll = new JScrollPane(editor);

		final int[] rows = {TableLayout.FILL,};
		final int[] cols = {250, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols, 5, 5));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		add(list, new Rectangle(0, 0, 1, 1));
		add(editorScroll, new Rectangle(1, 0, 1, 1));
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if( e.getValueIsAdjusting() )
		{
			return;
		}
		// Try to save the current editor first.
		if( currentEditor != null )
		{
			// If it hasn't been deleted from the tree...
			if( currentEditor.getControl().getParent() != null )
			{
				currentEditor.saveToControl();
				list.controlChanged(currentEditor.getControl());
			}
			currentEditor = null;
		}

		// See if we can construct a new editor for the given control.
		GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
		{
			@Override
			public Object construct() throws Exception
			{
				Control control = list.getSelectedControl();
				if( control != null )
				{
					// Get the class
					PluginServiceImpl pluginService = Driver.instance().getPluginService();
					ControlDefinition definition = control.getDefinition();
					Extension extension = definition.getExtension();
					EditorFactory editorFactory = (EditorFactory) pluginService.getBean(
						extension.getDeclaringPluginDescriptor(), definition.getEditorFactoryClass());

					currentEditor = editorFactory.getEditor(control, WizardHelper.WIZARD_TYPE_POWERSEARCH, schema,
						pluginService);

					currentEditor.setClientService(clientService);
					currentEditor.setPluginService(pluginService);
					currentEditor.setScriptOptions(scriptOptions);
					currentEditor.init();

					// Ask it to load it's defaults.
					currentEditor.loadFromControl();
				}
				return null;
			}

			@Override
			public void exception()
			{
				JOptionPane.showMessageDialog(getComponent(),
					CurrentLocale.get("com.tle.admin.powersearch.powersearchtab.error")); //$NON-NLS-1$
				LOGGER.warn("Could not create editor", getException());

				// We want to do the stuff in finished() anyway
				finished();
			}

			@Override
			protected void afterFinished()
			{
				// nothing
			}

			@Override
			public void finished()
			{
				// Throw out the old editor and replace with the new one.
				editor.removeAll();
				if( currentEditor != null )
				{
					editor.add(currentEditor);
				}
				else
				{
					editor.add(blankEditor);
				}
				editor.updateUI();
			}
		};
		worker.setComponent(parent);
		worker.start();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == list )
		{
			ControlDialog dialog = new ControlDialog(Driver.instance().getControlRepository());
			dialog.addControls(Contexts.CONTEXT_POWERSEARCH);

			ControlDefinition def = dialog.promptForSelection(panel);
			if( def != null )
			{
				list.addControl(def);
			}
		}
	}
}
