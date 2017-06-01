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

package com.tle.admin.usermanagement.standard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.dytech.gui.ChangeDetector;
import com.dytech.gui.filter.FilterModel;
import com.dytech.gui.filter.FilteredShuffleList;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.common.ListWithView;
import com.tle.admin.gui.common.ListWithViewInterface;
import com.tle.admin.plugin.GeneralPlugin;
import com.tle.beans.user.TLEGroup;
import com.tle.beans.usermanagement.standard.wrapper.SharedSecretSettings;
import com.tle.beans.usermanagement.standard.wrapper.SharedSecretSettings.SharedSecretValue;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.ExpressionBuilderFinder;
import com.tle.common.recipientselector.ExpressionFormatter;
import com.tle.common.recipientselector.RecipientFilter;
import com.tle.common.recipientselector.RecipientUtils;
import com.tle.common.recipientselector.SingleFinderDialog;
import com.tle.core.remoting.RemoteTLEGroupService;
import com.tle.core.remoting.RemoteUserService;

@SuppressWarnings("nls")
public class SharedSecretWrapper extends GeneralPlugin<SharedSecretSettings>
{
	private ListWithView<SharedSecretValue, Editor> listWithView;

	@Override
	public void init()
	{
		listWithView = new ListWithView<SharedSecretValue, Editor>()
		{
			@Override
			protected SharedSecretValue createElement()
			{
				SharedSecretValue mapping = new SharedSecretValue();
				return mapping;
			}

			@Override
			protected Editor getEditor(SharedSecretValue currentSelection)
			{
				if( currentSelection == null )
				{
					return null;
				}
				else
				{
					return new Editor();
				}
			}
		};
		listWithView.setListCellRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus)
			{
				DefaultListCellRenderer d = (DefaultListCellRenderer) super.getListCellRendererComponent(list, value,
					index, isSelected, cellHasFocus);
				String id = ((SharedSecretValue) value).getId();
				if( id.length() == 0 )
				{
					id = s("default");
				}
				d.setText(id);
				return d;
			}

		});

		addFillComponent(listWithView);
	}

	private List<SharedSecretValue> getEntries()
	{
		return listWithView.save();
	}

	@Override
	public void validation() throws EditorException
	{
		Set<String> ids = new HashSet<String>();
		int row = 0;
		for( SharedSecretValue v : getEntries() )
		{
			try
			{
				String id2 = v.getId();
				int length = id2.length();
				if( length > 0 )
				{
					char first = id2.charAt(0);
					if( !Character.isLetter(first) )
					{
						throw new EditorException(s("mustbegin"));
					}
				}
				if( !ids.add(id2) )
				{
					throw new EditorException(s("mustbeunique", id2));
				}

				requireAllowedCharacters(id2);

				if( v.getSecret().length() == 0 )
				{
					throw new EditorException(s("enter"));
				}
				row++;
			}
			catch( EditorException e )
			{
				listWithView.getList().setSelectedIndex(row);
				throw e;
			}
		}
	}

	// Sonar - Farmed out to reduce Cyclomatic Complexity
	// THrows exception if any chars ore other than alphanumeric, hyphen,
	// underscore, or dot
	private void requireAllowedCharacters(String id2) throws EditorException
	{
		for( int i = 0; i < id2.length(); i++ )
		{
			char c = id2.charAt(i);
			if( Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.' )
			{
				continue;
			}
			throw new EditorException(s("invalid", c));
		}
	}

	protected class Editor extends JPanel implements ListWithViewInterface<SharedSecretValue>
	{
		private RemoteTLEGroupService tleGroupService;

		private final JTextField name = new JTextField();
		private final JTextField secret = new JTextField();
		private ExpressionBuilderFinder finder;
		private JTextField expressionField;
		private JButton expressionButton;
		private ExpressionFormatter formatter;
		private final JTextField prefix = new JTextField();
		private final JTextField postfix = new JTextField();

		private JRadioButton failNonExistantUser;
		private JRadioButton ignoreNonExistantUser;
		private JRadioButton autoCreateNonExistantUser;
		private FilteredShuffleList<TLEGroup> autoAddToGroups;

		private ChangeDetector changeDetector;

		@Override
		public Component getComponent()
		{
			return this;
		}

		@Override
		public void setup()
		{
			tleGroupService = clientService.getService(RemoteTLEGroupService.class);

			initFinderParts();

			failNonExistantUser = new JRadioButton(s("nonexistantuser.fail"), true);
			ignoreNonExistantUser = new JRadioButton(s("nonexistantuser.ignore"));
			autoCreateNonExistantUser = new JRadioButton(s("nonexistantuser.create"));

			final ButtonGroup nonExistantUserGroup = AppletGuiUtils.group(failNonExistantUser, ignoreNonExistantUser,
				autoCreateNonExistantUser);

			autoCreateNonExistantUser.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(ItemEvent e)
				{
					autoAddToGroups.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
				}
			});

			autoAddToGroups = new FilteredShuffleList<TLEGroup>(null, new FilterModel<TLEGroup>()
			{
				@Override
				public List<TLEGroup> search(String pattern)
				{
					String query = pattern;
					if( !query.endsWith("*") )
					{
						query += "*";
					}
					return tleGroupService.search(query);
				}
			});
			autoAddToGroups.setListCellRenderer(new DefaultListCellRenderer()
			{
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus)
				{
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					setText(((TLEGroup) value).getName());
					return this;
				}
			});
			autoAddToGroups.setEnabled(false);

			setLayout(new MigLayout("fill", "[20][][][fill,grow]"));

			// Sometime's Sonar's complaints about repeated string literals
			// really don't help
			add(new JLabel(s("id")), "span 2");
			add(name, "span, growx"); // NOSONAR
			add(new JLabel(s("secret")), "span 2");
			add(secret, "span, growx"); // NOSONAR

			add(new JLabel(s("modifyusernames")), "span");
			add(new JLabel(s("prefix")), "skip, span 2");
			add(prefix, "span, growx"); // NOSONAR
			add(new JLabel(s("postfix")), "skip, span 2");
			add(postfix, "span, growx"); // NOSONAR

			add(new JLabel(s("expression")), "span");
			add(expressionField, "skip, span, split 2, growx");
			add(expressionButton);

			add(new JLabel(s("nonexistantuser")), "span");
			add(failNonExistantUser, "skip, span");
			add(ignoreNonExistantUser, "skip, span");
			add(autoCreateNonExistantUser, "skip, span");

			add(autoAddToGroups, "skip, span, grow, push");

			changeDetector = new ChangeDetector();
			changeDetector.watch(name);
			changeDetector.watch(secret);
			changeDetector.watch(prefix);
			changeDetector.watch(postfix);
			changeDetector.watch(expressionField);
			changeDetector.watch(nonExistantUserGroup);
			changeDetector.watch(autoAddToGroups.getModel());
		}

		private void initFinderParts()
		{
			expressionField = new JTextField();
			expressionField.setEditable(false);

			expressionButton = new JButton(s("configure"));
			expressionButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					SingleFinderDialog dialog = new SingleFinderDialog(finder);
					Pair<RecipientFilter, Object> result = dialog.showFinder(parentDialog);

					if( result != null )
					{
						String currentValue = RecipientUtils.convertToRecipient(result.getFirst(), result.getSecond());
						loadExpression(currentValue);
					}
				}
			});

			RemoteUserService userService = clientService.getService(RemoteUserService.class);
			formatter = new ExpressionFormatter(userService);
			finder = new ExpressionBuilderFinder(userService, RecipientFilter.USERS, RecipientFilter.GROUPS,
				RecipientFilter.ROLES, RecipientFilter.IP_ADDRESS, RecipientFilter.HOST_REFERRER,
				RecipientFilter.EXPRESSION, RecipientFilter.NO_OWNER);
		}

		void loadExpression(String currentValue)
		{
			String string = formatter.convertToInfix(currentValue);
			expressionField.setText(string);
		}

		@Override
		public void addNameListener(KeyListener listener)
		{
			name.addKeyListener(listener);
		}

		@Override
		public void load(SharedSecretValue element)
		{
			name.setText(element.getId());
			finder.setExpression(element.getExpression());
			loadExpression(element.getExpression());
			secret.setText(element.getSecret());
			prefix.setText(element.getPrefix());
			postfix.setText(element.getPostfix());

			if( element.isIgnoreNonExistantUser() )
			{
				ignoreNonExistantUser.setSelected(true);
			}
			else if( element.isAutoCreate() )
			{
				autoCreateNonExistantUser.setSelected(true);
			}

			List<String> groups = element.getGroups();
			if( !Check.isEmpty(groups) )
			{
				autoAddToGroups.addItems(tleGroupService.getInformationForGroups(element.getGroups()));
			}
		}

		@Override
		public void save(SharedSecretValue element)
		{
			element.setId(name.getText());
			element.setAutoCreate(autoCreateNonExistantUser.isSelected());
			try
			{
				element.setExpression((String) finder.getSelectedResults().get(0));
			}
			catch( RuntimeApplicationException ex )
			{
				element.setExpression("");
			}
			element.setSecret(secret.getText());
			element.setPrefix(prefix.getText());
			element.setPostfix(postfix.getText());
			element.setIgnoreNonExistantUser(ignoreNonExistantUser.isSelected());

			final List<String> groups = element.getGroups();
			groups.clear();
			for( TLEGroup g : autoAddToGroups.getModel() )
			{
				groups.add(g.getUuid());
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

	@Override
	public void load(SharedSecretSettings xml)
	{
		List<SharedSecretValue> sharedSecrets = xml.getSharedSecrets();
		Collections.sort(sharedSecrets, new Comparator<SharedSecretValue>()
		{
			@Override
			public int compare(SharedSecretValue o1, SharedSecretValue o2)
			{
				return o1.getId().compareToIgnoreCase(o2.getId());
			}
		});
		listWithView.load(sharedSecrets);
	}

	@Override
	public boolean save(SharedSecretSettings xml)
	{
		xml.setSharedSecrets(getEntries());

		// reload the secrets (so they are sorted)
		SharedSecretValue selected = listWithView.getList().getSelectedValue();
		load(xml);
		listWithView.getList().setSelectedValue(selected, true);

		return true;
	}

	private static String s(String keyPart, Object... values)
	{
		return CurrentLocale.get("com.tle.admin.usermanagement.sharedsecretwrapper." + keyPart, values);
	}
}
