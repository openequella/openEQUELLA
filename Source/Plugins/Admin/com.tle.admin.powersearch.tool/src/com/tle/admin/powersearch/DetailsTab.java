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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.dytech.gui.JShuffleBox;
import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.baseentity.BaseEntityEditor.AbstractDetailsTab;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.i18n.I18nTextArea;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.Format;
import com.tle.common.NameValue;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.SingleUserSelector;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.core.remoting.RemoteSchemaService;
import com.tle.core.remoting.RemoteUserService;
import com.tle.i18n.BundleCache;

/**
 * @author Nicholas Read
 */
public class DetailsTab extends AbstractPowerSearchTab implements ActionListener, AbstractDetailsTab<PowerSearch>
{
	private I18nTextField name;
	private I18nTextArea description;
	private SingleUserSelector owner;

	private JShuffleBox<NameValue> itemdefs;
	private RemoteSchemaService schemaService;

	private JComboBox schemaList;
	int lastSelectedIndex;

	@Override
	public void setDriver(Driver driver)
	{
		super.setDriver(driver);
		this.schemaService = clientService.getService(RemoteSchemaService.class);
	}

	@Override
	public String getTitle()
	{
		return CurrentLocale.get("com.tle.admin.powersearch.detailstab.title"); //$NON-NLS-1$
	}

	@Override
	public void init(Component parent)
	{
		JLabel nameLabel = new JLabel(CurrentLocale.get("com.tle.admin.powersearch.detailstab.searchname")); //$NON-NLS-1$
		JLabel descriptionLabel = new JLabel(CurrentLocale.get("com.tle.admin.powersearch.detailstab.desc")); //$NON-NLS-1$
		JLabel ownerLabel = new JLabel(CurrentLocale.get("com.tle.admin.powersearch.detailstab.owner")); //$NON-NLS-1$

		JLabel schemaLabel = new JLabel(CurrentLocale.get("com.tle.admin.powersearch.detailstab.selectschema")); //$NON-NLS-1$
		JLabel itemdefLabel = new JLabel(CurrentLocale.get("com.tle.admin.powersearch.detailstab.selectcollections")); //$NON-NLS-1$

		descriptionLabel.setVerticalAlignment(SwingConstants.TOP);

		name = new I18nTextField(BundleCache.getLanguages());
		description = new I18nTextArea(BundleCache.getLanguages());

		owner = new SingleUserSelector(clientService.getService(RemoteUserService.class));

		itemdefs = new JShuffleBox<NameValue>();
		itemdefs.setAllowDuplicates(false);

		schemaList = new JComboBox();
		try
		{
			List<BaseEntityLabel> schemas = clientService.getService(RemoteSchemaService.class).listAll();
			List<NameValue> nameValues = BundleCache.getNameValues(schemas);
			Collections.sort(nameValues, Format.NAME_VALUE_COMPARATOR);
			AppletGuiUtils.addItemsToJCombo(schemaList, nameValues);
		}
		catch( RuntimeApplicationException ex )
		{
			LOGGER.error("Could not enumerate schemas", ex); //$NON-NLS-1$
			JOptionPane.showMessageDialog(parent, CurrentLocale.get("com.tle.admin.powersearch.detailstab.error")); //$NON-NLS-1$
		}

		schemaList.setSelectedIndex(-1);
		schemaList.addActionListener(this);
		lastSelectedIndex = -1;

		final int height1 = name.getPreferredSize().height;
		final int ownerHeight = owner.getPreferredSize().height;
		final int width1 = schemaLabel.getPreferredSize().width;

		final int[] rows = {height1, height1 * 3, ownerHeight, 20, height1, height1, TableLayout.DOUBLE_FILL,
				TableLayout.FILL};
		final int[] cols = {width1, TableLayout.DOUBLE_FILL, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		add(nameLabel, new Rectangle(0, 0, 1, 1));
		add(name, new Rectangle(1, 0, 1, 1));

		add(descriptionLabel, new Rectangle(0, 1, 1, 1));
		add(description, new Rectangle(1, 1, 1, 1));

		add(ownerLabel, new Rectangle(0, 2, 1, 1));
		add(owner, new Rectangle(1, 2, 1, 1));

		add(schemaLabel, new Rectangle(0, 4, 1, 1));
		add(schemaList, new Rectangle(1, 4, 1, 1));

		add(itemdefLabel, new Rectangle(0, 5, 2, 1));
		add(itemdefs, new Rectangle(0, 6, 2, 1));

		// Make sure things are readonly.
		if( state.isReadonly() )
		{
			name.setEnabled(false);
			description.setEnabled(false);
			owner.setEnabled(false);
			schemaList.setEnabled(false);
			itemdefs.setEnabled(false);
		}
	}

	@Override
	public void addNameListener(KeyListener listener)
	{
		name.addKeyListener(listener);
	}

	@Override
	public void load()
	{
		final PowerSearch powerSearch = state.getEntity();

		// Load the basic stuff
		name.load(powerSearch.getName());
		description.load(powerSearch.getDescription());
		owner.setUserId(powerSearch.getOwner());

		// Select the correct schema in the list
		schemaList.removeActionListener(this);
		int count = schemaList.getItemCount();
		Schema schema1 = powerSearch.getSchema();
		if( schema1 != null )
		{
			for( int i = 0; i < count && lastSelectedIndex == -1; i++ )
			{
				NameValue pair = (NameValue) schemaList.getItemAt(i);
				if( pair.getValue().equals(Long.toString(schema1.getId())) )
				{
					schemaList.setSelectedIndex(i);
					lastSelectedIndex = i;
				}
			}
		}
		schemaList.addActionListener(this);

		// Load the schema and item defs
		if( schemaList.getSelectedIndex() >= 0 )
		{
			loadSchemaAndItemDefs(new Runnable()
			{
				@Override
				public void run()
				{
					// Select the correct item defs
					Collection<ItemDefinition> idefs = powerSearch.getItemdefs();
					if( idefs != null )
					{
						Set<String> ids = new HashSet<String>();
						for( ItemDefinition idef : idefs )
						{
							ids.add(Long.toString(idef.getId()));
						}

						List<NameValue> nvs = new ArrayList<NameValue>();
						for( NameValue pair : itemdefs.getLeft() )
						{
							if( ids.contains(pair.getValue()) )
							{
								nvs.add(pair);
							}
						}

						for( NameValue pair : nvs )
						{
							itemdefs.removeFromLeft(pair);
							itemdefs.addToRight(pair);
						}
					}
					parentChangeDetector.clearChanges();
				}
			});
		}

	}

	@Override
	public void save()
	{
		final PowerSearch powerSearch = state.getEntity();

		powerSearch.setName(name.save());
		powerSearch.setDescription(description.save());
		powerSearch.setOwner(owner.getUser().getUniqueID());

		NameValue nv = (NameValue) schemaList.getSelectedItem();
		powerSearch.setSchema(new Schema(Long.parseLong(nv.getValue())));

		Collection<ItemDefinition> newItemdefs = new ArrayList<ItemDefinition>();
		for( NameValue pair : itemdefs.getRight() )
		{
			newItemdefs.add(new ItemDefinition(Long.parseLong(pair.getValue())));
		}
		powerSearch.setItemdefs(newItemdefs);
	}

	public void promptToChangeSchema()
	{
		if( lastSelectedIndex != -1 )
		{
			int result = JOptionPane.showConfirmDialog(panel,
				CurrentLocale.get("com.tle.admin.powersearch.detailstab.confirm"), CurrentLocale //$NON-NLS-1$
					.get("com.tle.admin.powersearch.detailstab.sure"), JOptionPane.YES_NO_OPTION, //$NON-NLS-1$
				JOptionPane.QUESTION_MESSAGE);

			if( result != JOptionPane.YES_OPTION )
			{
				schemaList.setSelectedIndex(lastSelectedIndex);
				return;
			}
		}

		final NameValue schemaPair = (NameValue) schemaList.getSelectedItem();

		statusBar.setMessage(CurrentLocale.get("com.tle.admin.powersearch.detailstab.loading", //$NON-NLS-1$
			schemaPair));
		statusBar.setSpinnerVisible(true);

		GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
		{
			@Override
			public Object construct() throws Exception
			{
				loadSchemaAndItemDefs(null);
				return null;
			}

			@Override
			public void finished()
			{
				lastSelectedIndex = schemaList.getSelectedIndex();
				clearStatusBar();
			}

			@Override
			public void exception()
			{
				Exception ex = getException();
				LOGGER.error("Could not switch to schema '" + schemaPair.getValue() + "'", ex); //$NON-NLS-1$ //$NON-NLS-2$
				JOptionPane.showMessageDialog(panel,
					CurrentLocale.get("com.tle.admin.powersearch.detailstab.couldnotload")); //$NON-NLS-1$
				clearStatusBar();
			}

			private void clearStatusBar()
			{
				statusBar.clearMessage();
				statusBar.setSpinnerVisible(false);
			}
		};

		worker.setComponent(panel);
		worker.start();
	}

	void loadSchemaAndItemDefs(final Runnable afterLoad)
	{
		final NameValue schemaPair = (NameValue) schemaList.getSelectedItem();
		long schemaId = Long.parseLong(schemaPair.getValue());

		final Schema schemaBean = schemaService.get(schemaId);

		List<BaseEntityLabel> labels = clientService.getService(RemoteItemDefinitionService.class)
			.listUsableItemDefinitionsForSchema(schemaId);
		final List<NameValue> itemdefsForSchema = BundleCache.getNameValues(labels);
		Collections.sort(itemdefsForSchema, Format.NAME_VALUE_COMPARATOR);

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Load the schema
				schema.loadSchema(schemaBean.getDefinitionNonThreadSafe());

				// Add all the item defs
				itemdefs.removeAllFromLeft();
				itemdefs.removeAllFromRight();
				itemdefs.addToLeft(itemdefsForSchema);

				if( afterLoad != null )
				{
					afterLoad.run();
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == schemaList )
		{
			promptToChangeSchema();
		}
	}

	@Override
	public void validation() throws EditorException
	{
		if( name.isCompletelyEmpty() )
		{
			throw new EditorException(CurrentLocale.get("com.tle.admin.powersearch.detailstab.supplyname")); //$NON-NLS-1$
		}

		if( itemdefs.getRightCount() == 0 )
		{
			throw new EditorException(CurrentLocale.get("com.tle.admin.powersearch.detailstab.selectcollection")); //$NON-NLS-1$
		}

		if( schemaList.getSelectedIndex() == -1 )
		{
			throw new EditorException(CurrentLocale.get("com.tle.admin.powersearch.detailstab.selectschema")); //$NON-NLS-1$
		}

		if( owner.getUser() == null )
		{
			throw new EditorException(CurrentLocale.get("com.tle.admin.powersearch.detailstab.noowner")); //$NON-NLS-1$
		}
	}
}
