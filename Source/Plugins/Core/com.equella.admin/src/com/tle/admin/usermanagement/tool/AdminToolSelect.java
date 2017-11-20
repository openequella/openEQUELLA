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

package com.tle.admin.usermanagement.tool;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;

import com.dytech.gui.JHoverButton;
import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.AdvancedSwingWorker;
import com.tle.admin.AdminTool;
import com.tle.admin.Driver;
import com.tle.admin.security.tree.OverrideRenderer;
import com.tle.admin.usermanagement.UMWConfig;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteUserService;

@SuppressWarnings("nls")
public abstract class AdminToolSelect extends AdminTool implements ActionListener, MouseListener
{
	private static final String CONFIGURE_ICON = "/icons/edit.gif";
	private static final String SELECT_WRAPPER_ICON = "/icons/edit.gif";

	private JPanel everything;
	private JPanel buttons;

	private String toolName;

	protected JTable wrapperList;
	public WrapperTableModel wrapperModel;

	protected JHoverButton selectButton;
	protected JHoverButton selectWrapperButton;

	public AdminToolSelect()
	{
		super();
	}

	@Override
	public void setup(Set<String> grantedPrivileges, String name)
	{
		this.toolName = name;
		JComponent bottom = createWrapperList();

		bottom.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		everything = new JPanel(new BorderLayout(5, 5));
		everything.add(createHeading(), BorderLayout.NORTH);
		everything.add(bottom, BorderLayout.CENTER);

		fillLists();
	}

	private JComponent createWrapperList()
	{
		wrapperModel = new WrapperTableModel();
		wrapperList = new JTable();

		// Dodgy hack to get Checkboxes in table to display
		// with native L&F without changing to flatter L&F.
		// There are probably millions of ways to avoid this,
		// but I can't find any.
		// see Jira Defect TLE-2082 :
		// http://apps.dytech.com.au/jira/browse/TLE-2082
		UIManager.addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				try
				{
					wrapperList.updateUI();
				}
				catch( Exception ex )
				{
					// Ignore
				}
			}
		});

		wrapperList.setModel(wrapperModel);
		wrapperList.getColumnModel().getColumn(1).setCellRenderer(new OverrideRenderer());

		wrapperList.addMouseListener(this);
		wrapperList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane scroll = new JScrollPane(wrapperList);
		JPanel panel = new JPanel(new TableLayout(new int[]{TableLayout.FILL, TableLayout.PREFERRED},
			new int[]{TableLayout.FILL}));
		panel.add(scroll, new Rectangle(0, 0, 1, 1));
		panel.add(createWrapperSelectButtons(), new Rectangle(0, 1, 1, 1));

		return panel;
	}

	protected JPanel createHeading()
	{
		JLabel heading = new JLabel(toolName);
		JPanel headingPanel = new JPanel();
		headingPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		headingPanel.add(heading);

		return headingPanel;
	}

	protected JPanel createSelectButtons()
	{
		Icon configureIcon = new ImageIcon(AdminToolSelect.class.getResource(CONFIGURE_ICON));

		selectButton = new JHoverButton(CurrentLocale.get("com.tle.admin.gui.admintoolselect.configure"), configureIcon);

		selectButton.setBorderPainted(false);
		selectButton.addActionListener(this);

		buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		buttons.add(selectButton);

		return buttons;
	}

	protected JPanel createWrapperSelectButtons()
	{
		Icon configureIcon = new ImageIcon(AdminToolSelect.class.getResource(SELECT_WRAPPER_ICON));
		selectWrapperButton = new JHoverButton(CurrentLocale.get("com.tle.admin.gui.admintoolselect.configure"),
			configureIcon);

		selectWrapperButton.setBorderPainted(false);
		selectWrapperButton.addActionListener(this);

		buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		buttons.add(selectWrapperButton);

		return buttons;
	}

	@Override
	public void toolSelected()
	{
		managementPanel.add(everything);
	}

	protected abstract void fillLists();

	/**
	 * Called when the add button is pressed. If the returned object is null
	 * then no Object is added.
	 */
	protected abstract void onConfigure(Object selected, boolean select);

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == selectWrapperButton )
		{
			if( wrapperList.getSelectedRow() < 0 )
			{
				Driver.displayInformation(parentFrame,
					CurrentLocale.get("com.tle.admin.gui.admintoolselect.mustselect"));
			}
			else
			{
				configureWrapper();
			}
		}

	}

	private void configureWrapper()
	{
		onConfigure(wrapperModel.get(wrapperList.getSelectedRow()), false);
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		if( e.getClickCount() == 2 )
		{
			configureWrapper();
		}
		else
		{
			int col = wrapperList.columnAtPoint(e.getPoint());
			int row = wrapperList.rowAtPoint(e.getPoint());
			if( wrapperModel.isCellEditable(row, col) )
			{
				editingStopped();
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		// We don't care about this event
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		// We don't care about this event
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		// We don't care about this event
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		// We don't care about this event
	}

	protected static final List<String> COLUMNS = Collections.unmodifiableList(Arrays.asList(
		CurrentLocale.get("com.tle.admin.gui.admintoolselect.name"),
		CurrentLocale.get("com.tle.admin.gui.admintoolselect.enabled")));

	protected class WrapperTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 1L;
		private final List<UMWConfig> list;

		@Override
		public String getColumnName(int column)
		{
			return COLUMNS.get(column);
		}

		public WrapperTableModel()
		{
			list = new ArrayList<UMWConfig>();
		}

		public UMWConfig get(int selectedRow)
		{
			return list.get(selectedRow);
		}

		public Iterator<UMWConfig> iterator()
		{
			return list.iterator();
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount()
		{
			return COLUMNS.size();
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount()
		{
			return list.size();
		}

		@Override
		public Class<?> getColumnClass(int c)
		{
			return getValueAt(0, c).getClass();
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return columnIndex == 1;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			UMWConfig config = get(rowIndex);
			Object value = null;
			if( columnIndex == 0 )
			{
				value = config.getName();
			}
			else
			{
				value = config.isEnabled();
			}
			return value;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			UMWConfig config = get(rowIndex);
			if( columnIndex == 1 )
			{
				boolean b = ((Boolean) aValue).booleanValue();
				if( b )
				{
					if( JOptionPane.showConfirmDialog(parentFrame,
						"You have chosen to enable this plug-in, but this may not have\nbeen"
							+ " your intention.  Do you want to enable this plug-in?", "Enable Plug-in?",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION )
					{
						config.setEnabled(true);
						editingStopped();
					}
				}
				else
				{
					String[] locButtons = {"Disable plug-in", "Configure plug-in settings", "Cancel",};

					int result = JOptionPane.showOptionDialog(parentFrame,
						"You have chosen to disable this plug-in, but this may not have been your intention.\n"
							+ "Please confirm what you are trying to do by selecting from the following actions:",
						"Disable Plug-in?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
						locButtons, locButtons[2]);

					if( result == JOptionPane.YES_OPTION )
					{
						config.setEnabled(false);
						editingStopped();
					}
					else if( result == JOptionPane.NO_OPTION )
					{
						SwingUtilities.invokeLater(new Runnable()
						{
							@Override
							public void run()
							{
								configureWrapper();
							}
						});
					}
				}
			}
			fireTableCellUpdated(rowIndex, columnIndex);
		}

		public void addElement(UMWConfig umw)
		{
			list.add(umw);
		}
	}

	public void editingStopped()
	{
		int row = wrapperList.getSelectedRow();
		final UMWConfig config = wrapperModel.get(row);

		ClientService clientService = driver.getClientService();
		final RemoteUserService userService = clientService.getService(RemoteUserService.class);
		new AdvancedSwingWorker<Object>()
		{
			@Override
			public Object construct() throws Exception
			{
				UserManagementSettings xml = userService.getPluginConfig(config.getSettingsClass());
				xml.setEnabled(config.isEnabled());
				userService.setPluginConfig(xml);
				return null;
			}

			@Override
			public void exception()
			{
				Driver.displayInformation(parentFrame, CurrentLocale.get("com.tle.admin.gui.admintoolselect.error")); //$NON-NLS-1$
			}

		}.start();
	}

	public void addWrapperElement(UMWConfig o)
	{
		wrapperModel.addElement(o);
	}
}
