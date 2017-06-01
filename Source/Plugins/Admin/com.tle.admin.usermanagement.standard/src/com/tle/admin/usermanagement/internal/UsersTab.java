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

package com.tle.admin.usermanagement.internal;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.dytech.common.text.NumberStringComparator;
import com.dytech.gui.TableLayout;
import com.dytech.gui.filter.FilterList;
import com.dytech.gui.filter.FilterModel;
import com.tle.admin.common.gui.actions.BulkAction;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.common.JChangeDetectorPanel;
import com.tle.admin.gui.common.actions.AddAction;
import com.tle.admin.gui.common.actions.RemoveAction;
import com.tle.beans.user.TLEUser;
import com.tle.common.Format;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteTLEGroupService;
import com.tle.core.remoting.RemoteTLEUserService;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class UsersTab extends JChangeDetectorPanel implements ListSelectionListener
{
	private static final long serialVersionUID = 1L;

	protected final RemoteTLEUserService userService;
	protected final RemoteTLEGroupService groupService;
	protected final RemoteUserService userCacheService;

	protected UserDetailsPanel details;
	protected FilterList<TLEUser> filterList;

	private final BulkUserImportAction bulkAction;
	private final AddUserAction addAction;
	private final RemoveUserAction removeAction;

	public UsersTab(RemoteTLEUserService userService, RemoteTLEGroupService groupService,
		RemoteUserService userCacheService)
	{
		this.userService = userService;
		this.groupService = groupService;
		this.userCacheService = userCacheService;
		bulkAction = new BulkUserImportAction();
		addAction = new AddUserAction();
		removeAction = new RemoveUserAction();

		setupGui();
	}

	private void setupGui()
	{
		JButton add = new JButton(addAction);
		JButton remove = new JButton(removeAction);
		JButton bulk = new JButton(bulkAction);

		filterList = new FilterList<TLEUser>(new FilterModel<TLEUser>()
		{
			static final String WILD = "*"; //$NON-NLS-1$

			@Override
			public List<TLEUser> search(final String pattern)
			{
				String wildPattern = pattern;
				if( !wildPattern.startsWith(WILD) )
				{
					wildPattern = WILD + wildPattern;
				}
				if( !wildPattern.endsWith(WILD) )
				{
					wildPattern += WILD;
				}

				List<TLEUser> results = userService.searchUsers(wildPattern, null, true);
				Collections.sort(results, new NumberStringComparator<TLEUser>()
				{
					private static final long serialVersionUID = 1L;

					@Override
					public String convertToString(TLEUser t)
					{
						return t.getUsername();
					}
				});

				return results;
			}
		});
		filterList.setListCellRenderer(new DefaultListCellRenderer()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public java.awt.Component getListCellRendererComponent(javax.swing.JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus)
			{
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				setText(Format.format((TLEUser) value));
				return this;
			}
		});
		filterList.setSortingComparator(new NumberStringComparator<TLEUser>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String convertToString(TLEUser t)
			{
				return Format.format(t);
			}
		});
		filterList.addListSelectionListener(this);

		details = new UserDetailsPanel(userService, userCacheService);

		final int width1 = remove.getPreferredSize().width;
		final int height1 = remove.getPreferredSize().height;

		final int[] rows = {TableLayout.FILL, height1,};
		final int[] cols = {width1, width1, width1, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));
		setBorder(AppletGuiUtils.DEFAULT_BORDER);

		add(new JIgnoreChangeComponent(filterList), new Rectangle(0, 0, 3, 1));
		add(add, new Rectangle(0, 1, 1, 1));
		add(remove, new Rectangle(1, 1, 1, 1));
		add(bulk, new Rectangle(2, 1, 1, 1));
		add(details, new Rectangle(3, 0, 1, 2));

		updateGui();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event
	 * .ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if( e.getSource() == filterList )
		{
			details.loadUser(filterList.getSelectedValue());
		}
		updateGui();
	}

	private void updateGui()
	{
		removeAction.update();
	}

	public void save() throws EditorException
	{
		details.save();
	}

	protected class AddUserAction extends AddAction
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			filterList.getModel().clear();
			details.loadUser(new TLEUser());
		}
	}

	protected class RemoveUserAction extends RemoveAction
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			int result = JOptionPane.showConfirmDialog(UsersTab.this,
				CurrentLocale.get("com.tle.admin.usermanagement.internal.userstab.sure")); //$NON-NLS-1$
			if( result == JOptionPane.YES_OPTION )
			{
				for( TLEUser user : filterList.getSelectedValues() )
				{
					userService.delete(user.getUuid());
					filterList.getModel().remove(user);
				}
				details.loadUser(null);
			}
		}

		@Override
		public void update()
		{
			setEnabled(filterList.getSelectedValue() != null);
		}
	}

	protected class BulkUserImportAction extends BulkAction<TLEUser>
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected void refresh()
		{
			filterList.getModel().clear();
		}

		@Override
		protected void bulkImport(byte[] array, boolean override) throws Exception
		{
			new UserBulkImporter(userService, groupService).bulkImport(array, override);
		}

		@Override
		protected Component getParent()
		{
			return UsersTab.this.getParent();
		}
	}
}
