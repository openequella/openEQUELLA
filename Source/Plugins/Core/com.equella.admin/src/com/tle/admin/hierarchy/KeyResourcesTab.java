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

package com.tle.admin.hierarchy;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.common.actions.DownAction;
import com.tle.admin.gui.common.actions.JTextlessButton;
import com.tle.admin.gui.common.actions.RemoveAction;
import com.tle.admin.gui.common.actions.SearchAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.admin.gui.common.actions.UpAction;
import com.tle.admin.hierarchy.TopicEditor.AbstractTopicEditorTab;
import com.tle.beans.hierarchy.HierarchyPack;
import com.tle.beans.item.Item;
import com.tle.client.gui.popup.TablePopupListener;
import com.tle.common.adminconsole.RemoteAdminService;
import com.tle.common.applet.client.ClientService;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.searching.Search;
import com.tle.common.searching.SearchResults;
import com.tle.common.searching.VeryBasicSearch;
import com.tle.i18n.BundleCache;

/**
 * @author Nicholas Read
 */
public class KeyResourcesTab extends AbstractTopicEditorTab implements ActionListener
{
	private static final long serialVersionUID = 1L;

	private static final int RESULTS_PER_PAGE = 10;

	private final RemoteAdminService adminService;

	private final List<TLEAction> actions = new ArrayList<TLEAction>();

	private MyTableModel selectedModel;
	private JTable selectedTable;

	private int startResult;
	private int totalResults;

	private JTextField searchQuery;

	private JButton firstPage;
	private JButton prevPage;
	private JButton nextPage;
	private JButton lastPage;

	private JLabel countLabel;
	private MyTableModel searchModel;
	private JTable searchTable;
	private JButton addAsKeyResource;

	public KeyResourcesTab(ClientService clientService)
	{
		adminService = clientService.getService(RemoteAdminService.class);
	}

	@Override
	@SuppressWarnings("nls")
	public void setup(ChangeDetector changeDetector)
	{
		JLabel selectedLabel = new JLabel(getString("keyresourcestab.selected"));
		JLabel searchLabel = new JLabel(getString("keyresourcestab.find"));

		final int height1 = selectedLabel.getPreferredSize().height;
		final int width1 = 20;
		final int[] rows = {height1, TableLayout.FILL, height1, TableLayout.FILL,};
		final int[] cols = {width1, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));
		setBorder(AppletGuiUtils.DEFAULT_BORDER);

		add(selectedLabel, new Rectangle(0, 0, 2, 1));
		add(setupSelectedPanel(changeDetector), new Rectangle(1, 1, 1, 1));
		add(searchLabel, new Rectangle(0, 2, 2, 1));
		add(setupSearchPanel(), new Rectangle(1, 3, 1, 1));

		update();
	}

	private JComponent setupSelectedPanel(ChangeDetector changeDetector)
	{
		actions.add(upAction);
		actions.add(downAction);
		actions.add(removeAction);

		selectedModel = new MyTableModel();
		selectedTable = createTable(selectedModel);
		selectedTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		selectedTable.addMouseListener(new TablePopupListener(selectedTable, removeAction, upAction, downAction));
		selectedTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				update();
			}
		});

		JButton up = new JTextlessButton(upAction);
		JButton down = new JTextlessButton(downAction);
		JButton remove = new JButton(removeAction);

		final int width1 = up.getPreferredSize().width;
		final int width2 = remove.getPreferredSize().width;
		final int height1 = up.getPreferredSize().height;
		final int height2 = remove.getPreferredSize().height;
		final int[] rows = {TableLayout.FILL, height1, height1, TableLayout.FILL, height2,};
		final int[] cols = {width1, TableLayout.FILL, width2,};

		JPanel all = new JPanel(new TableLayout(rows, cols));
		all.add(new JScrollPane(selectedTable), new Rectangle(1, 0, 2, 4));
		all.add(up, new Rectangle(0, 1, 1, 1));
		all.add(down, new Rectangle(0, 2, 1, 1));
		all.add(remove, new Rectangle(2, 4, 1, 1));

		changeDetector.watch(selectedModel);

		return all;
	}

	private JComponent setupSearchPanel()
	{
		searchQuery = new JTextField();
		searchQuery.setAction(searchAction);

		JButton searchButton = new JButton(searchAction);

		searchModel = new MyTableModel();
		searchTable = createTable(searchModel);
		searchTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		searchTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				addAsKeyResource.setEnabled(searchTable.getSelectedRowCount() > 0);
			}
		});

		addAsKeyResource = new JButton(getString("keyresourcestab.add")); //$NON-NLS-1$
		addAsKeyResource.addActionListener(this);
		addAsKeyResource.setEnabled(false);

		final int width1 = searchButton.getPreferredSize().width;
		final int width2 = addAsKeyResource.getPreferredSize().width;
		final int height1 = searchButton.getPreferredSize().height;
		final int[] rows = {height1, TableLayout.FILL, height1, height1,};
		final int[] cols = {width1, TableLayout.FILL, width2, TableLayout.FILL, width1,};

		JPanel all = new JPanel(new TableLayout(rows, cols));

		all.add(searchQuery, new Rectangle(0, 0, 4, 1));
		all.add(searchButton, new Rectangle(4, 0, 1, 1));
		all.add(new JScrollPane(searchTable), new Rectangle(0, 1, 5, 1));
		all.add(setupResultsNavigator(), new Rectangle(0, 2, 5, 1));
		all.add(addAsKeyResource, new Rectangle(2, 3, 1, 1));

		return all;
	}

	@SuppressWarnings("nls")
	private JComponent setupResultsNavigator()
	{
		countLabel = new JLabel();
		firstPage = new JButton("<<");
		prevPage = new JButton("<");
		nextPage = new JButton(">");
		lastPage = new JButton(">>");

		firstPage.setEnabled(false);
		prevPage.setEnabled(false);
		nextPage.setEnabled(false);
		lastPage.setEnabled(false);

		firstPage.addActionListener(this);
		prevPage.addActionListener(this);
		nextPage.addActionListener(this);
		lastPage.addActionListener(this);

		final int width1 = firstPage.getPreferredSize().width;
		final int height = firstPage.getPreferredSize().height;
		final int[] rows = {height,};
		final int[] cols = {TableLayout.FILL, width1, width1, width1, width1,};

		JPanel all = new JPanel(new TableLayout(rows, cols));

		all.add(countLabel, new Rectangle(0, 0, 1, 1));
		all.add(firstPage, new Rectangle(1, 0, 1, 1));
		all.add(prevPage, new Rectangle(2, 0, 1, 1));
		all.add(nextPage, new Rectangle(3, 0, 1, 1));
		all.add(lastPage, new Rectangle(4, 0, 1, 1));

		return all;
	}

	private JTable createTable(final MyTableModel model)
	{
		return new JTable(model)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getToolTipText(MouseEvent event)
			{
				String tip = null;
				int rowIndex = rowAtPoint(event.getPoint());
				if( rowIndex != -1 )
				{
					// References 'final' model passed into createTable()
					// function.
					tip = model.getItemDescription(rowIndex);
				}

				if( tip == null || tip.trim().length() == 0 )
				{
					tip = getString("keyresourcestab.nodesc"); //$NON-NLS-1$
				}
				else if( tip.length() > 100 )
				{
					tip = tip.substring(0, 100) + "..."; //$NON-NLS-1$
				}

				return tip;
			}
		};
	}

	@Override
	public void load(HierarchyPack pack)
	{
		selectedModel.setEntries(pack.getTopic().getKeyResources());
	}

	@Override
	public void save(HierarchyPack pack)
	{
		pack.getTopic().setKeyResources(selectedModel.getEntries());
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == addAsKeyResource )
		{
			Item entry = searchModel.getEntry(searchTable.getSelectedRow());

			Set<Integer> versions = selectedModel.getItemVersions(entry.getUuid());
			if( versions.contains(entry.getVersion()) )
			{
				JOptionPane.showMessageDialog(this,
					getString("keyresourcestab.warn"), CurrentLocale //$NON-NLS-1$
						.get(getKey("keyresourcestab.already")), //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
			}
			else if( !versions.isEmpty() )
			{
				String message = null;
				if( versions.size() == 1 )
				{
					final int otherVersion = versions.iterator().next();
					if( otherVersion < entry.getVersion() )
					{
						message = getString("keyresourcestab.earlier"); //$NON-NLS-1$
					}
					else
					{
						message = getString("keyresourcestab.newer"); //$NON-NLS-1$
					}
				}
				else
				{
					message = getString("keyresourcestab.other"); //$NON-NLS-1$
				}

				if( JOptionPane.showConfirmDialog(this, message,
					CurrentLocale.get("com.dytech.edge.admin.gui.confirm"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ) //$NON-NLS-1$
				{
					selectedModel.addEntry(entry);
				}
			}
			else
			{
				selectedModel.addEntry(entry);
			}
		}
		else if( e.getSource() == firstPage )
		{
			startResult = 0;
			doSearch();
		}
		else if( e.getSource() == nextPage )
		{
			startResult += RESULTS_PER_PAGE;
			doSearch();
		}
		else if( e.getSource() == prevPage )
		{
			startResult -= RESULTS_PER_PAGE;
			doSearch();
		}
		else if( e.getSource() == lastPage )
		{
			startResult = totalResults
				- (totalResults % RESULTS_PER_PAGE == 0 ? RESULTS_PER_PAGE : totalResults % RESULTS_PER_PAGE);
			doSearch();
		}
	}

	private void update()
	{
		for( TLEAction action : actions )
		{
			action.update();
		}
	}

	private final TLEAction searchAction = new SearchAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			startResult = 0;
			doSearch();
		}
	};

	private final TLEAction removeAction = new RemoveAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			final int[] rows = selectedTable.getSelectedRows();
			for( int i = rows.length - 1; i >= 0; i-- )
			{
				selectedModel.removeEntry(rows[i]);
			}
		}

		@Override
		public void update()
		{
			setEnabled(selectedTable.getSelectedRowCount() > 0);
		}
	};

	private final TLEAction upAction = new UpAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			int row = selectedTable.getSelectedRow();
			selectedModel.moveUp(row);
			selectedTable.getSelectionModel().setSelectionInterval(row - 1, row - 1);
		}

		@Override
		public void update()
		{
			final int[] rows = selectedTable.getSelectedRows();
			setEnabled(rows.length == 1 && rows[0] >= 1);
		}
	};

	private final TLEAction downAction = new DownAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			int row = selectedTable.getSelectedRow();
			selectedModel.moveDown(row);
			selectedTable.getSelectionModel().setSelectionInterval(row + 1, row + 1);
		}

		@Override
		public void update()
		{
			final int[] rows = selectedTable.getSelectedRows();
			setEnabled(rows.length == 1 && rows[0] < (selectedModel.getRowCount() - 1));
		}
	};

	private void doSearch()
	{
		searchModel.setEntries(null);

		final String queryTerms = searchQuery.getText();
		GlassSwingWorker<SearchResults<Item>> worker = new GlassSwingWorker<SearchResults<Item>>()
		{
			@Override
			public SearchResults<Item> construct() throws Exception
			{
				Search search = new VeryBasicSearch(queryTerms);
				SearchResults<Item> results = adminService.searchReducedItems(search, startResult, RESULTS_PER_PAGE);
				BundleCache.ensureCached(results.getResults(), BundleCache.ITEM_NAME_GETTER);
				BundleCache.ensureCached(results.getResults(), BundleCache.ITEM_DESCRIPTION_GETTER);
				return results;
			}

			@Override
			public void finished()
			{
				SearchResults<Item> results = get();
				totalResults = results.getAvailable();
				searchModel.setEntries(results.getResults());

				doInAnyCase();
			}

			@Override
			public void exception()
			{
				Driver.displayInformation(getComponent(),
					getString("keyresourcestab.invalid")); //$NON-NLS-1$
				getException().printStackTrace();

				doInAnyCase();
			}

			public void doInAnyCase()
			{
				countLabel.setText(CurrentLocale.get(
					getKey("keyresourcestab.results"), new Object[]{ //$NON-NLS-1$
					startResult + 1, startResult + searchModel.getRowCount(), totalResults}));

				prevPage.setEnabled(startResult > 0);
				firstPage.setEnabled(startResult > 0);

				boolean morePages = startResult + RESULTS_PER_PAGE < totalResults;
				nextPage.setEnabled(morePages);
				lastPage.setEnabled(morePages);
			}
		};
		worker.setComponent(this);
		worker.start();
	}

	/**
	 * @author Nicholas Read
	 */
	private class MyTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 1L;
		private final String FIRST = getString("keyresourcestab.name"); //$NON-NLS-1$
		private final String SECOND = getString("keyresourcestab.version"); //$NON-NLS-1$
		private final String THIRD = getString("keyresourcestab.status"); //$NON-NLS-1$

		private final List<Item> entries = new ArrayList<Item>();

		public MyTableModel()
		{
			super();
		}

		public Set<Integer> getItemVersions(String itemUuid)
		{
			Set<Integer> results = new HashSet<Integer>();
			for( Item item : entries )
			{
				if( Objects.equals(itemUuid, item.getUuid()) )
				{
					results.add(item.getVersion());
				}
			}
			return results;
		}

		public List<Item> getEntries()
		{
			return Collections.unmodifiableList(entries);
		}

		public void setEntries(List<Item> entries)
		{
			this.entries.clear();
			if( entries != null )
			{
				this.entries.addAll(entries);
			}
			fireTableDataChanged();
		}

		public void addEntry(Item entry)
		{
			int index = entries.size();
			entries.add(entry);
			fireTableRowsInserted(index, index);
		}

		public void removeEntry(int index)
		{
			entries.remove(index);
			fireTableRowsDeleted(index, index);
		}

		public Item getEntry(int index)
		{
			return entries.get(index);
		}

		public void moveUp(int index)
		{
			entries.add(index - 1, entries.remove(index));
			fireTableRowsUpdated(index - 1, index);
		}

		public void moveDown(int index)
		{
			entries.add(index + 1, entries.remove(index));
			fireTableRowsUpdated(index, index + 1);
		}

		@Override
		public int getRowCount()
		{
			return entries.size();
		}

		public String getItemDescription(int row)
		{
			return BundleCache.getString(getEntry(row).getDescription());
		}

		@Override
		public int getColumnCount()
		{
			return 3;
		}

		@Override
		public String getColumnName(int column)
		{
			switch( column )
			{
				case 0:
					return FIRST;
				case 1:
					return SECOND;
				case 2:
					return THIRD;
				default:
					throw new IllegalStateException();
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			switch( columnIndex )
			{
				case 1:
					return Integer.class;
				default:
					return super.getColumnClass(columnIndex);
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			Item entry = entries.get(rowIndex);
			switch( columnIndex )
			{
				case 0:
					return CurrentLocale.get(entry.getName(), entry.getUuid());
				case 1:
					return entry.getVersion();
				case 2:
					return entry.getStatus();
				default:
					throw new IllegalStateException();
			}
		}
	}

	@Override
	public void validation() throws EditorException
	{
		// nothing to validate
	}
}
