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

package com.tle.client.harness;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.CookieHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jnlp.ServiceManager;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor;
import org.springframework.remoting.support.RemoteInvocation;

import com.dytech.common.net.Proxy;
import com.dytech.edge.common.Version;
import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.thoughtworks.xstream.XStream;
import com.tle.admin.PluginServiceImpl;
import com.tle.client.ListCookieHandler;
import com.tle.common.util.BlindSSLSocketFactory;
import com.tle.core.plugins.PluginAwareObjectInputStream;
import com.tle.core.plugins.PluginAwareObjectOutputStream;
import com.tle.core.remoting.RemotePluginDownloadService;
import com.tle.core.remoting.SessionLogin;

@SuppressWarnings("nls")
public class ClientLauncher extends JFrame implements ActionListener, WindowListener, MouseListener
{
	private static final long serialVersionUID = 1L;
	private static final Log LOGGER = LogFactory.getLog(ClientLauncher.class);

	private static final int WINDOW_WIDTH = 450;
	private static final String WINDOW_TITLE = "Client Launcher";
	private static final String SERVER_XML = "server.xml"; //$NON-NLS-1$

	private HarnessConfig config;

	private JComboBox profileChoice;
	private JLabel configureLink;
	private JButton add;
	private JButton edit;
	private JButton remove;
	private JButton proxy;
	private JButton connect;
	private JButton exit;

	private int configButtonsHeight;
	private TableLayout layout;

	public static void main(String args[]) throws Exception
	{
		new ClientLauncher();
	}

	public ClientLauncher()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch( Exception ex )
		{
			LOGGER.error("Look And Feel could not be set.", ex);
		}

		setup();
		setVisible(true);
	}

	private void setup()
	{
		BlindSSLSocketFactory.register();

		setupGUI();
		loadProfiles();
	}

	private void setupGUI()
	{
		JComponent serverList = createServerList();
		JComponent configButtons = createConfigButtons();
		JComponent connectExit = createConnectExit();

		configButtonsHeight = configButtons.getPreferredSize().height;

		final int height1 = serverList.getMinimumSize().height;
		final int height2 = connectExit.getMinimumSize().height;
		final int[] rows = {height1, 0, height2,};
		final int[] cols = {WINDOW_WIDTH,};

		layout = new TableLayout(rows, cols);
		JPanel all = new JPanel(layout);
		all.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		all.add(serverList, new Rectangle(0, 0, 1, 1));
		all.add(configButtons, new Rectangle(0, 1, 1, 1));
		all.add(connectExit, new Rectangle(0, 2, 1, 1));

		updateButtons();

		setTitle(WINDOW_TITLE);
		setResizable(false);
		getContentPane().add(all);
		getRootPane().setDefaultButton(connect);
		addWindowListener(this);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		pack();
		ComponentHelper.centreOnScreen(this);
	}

	private JComponent createServerList()
	{
		JLabel profileLabel = new JLabel("Choose a Server:");

		configureLink = new JLabel("Configure...");
		configureLink.setForeground(Color.BLUE);
		configureLink.addMouseListener(this);

		profileChoice = new JComboBox();
		profileChoice.addActionListener(this);
		profileChoice.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus)
			{
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				ServerProfile p = (ServerProfile) value;
				if( p != null )
				{
					setText(p.getName() + " - " + p.getServer());
				}
				return this;
			}
		});

		final int height1 = profileChoice.getPreferredSize().height;
		final int width1 = configureLink.getPreferredSize().width;

		final int[] rows = {height1, height1,};
		final int[] cols = {TableLayout.FILL, width1,};

		JPanel all = new JPanel(new TableLayout(rows, cols));
		all.add(profileLabel, new Rectangle(0, 0, 1, 1));
		all.add(configureLink, new Rectangle(1, 0, 1, 1));
		all.add(profileChoice, new Rectangle(0, 1, 2, 1));

		return all;
	}

	private JComponent createConfigButtons()
	{
		JSeparator separator = new JSeparator();

		add = new JButton("Add Server");
		edit = new JButton("Edit Server");
		remove = new JButton("Remove Server");
		proxy = new JButton("Proxy Settings");

		add.addActionListener(this);
		edit.addActionListener(this);
		remove.addActionListener(this);
		proxy.addActionListener(this);

		final int height1 = add.getPreferredSize().height;
		final int height2 = separator.getPreferredSize().height;
		final int width1 = remove.getPreferredSize().width;
		final int width2 = proxy.getPreferredSize().width;
		final int width3 = Math.max(width1, width2);
		final int[] rows = {height1, height1, height2,};
		final int[] cols = {TableLayout.FILL, width3, width3, width3, TableLayout.FILL,};

		JPanel all = new JPanel(new TableLayout(rows, cols));

		all.add(add, new Rectangle(1, 0, 1, 1));
		all.add(edit, new Rectangle(2, 0, 1, 1));
		all.add(remove, new Rectangle(3, 0, 1, 1));
		all.add(proxy, new Rectangle(2, 1, 1, 1));
		all.add(separator, new Rectangle(0, 2, 5, 1));

		return all;
	}

	public JComponent createConnectExit()
	{
		connect = new JButton("Launch");
		exit = new JButton("Exit");

		connect.addActionListener(this);
		exit.addActionListener(this);

		final int width1 = connect.getPreferredSize().width;
		final int height1 = connect.getPreferredSize().height;
		final int[] rows = {height1,};
		final int[] cols = {TableLayout.FILL, width1, width1, TableLayout.FILL,};

		JPanel all = new JPanel(new TableLayout(rows, cols, 5, 5));

		all.add(connect, new Rectangle(1, 0, 1, 1));
		all.add(exit, new Rectangle(2, 0, 1, 1));

		return all;
	}

	private void loadProfiles()
	{
		File f = new File(SERVER_XML);
		if( f.exists() && f.isFile() )
		{
			try( Reader reader = new BufferedReader(new FileReader(f)) )
			{
				config = (HarnessConfig) new XStream().fromXML(reader);
			}
			catch( IOException ex )
			{
				// Ignore this
			}
			catch( Error err )
			{
				LOGGER.warn("Error reading configuration file", err);
			}
		}

		if( config == null )
		{
			config = new HarnessConfig();
		}

		if( config.getServers() != null )
		{
			for( ServerProfile profile : config.getServers() )
			{
				profileChoice.addItem(profile);
				if( profile.getName().equals(config.getLastSelectedName()) )
				{
					profileChoice.setSelectedItem(profile);
				}
			}

		}

		// Set the window title again...
		String newTitle = WINDOW_TITLE;
		newTitle += " - Admin Console"; //$NON-NLS-1$

		setTitle(newTitle);
	}

	private void saveProfiles()
	{
		// Save all the servers back to the configuration object.
		Collection<ServerProfile> servers = new ArrayList<ServerProfile>();
		int count = profileChoice.getItemCount();
		for( int i = 0; i < count; i++ )
		{
			servers.add((ServerProfile) profileChoice.getItemAt(i));
		}
		config.setServers(servers);

		// Delete the existing server file.
		File newFile = new File(SERVER_XML);
		if( newFile.exists() && !newFile.delete() )
		{
			LOGGER.error("Could not delete existing server.xml");
			return;
		}

		// Write the configuration back to the file.
		try( Writer out = new BufferedWriter(new FileWriter(newFile)) )
		{
			new XStream().toXML(config, out);
		}
		catch( Exception ex )
		{
			LOGGER.warn("Error saving server xml", ex);
		}
	}

	private void updateButtons()
	{
		boolean enable = profileChoice.getSelectedIndex() > -1;

		edit.setEnabled(enable);
		remove.setEnabled(enable);
		connect.setEnabled(enable);
	}

	private void launch(final ServerProfile server)
	{
		GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
		{
			@Override
			public Object construct() throws Exception
			{
				if( config.getProxy() != null )
				{
					ProxySettings p = config.getProxy();
					Proxy.setProxy(p.getHost(), p.getPort(), p.getUsername(), p.getPassword());
				}

				ClientLauncher.this.setVisible(false);
				ClientLauncher.this.dispose();
				// ClientHarness harness = new ClientHarness(client, config,
				// server);
				String url = server.getServer().trim();

				// Make sure the end-point has the soap context.
				if( !url.endsWith("/") )
				{
					url += '/';
				}

				URL endpointUrl = new URL(url);
				LOGGER.info("Endpoint:  " + endpointUrl.toString());

				// Initialise server session
				ListCookieHandler lch = new ListCookieHandler();
				lch.setIgnoreCookieOverrideAttempts(true);
				CookieHandler.setDefault(lch);

				Map<String, String> params = new HashMap<String, String>();
				params.put("username", server.getUsername());
				params.put("password", server.getPassword());
				SessionLogin.postLogin(endpointUrl, params);
				
				PluginServiceImpl pluginService = new PluginServiceImpl(endpointUrl, Version.load()
					.getCommit(), createInvoker(RemotePluginDownloadService.class, endpointUrl));
				pluginService.registerPlugins();
				HarnessInterface client = (HarnessInterface) pluginService.getBean("com.equella.admin",
					"com.tle.admin.AdminConsole");
				client.setPluginService(pluginService);
				client.setLocale(Locale.getDefault());

				System.setProperty("jnlp.INSTITUTIONNAME", "{temp inst name}");

				client.setEndpointURL(endpointUrl);
				// Initialise services
				ServiceManager.setServiceManagerStub(new ServiceManagerHarness(endpointUrl));
				client.start();
				return null;
			}

			@Override
			public void exception()
			{
				Exception ex = getException();

				LOGGER.error("Problem launching client", ex);
				JOptionPane.showMessageDialog(ClientLauncher.this, "Problem launching client (see console)");
				System.exit(1);

			}
		};

		worker.setComponent(this);
		worker.start();
	}

	@SuppressWarnings({"unchecked"})
	protected <T> T createInvoker(Class<T> clazz, URL endpointUrl)
	{
		HttpInvokerProxyFactoryBean factory = new HttpInvokerProxyFactoryBean();
		try
		{
			URL url = new URL(endpointUrl, "invoker/" + clazz.getName() + ".service");
			LOGGER.info("Invoking " + url.toString());
			factory.setServiceUrl(url.toString());
		}
		catch( MalformedURLException e )
		{
			throw new RuntimeException(e);
		}
		factory.setServiceInterface(clazz);
		factory.setHttpInvokerRequestExecutor(new PluginAwareSimpleHttpInvokerRequestExecutor());
		factory.afterPropertiesSet();
		return (T) factory.getObject();
	}

	public static class PluginAwareSimpleHttpInvokerRequestExecutor extends SimpleHttpInvokerRequestExecutor
	{
		@Override
		protected ObjectInputStream createObjectInputStream(InputStream is, String codebaseUrl) throws IOException
		{
			return new PluginAwareObjectInputStream(is);
		}

		@Override
		protected void writeRemoteInvocation(RemoteInvocation invocation, OutputStream os) throws IOException
		{
			ObjectOutputStream oos = new PluginAwareObjectOutputStream(decorateOutputStream(os));
			try
			{
				doWriteRemoteInvocation(invocation, oos);
				oos.flush();
			}
			finally
			{
				oos.close();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == profileChoice )
		{
			updateButtons();
		}
		else if( e.getSource() == connect )
		{
			ServerProfile sp = (ServerProfile) profileChoice.getSelectedItem();
			config.setLastSelectedName(sp.getName());
			saveProfiles();
			launch(sp);
		}
		else if( e.getSource() == exit )
		{
			onExit();
		}
		else if( e.getSource() == add )
		{
			ServerProfile selectedProfile = (ServerProfile) profileChoice.getSelectedItem();
			if( selectedProfile != null
				&& JOptionPane.showConfirmDialog(this, "Do you want to clone the currently selected profile?",
					"Clone?", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION )
			{
				selectedProfile = null;
			}

			AppServerEditDialog dialog = new AppServerEditDialog(this, selectedProfile);
			dialog.setModal(true);
			dialog.setVisible(true);

			if( dialog.getResult() == AppServerEditDialog.RESULT_OK )
			{
				ServerProfile profile = dialog.getProfile();

				profileChoice.addItem(profile);
				profileChoice.setSelectedIndex(profileChoice.getItemCount() - 1);

				updateButtons();
			}
		}
		else if( e.getSource() == edit )
		{
			final int index = profileChoice.getSelectedIndex();
			if( index > -1 )
			{
				final ServerProfile profile = (ServerProfile) profileChoice.getSelectedItem();
				AppServerEditDialog dialog = new AppServerEditDialog(this, profile);
				dialog.setModal(true);
				dialog.setVisible(true);

				if( dialog.getResult() == AppServerEditDialog.RESULT_OK )
				{
					ServerProfile newProfile = dialog.getProfile();
					profileChoice.removeItemAt(index);
					profileChoice.insertItemAt(newProfile, index);
					profileChoice.setSelectedIndex(index);

					updateButtons();
				}
			}
		}
		else if( e.getSource() == remove )
		{
			final int index = profileChoice.getSelectedIndex();
			if( index > -1 )
			{
				final int result = JOptionPane.showConfirmDialog(this,
					"Are you sure you want to remove this server profile?", "Are you sure?", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);

				if( result == JOptionPane.YES_OPTION )
				{
					profileChoice.removeItemAt(index);
					updateButtons();
				}
			}
		}
		else if( e.getSource() == proxy )
		{
			ProxyDetailsDialog dialog = new ProxyDetailsDialog(this);
			dialog.loadConfiguration(config.getProxy());
			dialog.setVisible(true);

			if( dialog.getResult() == ProxyDetailsDialog.RESULT_OK )
			{
				config.setProxy(dialog.saveConfiguration());
			}
		}
	}

	private void onExit()
	{
		saveProfiles();
		dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowActivated(WindowEvent e)
	{
		// We don't care about this event
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosed(WindowEvent e)
	{
		// We don't care about this event
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosing(WindowEvent e)
	{
		onExit();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent
	 * )
	 */
	@Override
	public void windowDeactivated(WindowEvent e)
	{
		// We don't care about this event
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent
	 * )
	 */
	@Override
	public void windowDeiconified(WindowEvent e)
	{
		// We don't care about this event
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowIconified(WindowEvent e)
	{
		// We don't care about this event
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowOpened(WindowEvent e)
	{
		// We don't care about this event
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e)
	{
		if( e.getSource() == configureLink )
		{
			int height = layout.getRowSize(1);
			if( height == 0 )
			{
				height = configButtonsHeight;
			}
			else
			{
				height = 0;
			}
			layout.setRowSize(1, height);
			pack();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e)
	{
		// We don't care about this event
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e)
	{
		// We don't care about this event
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e)
	{
		// We don't care about this event
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e)
	{
		// We don't care about this event
	}
}
