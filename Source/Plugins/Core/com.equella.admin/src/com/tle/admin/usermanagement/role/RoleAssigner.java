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

package com.tle.admin.usermanagement.role;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.UUID;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.dytech.gui.ChangeDetector;
import com.dytech.gui.TableLayout;
import com.tle.admin.gui.common.JChangeDetectorPanel;
import com.tle.admin.gui.common.ListWithView;
import com.tle.admin.gui.common.ListWithViewInterface;
import com.tle.beans.ump.RoleMapping;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.ExpressionBuilderFinder;
import com.tle.common.recipientselector.RecipientFilter;
import com.tle.core.remoting.RemoteUserService;

public class RoleAssigner extends JChangeDetectorPanel
{
	private static final long serialVersionUID = 1L;

	private final RemoteUserService userService;

	private ListWithView<RoleMapping, Editor> listWithView;

	public RoleAssigner(RemoteUserService userService)
	{
		this.userService = userService;

		init();
	}

	private void init()
	{
		listWithView = new ListWithView<RoleMapping, Editor>()
		{
			private static final long serialVersionUID = 1L;
			private final Editor editor = new Editor();

			@Override
			protected RoleMapping createElement()
			{
				RoleMapping mapping = new RoleMapping();
				mapping.setId(UUID.randomUUID().toString());
				mapping.setName(CurrentLocale.get("com.tle.admin.usermanagement.role.roleassigner.untitled")); //$NON-NLS-1$
				return mapping;
			}

			@Override
			protected Editor getEditor(RoleMapping currentSelection)
			{
				if( currentSelection == null )
				{
					return null;
				}
				else
				{
					return editor;
				}
			}
		};

		setLayout(new GridLayout(1, 1));
		add(listWithView);
		watch(listWithView);
	}

	public List<RoleMapping> getRoleMappings()
	{
		return listWithView.save();
	}

	public void setRoleMappings(List<RoleMapping> roles)
	{
		listWithView.load(roles);
	}

	/**
	 * Editor for a role.
	 * 
	 * @author Nicholas Read
	 */
	class Editor extends JPanel implements ListWithViewInterface<RoleMapping>
	{
		private static final long serialVersionUID = 1L;
		private JTextField name;
		private ExpressionBuilderFinder finder;
		private ChangeDetector changeDetector;

		@Override
		public Component getComponent()
		{
			return this;
		}

		@Override
		public void setup()
		{
			if( name != null )
			{
				// Already initialised
				return;
			}

			JLabel nameLabel = new JLabel(CurrentLocale.get("com.tle.admin.usermanagement.role.roleassigner.name")); //$NON-NLS-1$
			name = new JTextField();

			finder = new ExpressionBuilderFinder(userService, RecipientFilter.USERS, RecipientFilter.GROUPS,
				RecipientFilter.IP_ADDRESS, RecipientFilter.HOST_REFERRER, RecipientFilter.EXPRESSION,
				RecipientFilter.NO_OWNER);

			final int width1 = nameLabel.getPreferredSize().width;
			final int height1 = name.getPreferredSize().height;

			final int[] rows = {height1, TableLayout.FILL,};
			final int[] cols = {width1, TableLayout.FILL,};

			setLayout(new TableLayout(rows, cols));
			add(nameLabel, new Rectangle(0, 0, 1, 1));
			add(name, new Rectangle(1, 0, 1, 1));
			add(finder, new Rectangle(0, 1, 2, 1));

			changeDetector = new ChangeDetector();
			changeDetector.watch(name);
			changeDetector.watch(finder.getTreeModel());
		}

		@Override
		public void addNameListener(KeyListener listener)
		{
			name.addKeyListener(listener);
		}

		@Override
		public void load(RoleMapping element)
		{
			name.setText(element.getName());
			finder.setExpression(element.getExpression());
		}

		@Override
		public void save(RoleMapping element)
		{
			element.setName(name.getText());
			try
			{
				element.setExpression((String) finder.getSelectedResults().get(0));
			}
			catch( RuntimeApplicationException ex )
			{
				element.setExpression(""); //$NON-NLS-1$
			}
		}

		public ExpressionBuilderFinder getFinder()
		{
			return finder;
		}

		@Override
		public void clearChanges()
		{
			changeDetector.clearChanges();
		}

		@Override
		public boolean hasDetectedChanges()
		{
			return changeDetector.hasDetectedChanges();
		}
	}
}
