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

package com.tle.admin.plugin;

import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.ComponentHelper;
import com.dytech.gui.JStatusBar;
import com.dytech.gui.TableLayout;
import com.tle.admin.Driver;
import com.tle.admin.common.gui.EditorHelper;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.EditorInterface;
import com.tle.admin.gui.common.JChangeDetectorPanel;
import com.tle.admin.gui.common.actions.CloseAction;
import com.tle.admin.gui.common.actions.SaveAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public abstract class PluginDialog<T, S extends PluginSetting> extends JDialog implements EditorInterface
{
	private static final Log LOGGER = LogFactory.getLog(PluginDialog.class);
	private static final int WINDOW_WIDTH = 600;
	private static final int WINDOW_HEIGHT = 500;

	private final GeneralPlugin<T> plugin;
	protected final S setting;
	private final String windowTitle;

	private JStatusBar statusbar;

	private ChangeDetector detector;
	private boolean saved = false;

	public PluginDialog(Frame frame, String title, S setting, GeneralPlugin<T> plugin)
	{
		super(frame);
		windowTitle = title;
		this.setting = setting;
		this.plugin = plugin;
		detector = new ChangeDetector();
	}

	public void setup()
	{
		createGUI();
		load();
	}

	private void createGUI()
	{
		JLabel title = new JLabel(setting.getName());
		Font f = title.getFont();
		title.setFont(new Font(f.getName(), Font.BOLD, f.getSize() * 2));

		String icon = setting.getIcon();
		if( !Check.isEmpty(icon) )
		{
			title.setIcon(new ImageIcon(getClass().getResource(icon)));
		}
		JButton help = new JButton(CurrentLocale.get("com.tle.admin.plugin.plugindialog.help")); //$NON-NLS-1$
		if( plugin.getHelp() != null )
		{
			help.addActionListener(new HelpListener(this.getParentWindow(), CurrentLocale
				.get("com.tle.admin.plugin.plugindialog.general"), plugin.getHelp())); //$NON-NLS-1$
		}

		JButton save = new JButton(saveAction);
		JButton close = new JButton(closeAction);

		plugin.setParent(this);
		plugin.setClientService(Driver.instance().getClientService());
		plugin.init();

		final int height1 = title.getPreferredSize().height;
		final int height2 = save.getPreferredSize().height;
		final int width1 = Math.max(save.getPreferredSize().width, close.getPreferredSize().width);

		final int[] rows = {height1, TableLayout.FILL, height2};
		final int[] cols = {TableLayout.FILL, width1, width1, width1};

		JChangeDetectorPanel changePanel = new JChangeDetectorPanel();
		changePanel.setLayout(new TableLayout(rows, cols, 5, 5));
		changePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		changePanel.add(title, new Rectangle(0, 0, 4, 1));
		changePanel.add(plugin.getComponent(), new Rectangle(0, 1, 4, 1));
		changePanel.add(help, new Rectangle(1, 2, 1, 1));
		if( plugin.hasSave() )
		{
			changePanel.add(save, new Rectangle(2, 2, 1, 1));
		}
		changePanel.add(close, new Rectangle(3, 2, 1, 1));

		statusbar = new JStatusBar(EditorHelper.getStatusBarSpinner());

		JPanel all = statusbar.attachToPanel(changePanel);

		getContentPane().add(all);
		setTitle(windowTitle + " - " + setting.getName()); //$NON-NLS-1$

		int width = WINDOW_WIDTH;
		int height = WINDOW_HEIGHT;
		if( setting.getWidth() > 0 )
		{
			width = setting.getWidth();
		}
		if( setting.getHeight() > 0 )
		{
			height = setting.getHeight();
		}
		setSize(width, height);

		help.setVisible(plugin.getHelp() != null);

		EditorHelper.listenForClosing(this, this);

		ComponentHelper.centreOnScreen(this);

		detector.watch(changePanel);
	}

	private void load()
	{
		try
		{
			_load(plugin);
			detector.clearChanges();
		}
		catch( Exception ex )
		{
			saveAction.setEnabled(false);
			LOGGER.error("Error", ex);
			Driver.displayError(this, "system.settings/loading", ex); //$NON-NLS-1$

		}
		finally
		{
			detector.setIgnoreChanges(false);
		}
	}

	private TLEAction saveAction = new SaveAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			EditorHelper.onSave(PluginDialog.this);
		}
	};

	private TLEAction closeAction = new CloseAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			EditorHelper.onClose(PluginDialog.this, null);
		}
	};

	/**
	 * Changeable interface method
	 */
	@Override
	public boolean hasDetectedChanges()
	{
		return detector.hasDetectedChanges();
	}

	/**
	 * Changeable interface method
	 */
	@Override
	public void clearChanges()
	{
		detector.clearChanges();
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.edge.admin.gui.EditorInterface#close()
	 */
	@Override
	public void close(Integer confirmOption)
	{
		dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.edge.admin.gui.EditorInterface#getDocumentName()
	 */
	@Override
	public String getDocumentName()
	{
		if( plugin.getDocumentName() != null )
		{
			return plugin.getDocumentName();
		}
		return setting.getName();
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.edge.admin.gui.EditorInterface#getParentWindow()
	 */
	@Override
	public Component getParentWindow()
	{
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.edge.admin.gui.EditorInterface#isReadOnly()
	 */
	@Override
	public boolean isReadOnly()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.edge.admin.gui.EditorInterface#save()
	 */
	@Override
	public void save() throws EditorException
	{
		try
		{
			_save(plugin);
			detector.clearChanges();
			saved = true;
		}
		catch( EditorException ex ) // NOSONAR - rethrow, no conversion
		{
			throw ex;
		}
		catch( Exception ex )
		{
			LOGGER.error("Problem saving system settings", ex); //$NON-NLS-1$
			throw new EditorException("system.settings/saving", ex); //$NON-NLS-1$
		}
	}

	public boolean saved()
	{
		return saved;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.edge.admin.gui.EditorInterface#validation()
	 */
	@Override
	public void validation() throws EditorException
	{
		plugin.validation();
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.edge.admin.gui.EditorInterface#unlock()
	 */
	@Override
	public void unlock()
	{
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.edge.admin.gui.EditorInterface#getStatusBar()
	 */
	@Override
	public JStatusBar getStatusBar()
	{
		return statusbar;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.edge.admin.gui.EditorInterface#beforeSaving()
	 */
	@Override
	public boolean beforeSaving()
	{
		return true;
	}

	protected abstract void _load(GeneralPlugin<T> gplugin);

	protected abstract void _save(GeneralPlugin<T> gplugin) throws EditorException;
}
