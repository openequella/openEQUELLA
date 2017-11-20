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
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.java.plugin.registry.Extension;

import com.dytech.edge.admin.wizard.Contexts;
import com.dytech.edge.admin.wizard.ControlDialog;
import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.WizardTree;
import com.dytech.edge.admin.wizard.editor.Editor;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.admin.wizard.walkers.ValidateControls;
import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.controls.EditorFactory;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.EditorInterface;
import com.tle.admin.gui.common.JChangeDetectorPanel;
import com.tle.beans.entity.itemdef.Wizard;
import com.tle.common.i18n.CurrentLocale;

public class WizardTab extends AbstractItemdefTab implements ActionListener, TreeSelectionListener, ComponentListener
{
	private static final int MIN_TREE_WIDTH = 250;
	private static final int MAX_TREE_WIDTH = 400;

	private final WizardTree tree;

	private JPanel editor;
	private JPanel blankEditor;
	private Editor currentEditor;
	private int wizardType;
	private final EditorInterface entityEditor;

	private TableLayout guiLayout;

	public WizardTab(WizardTree tree, EditorInterface entityEditor)
	{
		this.tree = tree;
		this.entityEditor = entityEditor;
		panel = new JChangeDetectorPanel()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean hasDetectedChanges()
			{
				return super.hasDetectedChanges() || (currentEditor != null && currentEditor.hasDetectedChanges());
			}

			@Override
			public void clearChanges()
			{
				super.clearChanges();
				if( currentEditor != null )
				{
					currentEditor.clearChanges();
				}
			}
		};
		panel.addComponentListener(this);
	}

	@Override
	public void init(Component parent)
	{
		wizardType = WizardHelper.WIZARD_TYPE_CONTRIBUTION;

		setupTab();
	}

	@Override
	public void validation() throws EditorException
	{
		if( currentEditor != null )
		{
			currentEditor.saveToControl();
		}

		ValidateControls walker = new ValidateControls(clientService);
		walker.execute(tree.getRoot());

		if( walker.errorDetected() )
		{
			tree.clearSelection();
			tree.setSelectedControl(walker.getInvalidControl());
			throw new EditorException(CurrentLocale.get("com.tle.admin.itemdefinition.wizardtab.error")); //$NON-NLS-1$
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
	public String getTitle()
	{
		return CurrentLocale.get("com.tle.admin.itemdefinition.wizardtab.title"); //$NON-NLS-1$
	}

	@Override
	public void save()
	{
		if( currentEditor != null )
		{
			currentEditor.saveToControl();
		}
		state.getEntity().setWizard((Wizard) tree.getRoot().save());
	}

	@Override
	public void load()
	{
		Wizard wizard = state.getEntity().getWizard();
		if( wizard == null )
		{
			wizard = new Wizard();
		}
		tree.loadItem(wizard);
	}

	private void setupTab()
	{
		tree.addTreeSelectionListener(this);
		tree.setControlListener(this);

		JLabel message = new JLabel(CurrentLocale.get("com.tle.admin.itemdefinition.wizardtab.noeditor")); //$NON-NLS-1$
		blankEditor = new JPanel();
		blankEditor.add(message);

		editor = new JPanel();
		editor.setLayout(new GridLayout(1, 1));
		editor.add(blankEditor);

		JScrollPane editorScroll = new JScrollPane(editor);
		editorScroll.getVerticalScrollBar().setUnitIncrement(15);
		editorScroll.getVerticalScrollBar().setBlockIncrement(20);

		final int[] rows = {TableLayout.FILL,};
		final int[] cols = {MIN_TREE_WIDTH, TableLayout.FILL};

		guiLayout = new TableLayout(rows, cols, 5, 5);
		setLayout(guiLayout);

		add(tree, new Rectangle(0, 0, 1, 1));
		add(editorScroll, new Rectangle(1, 0, 1, 1));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event
	 * .TreeSelectionEvent)
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e)
	{
		switchEditors(tree.getLastSelectedPathComponent());
	}

	private void switchEditors(final Control control)
	{
		// Try to save the current editor first.
		if( currentEditor != null )
		{
			// If it hasn't been deleted from the tree...
			final Control currentControl = currentEditor.getControl();
			if( (currentControl instanceof PagedWizardModel || currentControl.getParent() != null)
				&& currentEditor.hasDetectedChanges() )
			{
				currentEditor.saveToControl();
				tree.controlChanged(currentControl);
			}
			currentEditor = null;
		}

		GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
		{
			@Override
			public Object construct() throws Exception
			{
				// See if we can construct a new editor for the given control.
				// We are creating a new
				// editor everytime at the moment, but we might be able to cache
				// them eventually.
				if( control != null )
				{
					// Get the class
					ControlDefinition definition = control.getDefinition();
					Extension extension = definition.getExtension();
					EditorFactory editorFactory = (EditorFactory) pluginService.getBean(
						extension.getDeclaringPluginDescriptor(), definition.getEditorFactoryClass());

					currentEditor = editorFactory.getEditor(control, wizardType, schema, pluginService);

					currentEditor.setClientService(clientService);
					currentEditor.setPluginService(pluginService);
					currentEditor.setScriptOptions(options);
					currentEditor.setEntityEditor(entityEditor);
					currentEditor.init();

					// Ask it to load it's defaults.
					currentEditor.loadFromControl();
					currentEditor.clearChanges();
				}
				return null;
			}

			@Override
			protected void afterFinished()
			{
				// don't request focus
			}

			@Override
			public void exception()
			{
				Driver.displayInformation(panel,
					CurrentLocale.get("com.tle.admin.itemdefinition.wizardtab.editorerror")); //$NON-NLS-1$
				LOGGER.warn("Could not create editor", getException());

				// We want to do the stuff in finished() anyway
				finished();
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

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == tree )
		{
			onAddControl(tree.getLastSelectedPathComponent());
		}
	}

	private void onAddControl(final Control parent)
	{
		final GlassSwingWorker<ControlDialog> worker = new GlassSwingWorker<ControlDialog>()
		{
			@Override
			public ControlDialog construct()
			{
				ControlDialog dialog = new ControlDialog(Driver.instance().getControlRepository());

				dialog.addControls(Contexts.CONTEXT_PAGES);
				if( !tree.getModel().hasMetadataControl() )
				{
					dialog.addControls(Contexts.CONTEXT_METADATA);
				}
				if( parent != null )
				{
					List<String> contexts = parent.getContexts();
					for( String context : contexts )
					{
						dialog.addControls(context);
					}
				}
				return dialog;
			}

			@Override
			public void finished()
			{
				ControlDialog dialog = get();
				ControlDefinition def = dialog.promptForSelection(panel);

				if( def != null )
				{
					tree.doAddControl(def, parent, true);
				}
			}
		};

		worker.setComponent(panel);
		worker.start();
	}

	/**
	 * The size of the tree is now somewhat dynamic. It was getting lost in the
	 * new big windows :)
	 */
	@Override
	public void componentResized(ComponentEvent event)
	{
		int treeWidth = (int) (panel.getSize().width * 0.25);
		if( treeWidth < MIN_TREE_WIDTH )
		{
			treeWidth = MIN_TREE_WIDTH;
		}
		else if( treeWidth > MAX_TREE_WIDTH )
		{
			treeWidth = MAX_TREE_WIDTH;
		}
		guiLayout.setColumns(new int[]{treeWidth, TableLayout.FILL});
	}

	@Override
	public void componentHidden(ComponentEvent componentevent)
	{
		// Nothing to do here
	}

	@Override
	public void componentMoved(ComponentEvent componentevent)
	{
		// Nothing to do here
	}

	@Override
	public void componentShown(ComponentEvent componentevent)
	{
		// Nothing to do here
	}
}
