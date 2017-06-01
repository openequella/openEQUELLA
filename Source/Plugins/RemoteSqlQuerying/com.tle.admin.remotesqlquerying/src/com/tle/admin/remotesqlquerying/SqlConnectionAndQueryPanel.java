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

package com.tle.admin.remotesqlquerying;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.awt.font.TextAttribute;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.Changeable;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.gui.common.ListWithView;
import com.tle.admin.gui.common.ListWithViewInterface;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.admin.helper.JdbcDriver;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.remotesqlquerying.RemoteRemoteSqlQueryingService;

@SuppressWarnings("nls")
public class SqlConnectionAndQueryPanel extends JPanel implements Changeable
{
	private final ClientService clientService;
	private final ChangeDetector changeDetector;

	private final JComboBox drivers;
	private final JTextField url;
	private final JTextField username;
	private final JPasswordField password;
	private final ListWithView<QueryState, ListWithViewInterface<QueryState>> queryLWV;

	public SqlConnectionAndQueryPanel(ClientService clientService)
	{
		super(new MigLayout("fill, wrap 2", "[align label][grow, fill]"));

		this.clientService = clientService;

		drivers = new JComboBox();
		url = new JTextField();
		username = new JTextField();
		password = new JPasswordField();

		queryLWV = new ListWithView<QueryState, ListWithViewInterface<QueryState>>(false, false)
		{
			@Override
			protected ListWithViewInterface<QueryState> getEditor(QueryState q)
			{
				return new Editor();
			}

			@Override
			protected QueryState createElement()
			{
				throw new UnsupportedOperationException();
			}
		};

		drivers.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				url.setText(((JdbcDriver) drivers.getSelectedItem()).getJdbcUrl());
			}
		});
		JdbcDriver.addDrivers(drivers);

		add(new JLabel(s("driver")));
		add(drivers);
		add(new JLabel(s("url")));
		add(url);
		add(new JLabel(s("username")));
		add(username);
		add(new JLabel(s("password")));
		add(password);
		add(new JButton(testConnection), "span 2, align center");
		add(new JLabel(s("queries")), "wrap");
		add(queryLWV, "span 2, push, grow");

		changeDetector = new ChangeDetector();
		changeDetector.watch(drivers);
		changeDetector.watch(url);
		changeDetector.watch(username);
		changeDetector.watch(password);
		changeDetector.watch(queryLWV);
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);

		drivers.setEnabled(enabled);
		url.setEnabled(enabled);
		username.setEnabled(enabled);
		password.setEnabled(enabled);
		queryLWV.setEnabled(enabled);
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

	public void load(String driverClass, String jdbcUrl, String username, String password,
		Collection<QueryState> queries)
	{
		if( !Check.isEmpty(driverClass) )
		{
			AppletGuiUtils.selectInJCombo(drivers, new JdbcDriver(driverClass));
		}

		if( !Check.isEmpty(jdbcUrl) )
		{
			// Do not set this if the URL is empty, otherwise we overwrite the
			// URL template for the driver class.
			this.url.setText(jdbcUrl);
		}

		this.username.setText(username);
		this.password.setText(password);

		queryLWV.load(queries);
	}

	public String getDriverClass()
	{
		return ((JdbcDriver) drivers.getSelectedItem()).getDriverClass();
	}

	public String getJdbcUrl()
	{
		return url.getText();
	}

	public String getUsername()
	{
		return username.getText();
	}

	public String getPassword()
	{
		return new String(password.getPassword());
	}

	public List<QueryState> getQueries()
	{
		return queryLWV.save();
	}

	private final TLEAction testConnection = new TLEAction(s("testconnection"))
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
			{
				@Override
				public Object construct() throws Exception
				{
					clientService.getService(RemoteRemoteSqlQueryingService.class).testConnection(getDriverClass(),
						getJdbcUrl(), getUsername(), getPassword());
					return null;
				}

				@Override
				public void finished()
				{
					JOptionPane.showMessageDialog(getComponent(), s("testconnection.success.message"),
						s("testconnection.success.title"), JOptionPane.INFORMATION_MESSAGE);
				}

				@Override
				public void exception()
				{
					JOptionPane.showMessageDialog(getComponent(), getException().getMessage(),
						s("testconnection.error.title"), JOptionPane.ERROR_MESSAGE);
				}
			};
			worker.setComponent(SqlConnectionAndQueryPanel.this);
			worker.start();
		}
	};

	private static String s(String keyEnd)
	{
		return CurrentLocale.get("com.tle.admin.remotesqlquerying." + keyEnd);
	}

	private static class Editor extends JPanel implements ListWithViewInterface<QueryState>
	{
		private JLabel description;
		private JTextArea sql;
		private String originalSql;

		@Override
		@SuppressWarnings("unchecked")
		public void setup()
		{
			description = new JLabel();
			sql = new JTextArea();
			Map<TextAttribute, Object> fas = (Map<TextAttribute, Object>) sql.getFont().getAttributes();
			fas.put(TextAttribute.FAMILY, Font.MONOSPACED);
			sql.setFont(new Font(fas));

			setLayout(new BorderLayout());
			add(description, BorderLayout.NORTH);
			add(new JScrollPane(sql), BorderLayout.CENTER);
		}

		@Override
		public void load(QueryState q)
		{
			description.setText(q.getDescription());
			sql.setText(q.getSql());
		}

		@Override
		public void save(QueryState q)
		{
			q.setSql(sql.getText());
		}

		@Override
		public Component getComponent()
		{
			return this;
		}

		@Override
		public void clearChanges()
		{
			originalSql = sql.getText();
		}

		@Override
		public boolean hasDetectedChanges()
		{
			return !sql.getText().equals(originalSql);
		}

		@Override
		public void addNameListener(KeyListener listener)
		{
			// Nothing to do here
		}
	}
}
