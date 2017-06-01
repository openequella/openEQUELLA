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
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.dytech.common.text.NumberStringComparator;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.dytech.gui.ChangeDetector;
import com.dytech.gui.Changeable;
import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.gui.common.actions.AddAction;
import com.tle.admin.gui.common.actions.RemoveAction;
import com.tle.admin.gui.common.actions.TLEAction;
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
import com.tle.client.gui.popup.TablePopupListener;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.applet.client.EntityCache;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public abstract class EntityWhereEditor<T extends BaseEntity, U extends EntityScript<T>> extends JPanel
	implements
		Changeable
{
	private final ClientService clientService;
	private final Class<T> protoEntity;
	private final Map<Long, NameId> cache;

	private MyTableModel<T, U> model;
	private JTable table;
	private ChangeDetector changeDetector;

	public EntityWhereEditor(Map<Long, NameId> cache, ClientService clientService, Class<T> protoEntity,
		Class<U> protoScript, String entityDisplayName)
	{
		this.cache = cache;
		this.clientService = clientService;
		this.protoEntity = protoEntity;

		setup(protoScript, entityDisplayName);
	}

	private void setup(Class<U> protoScript, String entityDisplayName)
	{
		JButton add = new JButton(addAction);
		JButton remove = new JButton(removeAction);

		model = new MyTableModel<T, U>(cache, protoScript, entityDisplayName);

		table = new JTable(model);
		table.addMouseListener(new TablePopupListener(table, addAction, removeAction));

		ListSelectionModel selModel = table.getSelectionModel();
		selModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		selModel.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				removeAction.update();
			}
		});

		TableColumn c1 = table.getColumnModel().getColumn(1);
		c1.setCellRenderer(new ScriptingTableCellRenderer(model));
		c1.setCellEditor(new ScriptingCellEditor(this, clientService, model));

		final int height1 = add.getPreferredSize().height;
		final int width1 = remove.getPreferredSize().width;

		final int[] rows = {TableLayout.FILL, height1,};
		final int[] cols = {TableLayout.FILL, width1, width1,};

		setLayout(new TableLayout(rows, cols));

		add(new JScrollPane(table), new Rectangle(0, 0, 3, 1));

		add(add, new Rectangle(1, 1, 1, 1));
		add(remove, new Rectangle(2, 1, 1, 1));

		changeDetector = new ChangeDetector();
		changeDetector.watch(model);
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);

		table.setEnabled(enabled);
	}

	public void clear()
	{
		model.removeAll();
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.gui.Changeable#clearChanges()
	 */
	@Override
	public void clearChanges()
	{
		changeDetector.clearChanges();
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.gui.Changeable#hasDetectedChanges()
	 */
	@Override
	public boolean hasDetectedChanges()
	{
		return changeDetector.hasDetectedChanges();
	}

	public void load(List<U> wheres)
	{
		model.load(wheres);
	}

	// public void load(Class<T> klass, Map<Long, String> wheres)
	// {
	// List<U> result = new ArrayList<U>();
	// if( wheres != null )
	// {
	// for( Map.Entry<Long, String> entry : wheres.entrySet() )
	// {
	// try
	// {
	// T entity = klass.newInstance();
	// entity.setId(entry.getKey());
	//
	// U u = protoClass.newInstance();
	// u.setEntity(entity);
	// u.setScript(entry.getValue());
	//
	// result.add(u);
	// }
	// catch( Exception e )
	// {
	// throw new RuntimeApplicationException(e);
	// }
	// }
	// cache.initialiseNames(result);
	// }
	// load(result);
	// }

	public List<U> saveAsList()
	{
		return model.save();
	}

	public Map<Long, String> saveAsIdMap()
	{
		List<U> list = saveAsList();

		Map<Long, String> results = new HashMap<Long, String>(list.size());
		for( U where : list )
		{
			results.put(where.getEntity().getId(), where.getScript());
		}
		return results;
	}

	protected abstract EntityDialog getEntityDialog(Collection<NameId> allValues, Set<NameId> currentEntities);

	private final TLEAction addAction = new AddAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			GlassSwingWorker<EntityDialog> worker = new GlassSwingWorker<EntityDialog>()
			{
				@Override
				public EntityDialog construct() throws Exception
				{
					return getEntityDialog(cache.values(), model.getNameIds());
				}

				@Override
				public void finished()
				{
					List<NameId> entities = get().showDialog(getComponent());
					for( NameId entity : entities )
					{
						try
						{
							T t = protoEntity.newInstance();
							t.setId(entity.getId());
							model.add(t);
						}
						catch( Exception e ) /*
											 * InstantiationException,
											 * IllegalAccessException
											 */
						{
							e.printStackTrace();
						}
					}
				}

				@Override
				public void exception()
				{
					Driver.displayError(getComponent(), "itemEditor/enumerating", getException()); //$NON-NLS-1$
					getException().printStackTrace();
				}
			};
			worker.setComponent(EntityWhereEditor.this);
			worker.start();
		}
	};

	private final TLEAction removeAction = new RemoveAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			int[] rows = table.getSelectedRows();
			for( int i = rows.length - 1; i >= 0; i-- )
			{
				model.remove(rows[i]);
			}
		}

		@Override
		public void update()
		{
			setEnabled(table.getSelectedRowCount() > 0);
		}
	};

	/**
	 * @author Nicholas Read
	 */
	private static class MyTableModel<T extends BaseEntity, U extends EntityScript<T>> extends AbstractTableModel
		implements
			ScriptingTableModelInterface<T>
	{
		private static final long serialVersionUID = 1L;
		private final Map<Long, NameId> entityCache;
		private final Class<U> proto;
		private final String entityDisplayName;
		private final List<U> queries = new ArrayList<U>();

		public MyTableModel(Map<Long, NameId> entityCache, Class<U> proto, String entityDisplayName)
		{
			this.entityCache = entityCache;
			this.proto = proto;
			this.entityDisplayName = entityDisplayName;
		}

		public void removeAll()
		{
			queries.clear();
			fireTableDataChanged();
		}

		public void load(List<U> wheres)
		{
			if( !Check.isEmpty(wheres) )
			{
				queries.addAll(wheres);
				sort();
				fireTableDataChanged();
			}
		}

		public List<U> save()
		{
			return new ArrayList<U>(queries);
		}

		public Set<NameId> getNameIds()
		{
			Set<NameId> results = new HashSet<NameId>();
			for( U query : queries )
			{
				results.add(getNameId(query));
			}
			return results;
		}

		public void add(T entity)
		{
			try
			{
				U u = proto.newInstance();
				u.setEntity(entity);

				queries.add(u);
				sort();

				int index = queries.indexOf(u);
				fireTableRowsInserted(index, index);
			}
			catch( Exception e ) /*
								 * InstantiationException,
								 * IllegalAccessException
								 */
			{
				throw new RuntimeApplicationException(e);
			}
		}

		public void remove(int rowIndex)
		{
			queries.remove(rowIndex);
			int insertedIndex = queries.size();
			fireTableRowsDeleted(insertedIndex, insertedIndex);
		}

		private void sort()
		{
			Collections.sort(queries, new NumberStringComparator<U>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public String convertToString(U u)
				{
					return getNameId(u).getName();
				}
			});
		}

		/**
		 * @param u
		 * @return A placeholder NameId if the entity is not found (e.g. it's
		 *         been deleted)
		 */
		protected NameId getNameId(U u)
		{
			long id = u.getEntity().getId();
			NameId nameId = entityCache.get(id);
			if( nameId == null )
			{
				nameId = new NameId(CurrentLocale.get("com.tle.admin.search.unknownentity"), id); //$NON-NLS-1$
			}
			return nameId;
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
			return queries.get(row).getEntity();
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
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount()
		{
			return queries.size();
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount()
		{
			return 2;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			U query = queries.get(rowIndex);
			switch( columnIndex )
			{
				case 0:
					return getNameId(query).getName();
				case 1:
					return query.getScript();
				default:
					throw new IllegalStateException();
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return columnIndex == 1;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			if( columnIndex == 1 )
			{
				queries.get(rowIndex).setScript((String) aValue);
				fireTableCellUpdated(rowIndex, columnIndex);
			}
			else
			{
				throw new IllegalStateException();
			}
		}

		@Override
		public String getColumnName(int column)
		{
			switch( column )
			{
				case 0:
					return entityDisplayName;
				case 1:
					return CurrentLocale.get("com.tle.admin.gui.common.entitywhereeditor.scripting"); //$NON-NLS-1$
				default:
					throw new IllegalStateException();
			}
		}
	}

	/**
	 * @author Nicholas Read
	 */
	public static class ItemDefinitionWhereEditor extends EntityWhereEditor<ItemDefinition, ItemDefinitionScript>
	{
		private static final long serialVersionUID = 1L;

		public ItemDefinitionWhereEditor(EntityCache cache, ClientService clientService)
		{
			super(cache.getItemDefinitionMap(), clientService, ItemDefinition.class, ItemDefinitionScript.class,
				CurrentLocale.get("com.tle.admin.gui.common.entitywhereeditor.collection")); //$NON-NLS-1$
		}

		@Override
		protected EntityDialog getEntityDialog(Collection<NameId> cache, Set<NameId> filterOut)
		{
			return new EntityDialog.ItemDefinitionDialog(cache, filterOut);
		}
	}

	/**
	 * @author Nicholas Read
	 */
	public static class SchemaWhereEditor extends EntityWhereEditor<Schema, SchemaScript>
	{
		private static final long serialVersionUID = 1L;

		public SchemaWhereEditor(EntityCache cache, ClientService clientService)
		{
			super(cache.getSchemaMap(), clientService, Schema.class, SchemaScript.class, CurrentLocale
				.get("com.tle.admin.gui.common.entitywhereeditor.schema")); //$NON-NLS-1$
		}

		@Override
		protected EntityDialog getEntityDialog(Collection<NameId> cache, Set<NameId> filterOut)
		{
			return new EntityDialog.SchemaDialog(cache, filterOut);
		}
	}
}
