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

package com.tle.admin.workflow.editor;

import static com.tle.common.security.SecurityConstants.getRecipient;
import static com.tle.common.security.SecurityConstants.getRecipientType;
import static com.tle.common.security.SecurityConstants.getRecipientValue;
import static com.tle.common.security.SecurityConstants.Recipient.GROUP;
import static com.tle.common.security.SecurityConstants.Recipient.ROLE;
import static com.tle.common.security.SecurityConstants.Recipient.USER;

import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.TableLayout;
import com.tle.admin.helper.GroupBox;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.SingleTargetChooser;
import com.tle.beans.entity.Schema;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.MultipleFinderControl;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.core.remoting.RemoteSchemaService;
import com.tle.core.remoting.RemoteUserService;
import com.tle.i18n.BundleCache;

/**
 * @author Nicholas Read
 */
public class ModeratorsTab extends JPanel implements ActionListener, ItemListener
{

	private final SchemaModel schemaModel = new SchemaModel();
	private final RemoteSchemaService schemaService;

	private static final long serialVersionUID = 1L;
	private GroupBox staticGroup;
	private GroupBox pathGroup;
	private ButtonGroup group;

	private JCheckBox unanimous;
	private JCheckBox allowEditing;

	private MultipleFinderControl finderControl;
	private JComboBox schemaList;
	private SingleTargetChooser dynamicUserPath;
	private static String keyPfx = "com.tle.admin.workflow.tool."; //$NON-NLS-1$

	public ModeratorsTab(ChangeDetector changeDetector, RemoteUserService userService, RemoteSchemaService schemaService)
	{
		this.schemaService = schemaService;
		setupGui(userService);
		setupChangeDetector(changeDetector);
	}

	private void setupGui(RemoteUserService userService)
	{

		unanimous = new JCheckBox(CurrentLocale.get("com.tle.admin.workflow.editor.moderatorstab.all")); //$NON-NLS-1$
		unanimous.addActionListener(this);

		allowEditing = new JCheckBox(CurrentLocale.get("com.tle.admin.workflow.editor.moderatorstab.allow")); //$NON-NLS-1$

		final int height1 = allowEditing.getPreferredSize().height;

		group = new ButtonGroup();

		finderControl = new MultipleFinderControl(userService);
		finderControl.addActionListener(this);

		staticGroup = GroupBox.withRadioButton(CurrentLocale.get(keyPfx + "modtab.choosestatic"), false); //$NON-NLS-1$
		staticGroup.getInnerPanel().setLayout(new GridLayout(1, 1));
		staticGroup.add(finderControl);
		staticGroup.addToGroup(group);

		dynamicUserPath = new SingleTargetChooser(schemaModel, null);

		schemaList = new JComboBox();
		schemaList.addItemListener(this);
		for( final NameValue schema : BundleCache.getNameUuidValues(schemaService.listAll()) )
		{
			schemaList.addItem(schema);
		}

		final JLabel dynamicUserPathLabel = new JLabel(
			CurrentLocale.get("com.tle.admin.workflow.editor.autoassigntab.label.target")); //$NON-NLS-1$

		final JLabel schemaLabel = new JLabel(
			CurrentLocale.get("com.tle.admin.workflow.editor.autoassigntab.label.schema")); //$NON-NLS-1$

		final int pathHeight = schemaList.getPreferredSize().height;
		final int pathLabelWidth = schemaLabel.getPreferredSize().width;
		final int[] pathRows = new int[]{pathHeight, pathHeight};
		final int[] pathCols = new int[]{pathLabelWidth, TableLayout.FILL};
		pathGroup = GroupBox.withRadioButton(CurrentLocale.get(keyPfx + "modtab.choosepath"), false); //$NON-NLS-1$
		pathGroup.getInnerPanel().setLayout(new TableLayout(pathRows, pathCols, 5, 5));
		pathGroup.add(schemaLabel, new Rectangle(0, 0, 1, 1));
		pathGroup.add(schemaList, new Rectangle(1, 0, 1, 1));
		pathGroup.add(dynamicUserPathLabel, new Rectangle(0, 1, 1, 1));
		pathGroup.add(dynamicUserPath, new Rectangle(1, 1, 1, 1));
		pathGroup.addToGroup(group);

		int pathGroupHeight = pathGroup.getPreferredSize().height;
		final int[] rows = {TableLayout.FILL, pathGroupHeight, height1, height1,};
		final int[] cols = {TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));
		setBorder(AppletGuiUtils.DEFAULT_BORDER);

		add(staticGroup, new Rectangle(0, 0, 1, 1));
		add(pathGroup, new Rectangle(0, 1, 1, 1));
		add(unanimous, new Rectangle(0, 2, 1, 1));
		add(allowEditing, new Rectangle(0, 3, 1, 1));

		updateUnanimousity();
	}

	private void setupChangeDetector(ChangeDetector changeDetector)
	{
		changeDetector.watch(unanimous);
		changeDetector.watch(allowEditing);
		finderControl.watch(changeDetector);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == finderControl )
		{
			updateUnanimousity();
		}
		else if( e.getSource() == unanimous )
		{
			updateAutoAssign();
		}
	}

	/**
	 * Ensures that the unanimous acceptance control is correctly
	 * enabled/disabled depending on other settings.
	 */
	private void updateUnanimousity()
	{
		boolean enabled = true;
		for( Iterator<String> iter = finderControl.save().iterator(); iter.hasNext() && enabled; )
		{
			if( getRecipientType(iter.next()) == ROLE )
			{
				enabled = false;
			}
		}

		unanimous.setEnabled(enabled);
		if( !enabled )
		{
			unanimous.setSelected(false);
		}

		updateAutoAssign();
	}

	/**
	 * Ensures that the auto-assign control is correctly enabled/disabled
	 * depending on other settings.
	 */
	private void updateAutoAssign()
	{
		boolean value = unanimous.isEnabled() && unanimous.isSelected();

		for( UnanimousityChangeListener l : listenerList.getListeners(UnanimousityChangeListener.class) )
		{
			l.unanimousityChanged(value);
		}
	}

	/**
	 * Loads the given workflow item.
	 */
	public void load(WorkflowItem item)
	{
		unanimous.setSelected(item.isUnanimousacceptance());
		allowEditing.setSelected(item.isAllowEditing());

		String userPath = item.getUserPath();
		boolean found = false;
		final String itemSchemaUuid = item.getUserSchemaUuid();
		for( int i = 0; i < schemaList.getItemCount(); i++ )
		{
			final String uuid = ((NameValue) schemaList.getItemAt(i)).getValue();
			if( uuid != null && uuid.equals(itemSchemaUuid) )
			{
				schemaList.setSelectedIndex(i);
				found = true;
				break;
			}
		}
		if( !found )
		{
			schemaList.setSelectedIndex(0);
		}
		if( !Check.isEmpty(userPath) )
		{
			dynamicUserPath.setTarget(userPath);
			group.setSelected(pathGroup.getButtonModel(), true);
		}
		else
		{
			List<String> moderators = new ArrayList<String>();

			if( item.getUsers() != null )
			{
				for( String user : item.getUsers() )
				{
					moderators.add(getRecipient(USER, user));
				}
			}

			if( item.getGroups() != null )
			{
				for( String grp : item.getGroups() )
				{
					moderators.add(getRecipient(GROUP, grp));
				}
			}

			if( item.getRoles() != null )
			{
				for( String role : item.getRoles() )
				{
					moderators.add(getRecipient(ROLE, role));
				}
			}

			finderControl.load(moderators);
			group.setSelected(staticGroup.getButtonModel(), true);
		}

		updateUnanimousity();
	}

	/**
	 * Saves the current settings to the given workflow item.
	 */
	public void save(WorkflowItem item)
	{
		item.setUnanimousacceptance(unanimous.isEnabled() && unanimous.isSelected());
		item.setAllowEditing(allowEditing.isSelected());

		if( staticGroup.isSelected() )
		{
			Set<String> users = new HashSet<String>();
			Set<String> groups = new HashSet<String>();
			Set<String> roles = new HashSet<String>();

			for( String result : finderControl.save() )
			{
				String value = getRecipientValue(result);
				switch( getRecipientType(result) )
				{
					case USER:
						users.add(value);
						break;
					case GROUP:
						groups.add(value);
						break;
					case ROLE:
						roles.add(value);
						break;
					default:
						throw new IllegalStateException("We should never reach here"); //$NON-NLS-1$
				}
			}

			if( users.isEmpty() )
			{
				users = null;
			}

			if( groups.isEmpty() )
			{
				groups = null;
			}

			if( roles.isEmpty() )
			{
				roles = null;
			}

			item.setUsers(users);
			item.setGroups(groups);
			item.setRoles(roles);
			item.setUserPath(null);
		}
		else
		{
			item.setUsers(null);
			item.setGroups(null);
			item.setRoles(null);
			item.setUserPath(dynamicUserPath.getTarget());
			item.setUserSchemaUuid(((NameValue) schemaList.getSelectedItem()).getValue());
		}
	}

	public void addUnanimousityChangeListener(UnanimousityChangeListener l)
	{
		listenerList.add(UnanimousityChangeListener.class, l);
	}

	/**
	 * @author Nicholas Read
	 */
	public interface UnanimousityChangeListener extends EventListener
	{
		void unanimousityChanged(boolean newValue);
	}

	/**
	 * Schema dropdown
	 */
	@Override
	public void itemStateChanged(final ItemEvent e)
	{
		final String uuid = ((NameValue) schemaList.getSelectedItem()).getValue();
		final Schema selected = schemaService.get(schemaService.identifyByUuid(uuid));
		schemaModel.loadSchema(selected.getDefinitionNonThreadSafe());
	}
}
