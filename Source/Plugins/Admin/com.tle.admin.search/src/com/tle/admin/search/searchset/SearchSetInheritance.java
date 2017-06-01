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

package com.tle.admin.search.searchset;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.dytech.common.text.NumberStringComparator;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.dytech.gui.ChangeDetector;
import com.dytech.gui.Changeable;
import com.dytech.gui.TableLayout;
import com.tle.admin.search.searchset.scripting.ScriptingCellEditor;
import com.tle.admin.search.searchset.scripting.ScriptingTableCellRenderer;
import com.tle.admin.search.searchset.scripting.ScriptingTableModelInterface;
import com.tle.beans.EntityScript;
import com.tle.beans.ItemDefinitionScript;
import com.tle.beans.NameId;
import com.tle.beans.SchemaScript;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.applet.client.ClientService;
import com.tle.common.applet.client.EntityCache;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.search.searchset.SearchSet;

/**
 * @author Nicholas Read
 */
public class SearchSetInheritance extends JPanel implements Changeable
{
	private static final long serialVersionUID = 1L;

	private final ChangeDetector changeDetector;
	private final JCheckBox inheritFreetext;
	private final InheritedTableModel<Schema, SchemaScript> schemasModel;
	private final InheritedTableModel<ItemDefinition, ItemDefinitionScript> itemDefsModel;

	public SearchSetInheritance(final EntityCache cache, final ClientService clientService)
	{
		final JLabel schemasLabel = new JLabel(
			CurrentLocale.get("com.tle.admin.search.searchset.searchsetinheritance.inheritschemas")); //$NON-NLS-1$
		final JLabel itemDefsLabel = new JLabel(
			CurrentLocale.get("com.tle.admin.search.searchset.searchsetinheritance.inheritcollections")); //$NON-NLS-1$

		inheritFreetext = new JCheckBox(
			CurrentLocale.get("com.tle.admin.search.searchset.searchsetinheritance.inheritfreetext")); //$NON-NLS-1$
		schemasModel = new InheritedTableModel<Schema, SchemaScript>(cache.getSchemaMap(), SchemaScript.class,
			CurrentLocale.get("com.tle.admin.search.searchset.searchsetinheritance.schema")); //$NON-NLS-1$
		itemDefsModel = new InheritedTableModel<ItemDefinition, ItemDefinitionScript>(cache.getItemDefinitionMap(),
			ItemDefinitionScript.class,
			CurrentLocale.get("com.tle.admin.search.searchset.searchsetinheritance.collection")); //$NON-NLS-1$

		final JTable schemasTable = generateTable(schemasModel, clientService);
		final JTable itemDefTable = generateTable(itemDefsModel, clientService);

		final int height1 = inheritFreetext.getPreferredSize().height;
		final int height2 = schemasLabel.getPreferredSize().height;

		final int[] rows = {height1, height2, TableLayout.FILL, height2, TableLayout.FILL,};
		final int[] cols = {TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));

		add(inheritFreetext, new Rectangle(0, 0, 1, 1));

		add(schemasLabel, new Rectangle(0, 1, 1, 1));
		add(new JScrollPane(schemasTable), new Rectangle(0, 2, 1, 1));

		add(itemDefsLabel, new Rectangle(0, 3, 1, 1));
		add(new JScrollPane(itemDefTable), new Rectangle(0, 4, 1, 1));

		changeDetector = new ChangeDetector();
		changeDetector.watch(inheritFreetext);
		changeDetector.watch(schemasModel);
		changeDetector.watch(itemDefsModel);
	}

	private JTable generateTable(InheritedTableModel<?, ?> model, ClientService clientService)
	{
		JTable table = new JTable(model);
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		TableColumn c2 = table.getColumnModel().getColumn(2);
		c2.setCellRenderer(new ScriptingTableCellRenderer(model));
		c2.setCellEditor(new ScriptingCellEditor(this, clientService, model));

		return table;
	}

	@Override
	public boolean hasDetectedChanges()
	{
		return changeDetector.hasDetectedChanges();
	}

	@Override
	public void clearChanges()
	{
		changeDetector.clearChanges();
	}

	public void load(SearchSet searchSet, List<Schema> allParentSchemas, List<ItemDefinition> allParentItemDefs)
	{
		inheritFreetext.setSelected(searchSet.isInheritFreetext());
		schemasModel.load(allParentSchemas, searchSet.getInheritedSchemas());
		itemDefsModel.load(allParentItemDefs, searchSet.getInheritedItemDefs());
	}

	public void save(SearchSet searchSet)
	{
		searchSet.setInheritFreetext(inheritFreetext.isSelected());
		searchSet.setInheritedSchemas(schemasModel.save());
		searchSet.setInheritedItemDefs(itemDefsModel.save());
	}

	/**
	 * @author Nicholas Read
	 */
	private static class InheritedTableModel<T extends BaseEntity, U extends EntityScript<T>>
		extends
			AbstractTableModel implements ScriptingTableModelInterface<T>
	{
		private static final long serialVersionUID = 1L;

		private final List<T> entities = new ArrayList<T>();
		private final Set<T> enabled = new HashSet<T>();
		private final Map<T, String> scripts = new HashMap<T, String>();
		private final Class<U> protoClass;
		private final String entityDisplayName;
		private final Map<Long, NameId> cache;

		public InheritedTableModel(Map<Long, NameId> cache, Class<U> protoClass, String entityDisplayName)
		{
			this.cache = cache;
			this.protoClass = protoClass;
			this.entityDisplayName = entityDisplayName;
		}

		public void load(Collection<T> allInherited, Collection<U> enabledInherited)
		{
			for( T entity : allInherited )
			{
				entities.add(entity);
			}

			for( U query : enabledInherited )
			{
				if( entities.contains(query.getEntity()) )
				{
					scripts.put(query.getEntity(), query.getScript());
					enabled.add(query.getEntity());
				}
			}

			Collections.sort(entities, new NumberStringComparator<T>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public String convertToString(T t)
				{
					return getNameId(t).getName();
				}
			});

			fireTableDataChanged();
		}

		public List<U> save()
		{
			List<U> queries = new ArrayList<U>();
			for( T entity : enabled )
			{
				try
				{
					U u = protoClass.newInstance();
					u.setEntity(entity);
					u.setScript(scripts.get(entity));

					queries.add(u);
				}
				catch( Exception ex )
				{
					throw new RuntimeApplicationException(ex);
				}
			}
			return queries;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.dytech.edge.admin.hierarchy.FilteringTab.ScriptingTableModel#
		 * getItemDefinition(int)
		 */
		@Override
		public T getEntity(int row)
		{
			return entities.get(row);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.dytech.edge.admin.hierarchy.FilteringTab.ScriptingTableModel#
		 * enableScripting(int)
		 */
		@Override
		public boolean isScriptingEnabled(int row)
		{
			return enabled.contains(entities.get(row));
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount()
		{
			return entities.size();
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount()
		{
			return 3;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			return columnIndex == 0 ? Boolean.class : super.getColumnClass(columnIndex);
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			T entity = entities.get(rowIndex);
			switch( columnIndex )
			{
				case 0:
					return enabled.contains(entity);
				case 1:
					return getNameId(entity).getName();
				case 2:
					return scripts.get(entity);
				default:
					throw new IllegalStateException();
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			switch( columnIndex )
			{
				case 0:
					return true;
				case 2:
					return isScriptingEnabled(rowIndex);
				default:
					return false;
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			T entity = entities.get(rowIndex);
			switch( columnIndex )
			{
				case 0:
					if( (Boolean) aValue )
					{
						enabled.add(entity);
					}
					else
					{
						enabled.remove(entity);
					}
					break;
				case 2:
					scripts.put(entity, (String) aValue);
					break;
				default:
					throw new IllegalStateException();
			}
			fireTableCellUpdated(rowIndex, columnIndex);
		}

		@Override
		public String getColumnName(int column)
		{
			switch( column )
			{
				case 0:
					return CurrentLocale.get("com.tle.admin.search.searchset.searchsetinheritance.inherited"); //$NON-NLS-1$
				case 1:
					return entityDisplayName;
				case 2:
					return CurrentLocale.get("com.tle.admin.search.searchset.searchsetinheritance.scripting"); //$NON-NLS-1$
				default:
					throw new IllegalStateException();
			}
		}

		private NameId getNameId(T t)
		{
			return cache.get(t.getId());
		}
	}
}
