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

package com.tle.admin.usermanagement.ldap;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

import com.dytech.gui.TableLayout;
import com.google.common.base.Objects;
import com.tle.admin.usermanagement.ldap.LDAPDirectoryTree.TreeNode2;
import com.tle.beans.usermanagement.standard.LDAPSettings;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteLDAPService;

/**
 * @author cofarrell
 */
public class LDAPMappingPanel extends AbstractLDAPPanel implements ActionListener, ItemListener
{
	private static final long serialVersionUID = 1L;

	public static final int NAME = 0;
	public static final int DISPLAY = 1;
	public static final int VALUE = 2;

	@SuppressWarnings("nls")
	protected static final List<String> ATTRIBUTES_NAMES = Collections.unmodifiableList(Arrays.asList("username", "id",
		"groupId", "groupName", "surname", "givenname", "email", "memberOf", "member", "memberKey"));

	private LDAPDirectoryTree directory;

	private AttributeTableModel attributeModel;
	private JTable attributeTable;
	private JScrollPane groupScrollPane;

	private JButton left;
	private JButton right;

	private String dragString = "";

	private JTextField personField;
	private JTextField groupField;
	private JComboBox presetsCombo;
	private JButton manageDns;

	private RemoteLDAPService ldapService;

	public LDAPMappingPanel(ClientService services)
	{
		createGUI();
		ldapService = services.getService(RemoteLDAPService.class);
	}

	private void setupChangeDetector()
	{
		changeDetector.watch(attributeModel);
	}

	private void createGUI()
	{
		JPanel all = new JPanel(new BorderLayout(5, 5));
		all.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		all.add(createTop(), BorderLayout.NORTH);
		all.add(createMiddle(), BorderLayout.CENTER);

		setupChangeDetector();

		setLayout(new BorderLayout());
		add(all);
		setSize(800, 600);
	}

	@Override
	@SuppressWarnings("nls")
	public String getTabName()
	{
		return s("mappings");
	}

	@Override
	public void showPanel() throws Exception
	{
		super.showPanel();

		updateButtonText();

		directory.setLDAPSettings(settings);
		directory.setLdapService(ldapService);
		directory.reload();
	}

	@SuppressWarnings("nls")
	private JComponent createTop()
	{
		JLabel personLabel = new JLabel(s("personal"));
		JLabel groupLabel = new JLabel(s("group"));
		JLabel presetsLabel = new JLabel(s("presets"));

		personField = new JTextField();
		groupField = new JTextField();
		presetsCombo = new JComboBox(LDAPPresets.getAll());
		presetsCombo.addItemListener(this);

		final int width1 = personLabel.getPreferredSize().width;

		final int[] rows = {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED,};
		final int[] cols = {width1, TableLayout.FILL,};

		JPanel all = new JPanel(new TableLayout(rows, cols, 5, 5));
		all.add(presetsLabel, new Rectangle(0, 0, 1, 1));
		all.add(presetsCombo, new Rectangle(1, 0, 1, 1));
		all.add(personLabel, new Rectangle(0, 1, 1, 1));
		all.add(personField, new Rectangle(1, 1, 1, 1));
		all.add(groupLabel, new Rectangle(0, 2, 1, 1));
		all.add(groupField, new Rectangle(1, 2, 1, 1));
		all.add(groupLabel, new Rectangle(0, 3, 1, 1));
		all.add(groupField, new Rectangle(1, 3, 1, 1));

		return all;
	}

	@SuppressWarnings("nls")
	private JComponent createMiddle()
	{
		manageDns = new JButton(" ");
		manageDns.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ManageDNsDialog d = new ManageDNsDialog(ldapService);
				d.show(manageDns, settings);
				updateButtonText();
				directory.reload();
			}
		});

		left = new JButton(new ImageIcon(LDAPMappingPanel.class.getResource("/icons/left.gif")));
		right = new JButton(new ImageIcon(LDAPMappingPanel.class.getResource("/icons/right.gif")));

		left.addActionListener(this);
		right.addActionListener(this);

		JComponent mapping = createMapping();

		directory = new LDAPDirectoryTree();
		directory.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				TreePath path = null;
				if( directory.isSelectionEmpty() )
				{
					path = directory.getPathForLocation(e.getX(), e.getY());
				}
				else
				{
					path = directory.getSelectionPath();
				}

				if( path != null )
				{
					TreeNode2 tmp = (TreeNode2) path.getLastPathComponent();
					dragString = tmp.toString();
					setCursor(DragSource.DefaultCopyNoDrop);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				if( canDrag(e, getSelectedComponent()) != null )
				{
					right();
				}
				dragString = "";
			}
		});
		directory.addMouseMotionListener(new MouseMotionAdapter()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
				Point p = canDrag(e, getSelectedComponent());
				if( p != null )
				{
					JTable table = getSelectedTable();
					p = canDrag(e, table);

					int row;
					if( p != null )
					{
						row = table.rowAtPoint(p);
					}
					else
					{
						row = table.getRowCount();
					}

					table.changeSelection(row, 0, false, true);
					setCursor(DragSource.DefaultCopyDrop);
				}
				else if( dragString.length() != 0 )
				{
					setCursor(DragSource.DefaultCopyNoDrop);
				}
			}
		});

		ToolTipManager.sharedInstance().registerComponent(directory);
		JScrollPane treeScrollPane = new JScrollPane(directory);

		final int height1 = manageDns.getPreferredSize().height;
		final int height2 = left.getPreferredSize().height;
		final int width1 = left.getPreferredSize().width;

		final int[] rows = {height1, TableLayout.FILL, height2, height2, TableLayout.FILL,};
		final int[] cols = {TableLayout.FILL, width1, TableLayout.FILL,};

		JPanel all = new JPanel(new TableLayout(rows, cols, 5, 5));
		all.add(manageDns, new Rectangle(0, 0, 1, 1));
		all.add(treeScrollPane, new Rectangle(0, 1, 1, 4));
		all.add(mapping, new Rectangle(2, 0, 1, 5));
		all.add(left, new Rectangle(1, 2, 1, 1));
		all.add(right, new Rectangle(1, 3, 1, 1));

		return all;
	}

	private JComponent createMapping()
	{
		attributeModel = new AttributeTableModel();
		attributeTable = new JTable(attributeModel);

		attributeTable.setModel(attributeModel);
		attributeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		attributeTable.changeSelection(0, 0, false, true);

		return new JScrollPane(attributeTable);
	}

	@Override
	public void applySettings() throws Exception
	{
		settings.setPersonObject(personField.getText());
		settings.setGroupObject(groupField.getText());
		settings.setAttributes(attributeModel.getAttributes());
	}

	public JTable getSelectedTable()
	{
		return attributeTable;
	}

	public JComponent getSelectedComponent()
	{
		JComponent component = getSelectedTable();
		if( Objects.equal(component, attributeTable) )
		{
			return attributeTable;
		}

		return groupScrollPane;
	}

	public void right()
	{
		TreePath path = directory.getSelectionPath();
		if( path == null )
		{
			return;
		}
		TreeNode2 tmp = (TreeNode2) path.getLastPathComponent();
		JTable table = getSelectedTable();
		int row = table.getSelectedRow();

		table.getModel().setValueAt(tmp.toString(), row, 1);

		table.updateUI();
	}

	// / ************ LISTENERS **************//

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == left )
		{
			JTable table = getSelectedTable();
			int row = table.getSelectedRow();
			attributeModel.setValueAt("", row, 1);
			table.updateUI();
		}
		else if( e.getSource() == right )
		{
			right();
		}
	}

	@SuppressWarnings("nls")
	class AttributeTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 1L;

		private final String[] COLUMNS = {s("name"), s("value")};
		private Map<String, String> attributes = new HashMap<String, String>();

		public AttributeTableModel()
		{
			for( String element : ATTRIBUTES_NAMES )
			{
				attributes.put(element, "");
			}
		}

		public Map<String, String> getAttributes()
		{
			return attributes;
		}

		public void setAttributes(Map<String, String> attributes)
		{
			this.attributes.putAll(attributes);
			fireTableCellUpdated(0, attributes.size() - 1);
		}

		@Override
		public String getColumnName(int col)
		{
			return COLUMNS[col];
		}

		@Override
		public int getRowCount()
		{
			return getAttributeDisplay().length;
		}

		@Override
		public int getColumnCount()
		{
			return COLUMNS.length;
		}

		@Override
		public Object getValueAt(int row, int col)
		{
			String value = null;
			if( col == 0 )
			{
				value = getAttributeDisplay()[row];
			}
			else if( col == 1 )
			{
				value = attributes.get(ATTRIBUTES_NAMES.get(row));
			}
			return value;
		}

		@Override
		public boolean isCellEditable(int row, int col)
		{
			return col == 1;
		}

		@Override
		public void setValueAt(Object value, int row, int col)
		{
			attributes.put(ATTRIBUTES_NAMES.get(row), (String) value);
			fireTableCellUpdated(row, col);
		}
	}

	public Point canDrag(MouseEvent e, JComponent table)
	{
		Point p2 = table.getLocationOnScreen();
		Point p1 = directory.getLocationOnScreen();
		Point p = e.getPoint();

		Point p3 = new Point();
		p3.x = p1.x + p.x - p2.x;
		p3.y = p1.y + p.y - p2.y;

		if( table.contains(p3) && dragString.length() != 0 )
		{
			return p3;
		}
		else
		{
			return null;
		}
	}

	public void save(LDAPSettings settings)
	{
		settings.setAttributes(attributeModel.getAttributes());
		settings.setPersonObject(personField.getText());
		settings.setGroupObject(groupField.getText());
		settings.setBases(settings.getBases());
	}

	public void load(LDAPSettings ls)
	{
		this.settings = ls;
		attributeModel.setAttributes(ls.getAttributes());
		personField.setText(ls.getPersonObject());
		groupField.setText(ls.getGroupObject());
		updateButtonText();
	}

	@Override
	public void itemStateChanged(ItemEvent e)
	{
		if( presetsCombo.getSelectedIndex() > 0 )
		{
			setPreset((LDAPPresets.Preset) presetsCombo.getSelectedItem());
		}
	}

	private void setPreset(LDAPPresets.Preset preset)
	{
		groupField.setText(preset.getGroupObject());
		personField.setText(preset.getUserObject());

		attributeModel.setAttributes(preset.getValues());
		attributeTable.repaint();
	}

	@SuppressWarnings("nls")
	public String[] getAttributeDisplay()
	{
		return new String[]{s("username"), s("id"), s("groupid"), s("groupname"), s("familyname"), s("firstname"),
				s("email"), s("memberof"), s("groupmember"), s("memberkey")};
	}

	@SuppressWarnings("nls")
	private void updateButtonText()
	{
		manageDns.setText(s(Check.isEmpty(settings.getBases()) ? "dns.add" : "dns.manage"));
	}

	private static String s(String keypart)
	{
		return CurrentLocale.get("com.tle.admin.usermanagement.standard.ldap.mapping." + keypart);
	}
}
