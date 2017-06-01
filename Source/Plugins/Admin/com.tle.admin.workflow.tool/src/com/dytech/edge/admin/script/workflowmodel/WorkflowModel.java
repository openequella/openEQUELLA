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

package com.dytech.edge.admin.script.workflowmodel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.edge.admin.script.InvalidScriptException;
import com.dytech.edge.admin.script.TargetValueMap;
import com.dytech.edge.admin.script.basicmodel.BasicParser;
import com.dytech.edge.admin.script.basicmodel.ScriptTargetChooser;
import com.dytech.edge.admin.script.basicmodel.ScriptTargetDialog.SelectionType;
import com.dytech.edge.admin.script.options.DefaultScriptOptions;
import com.dytech.gui.TableLayout;
import com.tle.admin.Driver;
import com.tle.admin.controls.scripting.BasicModel;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.TargetListener;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.NameValue;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.core.remoting.RemoteSchemaService;
import com.tle.i18n.BundleCache;

public class WorkflowModel extends BasicModel
{
	private static final long serialVersionUID = 1L;

	private static final Log LOGGER = LogFactory.getLog(WorkflowModel.class);

	private int currentItemDefIndex;
	private TargetValueMap targetMap;
	private boolean loading = true;

	private JComboBox<NameValue> itemdefSelection;

	private RemoteItemDefinitionService itemdefService;
	private RemoteSchemaService schemaService;

	public WorkflowModel(Driver driver, String idStr)
	{
		super(null, new DefaultScriptOptions()
		{
			@Override
			public boolean restrictItemStatusForModeration()
			{
				return true;
			}

			@Override
			public boolean hasUserIsModerator()
			{
				return false;
			}
		}, null);

		setup(driver);

		if( Check.isEmpty(idStr) )
		{
			NameValue pair = itemdefSelection.getItemAt(0);
			loadItemDef(pair.getValue(), false);
		}
		else
		{
			boolean found = false;

			final int count = itemdefSelection.getItemCount();
			for( int i = 0; i < count && !found; i++ )
			{
				NameValue pair = itemdefSelection.getItemAt(i);
				if( idStr.equals(pair.getValue()) )
				{
					found = true;
					itemdefSelection.setSelectedIndex(i);
					currentItemDefIndex = i;
					loadItemDef(idStr, false);
				}
			}
			// if collection is not found (eg. it's been deleted) then need to
			// load something
			if( !found )
			{
				itemdefSelection.setSelectedIndex(0);
				currentItemDefIndex = 0;
				loadItemDef(itemdefSelection.getItemAt(0).getValue(), false);
			}
		}
		loading = false;
	}

	private void setup(Driver driver)
	{
		setup();
		itemdefService = driver.getClientService().getService(RemoteItemDefinitionService.class);
		schemaService = driver.getClientService().getService(RemoteSchemaService.class);
	}

	@Override
	public JPanel getStatementEditor()
	{
		return editor;
	}

	public String getUUID()
	{
		return ((NameValue) itemdefSelection.getSelectedItem()).getValue();
	}

	public void loadItemDef(String uuid, boolean clear)
	{
		try
		{
			ItemDefinition itemdef = itemdefService.getByUuid(uuid);
			if( itemdef.getSchema() != null )
			{
				long schemaId = itemdef.getSchema().getId();
				if( schemaId != 0 )
				{
					Schema schemaBean = schemaService.get(schemaId);
					xpathField.loadSchema(schemaBean.getDefinitionNonThreadSafe());
				}
			}

			targetMap = new TargetValueMap();
			targetMap.addPages(itemdef.getWizard().getPages());

			if( clear )
			{
				clearScript();
			}
		}
		catch( Exception ex )
		{
			Driver.displayError(null, "itemEditor/loading", ex); //$NON-NLS-1$
			LOGGER.error("Error loading collection " + uuid, ex);
		}
	}

	@Override
	public void importScript(Reader in) throws InvalidScriptException
	{
		BasicParser p = new BasicParser(options, new BufferedReader(in));
		importStatement(p.importScript());
	}

	@Override
	protected void updateButtons()
	{
		if( xpathField.getType() == null )
		{
			setButton.setEnabled(false);
			addButton.setEnabled(false);
		}
		else
		{
			super.updateButtons();
		}
	}

	@Override
	protected void setup()
	{
		editor = new JPanel(new BorderLayout(5, 0));
		editor.setAlignmentX(Component.LEFT_ALIGNMENT);
		editor.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
			BorderFactory.createEmptyBorder(2, 5, 2, 5)));

		editor.add(createTop(), BorderLayout.NORTH);
		editor.add(createButtons(), BorderLayout.EAST);
		editor.add(createCentre(), BorderLayout.CENTER);
	}

	private JPanel createTop()
	{
		JLabel label = new JLabel(CurrentLocale.get("com.dytech.edge.admin.script.workflowmodel.workflowmodel.select")); //$NON-NLS-1$
		itemdefSelection = new JComboBox<>();
		itemdefSelection.addActionListener(this);
		AppletGuiUtils.addItemsToJCombo(itemdefSelection, getItemDefs());

		int height = itemdefSelection.getPreferredSize().height;
		int width = label.getPreferredSize().width;

		final int[] rows = {height,};
		final int[] columns = {TableLayout.FILL, width, width * 4, TableLayout.FILL,};

		JPanel all = new JPanel(new TableLayout(rows, columns, 0, 5));
		all.add(label, new Rectangle(1, 0, 1, 1));
		all.add(itemdefSelection, new Rectangle(2, 0, 1, 1));

		return all;
	}

	private JPanel createCentre()
	{
		typeCombo = new JComboBox<>(TYPES.toArray(new String[0]));
		xpathField = new ScriptTargetChooser(new SchemaModel(), options);
		operatorCombo = new JComboBox<>(BASIC_OPERATORS.toArray(new String[0]));
		valueSelection = new JComboBox<>();

		typeCombo.addActionListener(this);
		operatorCombo.addActionListener(this);

		xpathField.addTargetListener(new TargetListener()
		{
			@Override
			public void targetAdded(String target)
			{
				populateValues();
				updateButtons();
			}

			@Override
			public void targetRemoved(String target)
			{
				// We don't care about this event.
			}
		});

		final int height = typeCombo.getPreferredSize().height;
		final int width1 = typeCombo.getPreferredSize().width;
		final int width2 = operatorCombo.getPreferredSize().width;

		final int[] rows = {TableLayout.FILL, height, TableLayout.FILL,};
		final int[] cols = {width1, TableLayout.FILL, width2, TableLayout.FILL,};

		JPanel all = new JPanel(new TableLayout(rows, cols, 0, 5));

		all.add(typeCombo, new Rectangle(0, 1, 1, 1));
		all.add(xpathField, new Rectangle(1, 1, 1, 1));
		all.add(operatorCombo, new Rectangle(2, 1, 1, 1));
		all.add(valueSelection, new Rectangle(3, 1, 1, 1));

		return all;
	}

	private JPanel createButtons()
	{
		addButton = new JButton(CurrentLocale.get("com.dytech.edge.admin.script.add")); //$NON-NLS-1$
		setButton = new JButton(CurrentLocale.get("com.dytech.edge.admin.script.set")); //$NON-NLS-1$

		addButton.addActionListener(this);
		setButton.addActionListener(this);

		setButton.setEnabled(false);

		JPanel all = new JPanel(new BorderLayout(5, 5));
		all.add(addButton, BorderLayout.NORTH);
		all.add(setButton, BorderLayout.SOUTH);

		return all;
	}

	protected Collection<NameValue> getItemDefs()
	{
		List<BaseEntityLabel> result = Driver.instance().getClientService()
			.getService(RemoteItemDefinitionService.class).listAll();
		List<NameValue> nameValues = BundleCache.getNameUuidValues(result);
		Collections.sort(nameValues, Format.NAME_VALUE_COMPARATOR);
		return nameValues;
	}

	// // STATEMENT CREATION ///////////////////////////////////////////////////

	@Override
	protected void populateValues()
	{
		valueSelection.setEditable(false);
		valueSelection.removeAllItems();
		valueSelection.getEditor().setItem(""); //$NON-NLS-1$

		final SelectionType type = xpathField.getType();
		if( type == null )
		{
			return;
		}

		switch( type )
		{
			case ITEM_STATUS:
				addStatuses();
				break;

			case USER_TYPE:
				addRoles();
				break;

			case SCHEMA_ITEM:
				ArrayList<String> items = new ArrayList<String>();
				String target = xpathField.getSchemaXpath();

				for( String s : targetMap.getValuesForTarget(target) )
				{
					items.add(s);
				}

				// We need this for backwards compatability with targets
				// that do not start with a slash.
				String target2 = target.substring(1);
				for( String s : targetMap.getValuesForTarget(target2) )
				{
					items.add(s);
				}

				if( items.isEmpty() )
				{
					valueSelection.setEditable(true);
				}
				else
				{
					for( String item : items )
					{
						valueSelection.addItem(new NameValue(item, item));
					}
				}
				break;

			default:
				valueSelection.setEditable(true);
		}
	}

	// // EVENT HANDLERS
	// ////////////////////////////////////////////////////////

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// We need to make sure we call the super actionPerformed as well.
		if( e.getSource() == itemdefSelection )
		{
			if( loading )
			{
				return;
			}

			int index = itemdefSelection.getSelectedIndex();
			NameValue pair = (NameValue) itemdefSelection.getSelectedItem();

			if( index != currentItemDefIndex )
			{
				int result;
				if( statement.isEmpty() )
				{
					result = JOptionPane.YES_OPTION;
				}
				else
				{
					result = JOptionPane.showConfirmDialog(itemdefSelection, getMessage(), getTitle(),
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				}

				if( result == JOptionPane.YES_OPTION )
				{
					currentItemDefIndex = index;
					loadItemDef(pair.getValue(), true);
					xpathField.setType(null);
					populateValues();
					updateButtons();
				}
				else
				{
					itemdefSelection.setSelectedIndex(currentItemDefIndex);
				}
			}
		}
		else
		{
			super.actionPerformed(e);
		}
	}

	private static String getMessage()
	{
		return CurrentLocale.get("com.dytech.edge.admin.script.workflowmodel.workflowmodel.message"); //$NON-NLS-1$
	}

	private static String getTitle()
	{
		return CurrentLocale.get("com.dytech.edge.admin.script.workflowmodel.workflowmodel.title"); //$NON-NLS-1$
	}
}
