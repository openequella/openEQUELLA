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

package com.tle.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.registry.Extension;

import com.dytech.gui.ComponentHelper;
import com.dytech.gui.JHoverButton;
import com.dytech.gui.JImage;
import com.dytech.gui.TableLayout;
import com.dytech.gui.VerticalFlowLayout;
import com.dytech.gui.flatter.FlatterLookAndFeel;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.common.adminconsole.RemoteAdminService;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.plugins.PluginTracker.ExtensionParamComparator;

@SuppressWarnings("nls")
public class ManagementDialog extends JFrame implements ActionListener, WindowListener
{
	private static final long serialVersionUID = 1L;

	private static final Log LOGGER = LogFactory.getLog(ManagementDialog.class);

	private static final int WINDOW_WIDTH = 450;
	private static final int WINDOW_HEIGHT = 610;

	private static final String EXIT_ICON = "/icons/exclamation.gif";

	private LookAndFeel flatterLaF;
	private LookAndFeel currentLaF;
	private ImageIcon bullets;

	private final PluginServiceImpl pluginService;
	private final ClientService clientService;

	private final List<Extension> registeredTools = new ArrayList<Extension>();
	private final List<Set<String>> registeredPrivs = new ArrayList<Set<String>>();
	private final Map<String, AdminTool> activatedTools = new HashMap<String, AdminTool>();
	private final List<JButton> buttons = new ArrayList<JButton>();

	private JPanel toolsPanel;
	private JPanel right;
	private JButton exitButton;
	private boolean pluginFailedToLoad;

	public ManagementDialog()
	{
		Driver driver = Driver.instance();
		clientService = driver.getClientService();
		pluginService = driver.getPluginService();

		setup();
		loadConfig();
	}

	protected void loadConfig()
	{
		// Allow secure tools if the "admin" user, or option is turned off on
		// the server.
		pluginFailedToLoad = false;

		final Map<String, Set<String>> allowedToolsWithPrivs = clientService.getService(RemoteAdminService.class)
			.getAllowedTools();
		final PluginTracker<?> tracker = new PluginTracker<Object>(pluginService, "com.tle.admin.tools", "tool", null,
			new ExtensionParamComparator("displayorder"));
		for( Extension extension : tracker.getExtensions() )
		{
			Set<String> privs = allowedToolsWithPrivs.get(extension.getUniqueId());
			if( privs != null )
			{
				registerTool(extension, privs);
			}
		}
	}

	private void setup()
	{
		bullets = new ImageIcon(ManagementDialog.class.getResource("/icons/menuitem.gif"));

		currentLaF = UIManager.getLookAndFeel();
		flatterLaF = new FlatterLookAndFeel();

		JImage header = new JImage(ManagementDialog.class.getResource("/icons/header.gif"));
		Dimension size1 = new Dimension(450, 70);
		header.setSize(size1);
		header.setMinimumSize(size1);
		header.setPreferredSize(size1);
		header.setMaximumSize(size1);

		JImage footer = new JImage(ManagementDialog.class.getResource("/icons/footer.gif"));
		Dimension size2 = new Dimension(450, 6);
		footer.setSize(size2);
		footer.setMinimumSize(size2);
		footer.setPreferredSize(size2);
		footer.setMaximumSize(size2);

		toolsPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 5, 1, false, false));
		right = new JPanel(new GridLayout(1, 1));

		ImageIcon icon = new ImageIcon(ManagementDialog.class.getResource(EXIT_ICON));
		exitButton = new JHoverButton(CurrentLocale.get("com.tle.admin.gui.managementdialog.exit"), icon);
		setupButton(exitButton);

		JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		south.add(exitButton);

		JPanel left = new JPanel(new BorderLayout());
		left.add(toolsPanel, BorderLayout.CENTER);
		left.add(south, BorderLayout.SOUTH);

		final int[] rows = {TableLayout.FILL,};
		final int[] cols = {175, TableLayout.FILL,};

		JPanel centre = new JPanel(new TableLayout(rows, cols, 5, 5));
		centre.add(left, new Rectangle(0, 0, 1, 1));
		centre.add(right, new Rectangle(1, 0, 1, 1));

		JPanel all = new JPanel(new BorderLayout());
		all.setBackground(Color.white);
		all.add(header, BorderLayout.NORTH);
		all.add(centre, BorderLayout.CENTER);
		all.add(footer, BorderLayout.SOUTH);

		applyFlatterLaF(all);

		setTitle(CurrentLocale.get("com.tle.admin.gui.managementdialog.title",
			Driver.instance().getVersion().getFull(), Driver.instance().getInstitutionName()));
		setIconImage(new ImageIcon(ManagementDialog.class.getResource("/icons/windowicon.gif")).getImage());
		setContentPane(all);
		setResizable(false);
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		addWindowListener(this);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		ComponentHelper.centreOnScreen(this);
	}

	private void registerTool(Extension extension, Set<String> grantedPrivileges)
	{
		registeredTools.add(extension);
		registeredPrivs.add(grantedPrivileges);

		JButton button = new JHoverButton(CurrentLocale.get(extension.getParameter("name").valueAsString()), bullets);
		setupButton(button);
		buttons.add(button);
		toolsPanel.add(button);
	}

	private void setupButton(JButton button)
	{
		applyFlatterLaF(button);

		button.setBorderPainted(false);
		button.setIconTextGap(10);
		button.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == exitButton )
		{
			dispose();
			onExit();
		}
		else
		{
			right.removeAll();

			final int count = buttons.size();
			for( int i = 0; i < count; ++i )
			{
				JButton b = buttons.get(i);
				if( e.getSource() == b )
				{
					right.removeAll();

					final Extension extension = registeredTools.get(i);
					final Set<String> privs = registeredPrivs.get(i);
					GlassSwingWorker<?> worker = new GlassSwingWorker<AdminTool>()
					{
						@Override
						public AdminTool construct() throws Exception
						{
							String id = extension.getUniqueId();
							AdminTool tool = activatedTools.get(id);
							if( tool == null )
							{
								tool = (AdminTool) pluginService.getBean(extension.getDeclaringPluginDescriptor(),
									extension.getParameter("class").valueAsString());

								tool.setDriver(Driver.instance());
								tool.setManagementPanel(right);
								tool.setParent(ManagementDialog.this);
								tool.setup(privs, CurrentLocale.get(extension.getParameter("name").valueAsString()));
								activatedTools.put(id, tool);
							}
							return tool;
						}

						@Override
						public void finished()
						{
							AdminTool tool = get();
							tool.toolSelected();
							applyFlatterLaF(right);
						}

						@Override
						public void exception()
						{
							LOGGER.error("Error loading tool", getException());
							JOptionPane.showMessageDialog(getComponent(), "Error loading tool");
						}
					};
					worker.setComponent(this);
					worker.start();
					return;
				}
			}
		}
	}

	@Override
	public void setVisible(boolean show)
	{
		super.setVisible(show);

		if( show )
		{
			if( pluginFailedToLoad )
			{
				JOptionPane.showMessageDialog(this, CurrentLocale.get("com.tle.admin.gui.managementdialog.failedload"),
					CurrentLocale.get("com.tle.admin.gui.managementdialog.failed"), JOptionPane.WARNING_MESSAGE);
				pluginFailedToLoad = false;
			}
		}
	}

	public void applyFlatterLaF(JComponent c)
	{
		try
		{
			UIManager.setLookAndFeel(flatterLaF);
			SwingUtilities.updateComponentTreeUI(c);
			c.invalidate();
			c.updateUI();

			UIManager.setLookAndFeel(currentLaF);
		}
		catch( Exception ex )
		{
			LOGGER.error("Could not apply Flatter LaF to component: " + c.toString(), ex);
		}
	}

	private void onExit()
	{
		clientService.stop();
	}

	@Override
	public void windowActivated(WindowEvent e)
	{
		// Nothing to do here
	}

	@Override
	public void windowClosed(WindowEvent e)
	{
		onExit();
	}

	@Override
	public void windowClosing(WindowEvent e)
	{
		onExit();
	}

	@Override
	public void windowDeactivated(WindowEvent e)
	{
		// Nothing to do here
	}

	@Override
	public void windowDeiconified(WindowEvent e)
	{
		// Nothing to do here
	}

	@Override
	public void windowIconified(WindowEvent e)
	{
		// Nothing to do here
	}

	@Override
	public void windowOpened(WindowEvent e)
	{
		// Nothing to do here
	}
}
