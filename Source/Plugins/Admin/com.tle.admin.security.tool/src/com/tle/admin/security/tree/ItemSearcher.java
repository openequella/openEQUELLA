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

package com.tle.admin.security.tree;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.gui.common.actions.SearchAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.beans.item.Item;
import com.tle.common.Check;
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
@SuppressWarnings("nls")
public class ItemSearcher extends JPanel implements ActionListener, ListSelectionListener
{
	private static final long serialVersionUID = 1L;

	private static final int RESULTS_PER_PAGE = 10;

	private final RemoteAdminService adminService;
	private final TabManager tabManager;

	private int start;
	private int total;

	private JTextField query;
	private JButton next;
	private JButton prev;
	private JLabel countLabel;
	private MyTableModel model;
	private JTable table;

	private Item currentSelection;

	public ItemSearcher(TabManager tabManager, ClientService clientService)
	{
		this.tabManager = tabManager;
		adminService = clientService.getService(RemoteAdminService.class);

		setupGui();
	}

	private void updateEditor()
	{
		final Item newSelection = getSelectedItem();
		if( Objects.equals(newSelection, currentSelection) )
		{
			return;
		}

		if( currentSelection != null && tabManager.hasDetectedChanges() )
		{
			String title = CurrentLocale.get("security.tree.prompt.savechanges.title");
			String msg = CurrentLocale.get("security.tree.prompt.savechanges.body");
			Object[] buttons = {CurrentLocale.get("prompts.save"), CurrentLocale.get("prompts.discard"),
					CurrentLocale.get("prompts.cancel"),};

			int result = JOptionPane.showOptionDialog(this, msg, title, JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, buttons, buttons[2]);

			if( result != JOptionPane.NO_OPTION )
			{
				if( result == JOptionPane.YES_OPTION )
				{
					GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
					{
						@Override
						public Object construct() throws Exception
						{
							tabManager.saveTabs();
							return null;
						}

						@Override
						public void finished()
						{
							int row = model.indexOf(newSelection);
							table.getSelectionModel().setSelectionInterval(row, row);
							currentSelection = newSelection;
							tabManager.updateTabs(currentSelection);
						}
					};
					worker.setComponent(this);
					worker.start();
				}

				// Reset to previous selection
				int row = model.indexOf(currentSelection);
				table.getSelectionModel().setSelectionInterval(row, row);
				return;
			}
		}

		currentSelection = newSelection;
		tabManager.updateTabs(currentSelection);
	}

	private void setupGui()
	{
		JLabel label = new JLabel(CurrentLocale.get("com.tle.admin.security.tree.itemsearcher.search"));

		query = new JTextField();
		query.setAction(searchAction);

		JButton search = new JButton(searchAction);

		model = new MyTableModel();
		table = new JTable(model)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getToolTipText(MouseEvent event)
			{
				String tip = null;
				int rowIndex = rowAtPoint(event.getPoint());
				if( rowIndex != -1 )
				{
					tip = ItemSearcher.this.model.getItemDescription(rowIndex);
				}

				if( tip == null || tip.trim().length() == 0 )
				{
					tip = CurrentLocale.get("com.tle.admin.security.tree.itemsearcher.nodesc");
				}
				else if( tip.length() > 100 )
				{
					tip = tip.substring(0, 100) + "...";
				}

				return tip;
			}
		};

		table.getSelectionModel().addListSelectionListener(this);
		JScrollPane scroller = new JScrollPane(table);

		countLabel = new JLabel();
		next = new JButton(">");
		prev = new JButton("<");

		next.setEnabled(false);
		prev.setEnabled(false);

		next.addActionListener(this);
		prev.addActionListener(this);

		final int width1 = label.getPreferredSize().width;
		final int width2 = search.getPreferredSize().width;
		final int height = search.getPreferredSize().height;
		final int[] rows = {height, TableLayout.FILL, height,};
		final int[] cols = {width1, TableLayout.FILL, width2, width2,};

		setLayout(new TableLayout(rows, cols));
		setBorder(AppletGuiUtils.DEFAULT_BORDER);

		add(label, new Rectangle(0, 0, 1, 1));
		add(query, new Rectangle(1, 0, 2, 1));
		add(search, new Rectangle(3, 0, 1, 1));
		add(scroller, new Rectangle(0, 1, 4, 1));
		add(countLabel, new Rectangle(0, 2, 2, 1));
		add(prev, new Rectangle(2, 2, 1, 1));
		add(next, new Rectangle(3, 2, 1, 1));

		updateEditor();
	}

	private Item getSelectedItem()
	{
		Item result = null;
		if( table.getSelectedRowCount() > 0 )
		{
			result = model.getEntry(table.getSelectedRow());
		}
		return result;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == next )
		{
			start += RESULTS_PER_PAGE;
			doSearch();
		}
		else if( e.getSource() == prev )
		{
			start -= RESULTS_PER_PAGE;
			doSearch();
		}
	}

	private final TLEAction searchAction = new SearchAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			start = 0;
			doSearch();
		}
	};

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		updateEditor();
	}

	private void doSearch()
	{
		model.setEntries(null);

		final String queryTerms = query.getText();
		GlassSwingWorker<?> worker = new GlassSwingWorker<SearchResults<Item>>()
		{
			@Override
			public SearchResults<Item> construct() throws Exception
			{
				Search search = new VeryBasicSearch(queryTerms);
				SearchResults<Item> results = adminService.searchReducedItems(search, start, RESULTS_PER_PAGE);

				BundleCache.ensureCached(results.getResults(), BundleCache.ITEM_NAME_GETTER);
				BundleCache.ensureCached(results.getResults(), BundleCache.ITEM_DESCRIPTION_GETTER);

				return results;
			}

			@Override
			public void finished()
			{
				SearchResults<Item> results = get();
				total = results.getAvailable();
				model.setEntries(results.getResults());

				doInAnyCase();
			}

			@Override
			public void exception()
			{
				Driver.displayInformation(ItemSearcher.this,
					CurrentLocale.get("com.tle.admin.security.tree.itemsearcher.invalid"));
				getException().printStackTrace();

				doInAnyCase();
			}

			public void doInAnyCase()
			{
				final int startDisplay = total == 0 ? 0 : (start + 1);
				countLabel.setText(CurrentLocale.get("com.tle.admin.security.tree.itemsearcher.results", new Object[]{
						startDisplay, (start + model.getRowCount()), total}));
				next.setEnabled(start + RESULTS_PER_PAGE < total);
				prev.setEnabled(start > 0);
			}
		};
		worker.setComponent(this);
		worker.start();
	}

	private static class MyTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 1L;
		private List<Item> entries;

		public MyTableModel()
		{
			super();
		}

		public void setEntries(List<Item> entries)
		{
			this.entries = entries;
			fireTableDataChanged();
		}

		public Item getEntry(int index)
		{
			return entries.get(index);
		}

		public int indexOf(Item item)
		{
			return entries.indexOf(item);
		}

		@Override
		public int getRowCount()
		{
			return entries == null ? 0 : entries.size();
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
					return CurrentLocale.get("com.tle.admin.security.tree.itemsearcher.name");
				case 1:
					return CurrentLocale.get("com.tle.admin.security.tree.itemsearcher.version");
				case 2:
					return CurrentLocale.get("com.tle.admin.security.tree.itemsearcher.status");
				default:
					throw new IllegalStateException("We should never reach here");
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
					String name = BundleCache.getString(entry.getName());
					if( Check.isEmpty(name) )
					{
						name = entry.getUuid();
					}
					return name;
				case 1:
					return entry.getVersion();
				case 2:
					return entry.getStatus();
				default:
					throw new IllegalStateException("We should never reach here");
			}
		}
	}
}
