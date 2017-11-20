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

package com.tle.admin.harvester.tool;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.common.DateSelector;
import com.tle.admin.gui.common.JNameValuePanel;
import com.tle.admin.harvester.standard.HarvesterPlugin;
import com.tle.common.harvester.HarvesterProfile;
import com.tle.common.harvester.RemoteHarvesterProfileService;
import com.tle.common.i18n.CurrentLocale;

@SuppressWarnings("nls")
public class HarvesterActionsTab extends BaseEntityTab<HarvesterProfile> implements ActionListener
{
	private HarvesterPlugin<?> plugin;
	protected JNameValuePanel namePanel;

	private final JButton runBtn;
	private final JButton testBtn;

	private final HarvesterProfileEditor editor;

	private final DateSelector dateSelect;

	@Override
	public void setDriver(Driver driver)
	{
		super.setDriver(driver);
		plugin.setDriver(driver);
	}

	public HarvesterActionsTab(HarvesterProfileEditor editor)
	{
		dateSelect = new DateSelector();

		JPanel runPanel = new JPanel();
		JPanel testPanel = new JPanel();

		runBtn = new JButton(getString("actionstab.run"));
		testBtn = new JButton(getString("actionstab.test"));

		runBtn.addActionListener(this);
		testBtn.addActionListener(this);

		putBtnOnPanel(runPanel, runBtn);
		putBtnOnPanel(testPanel, testBtn);

		namePanel = new JNameValuePanel();

		namePanel.addNameAndComponent(getString("actionstab.lastdate"), dateSelect);
		namePanel.addNameAndComponent(getString("actionstab.testlabel"), testPanel);
		namePanel.addNameAndComponent(getString("actionstab.runnow"), runPanel);

		this.editor = editor;
	}

	private void putBtnOnPanel(JPanel aPanel, JButton aButton)
	{
		final int width = aButton.getPreferredSize().width;
		final int height = aButton.getPreferredSize().height;

		final int[] rows = {height};
		final int[] cols = {width, TableLayout.FILL};

		aPanel.setLayout(new TableLayout(rows, cols, 5, 5));
		aPanel.add(aButton, new Rectangle(0, 0, 1, 1));
	}

	@Override
	public String getTitle()
	{
		return getString("actionstab.name");
	}

	@Override
	public void init(Component parent)
	{
		TableLayout layout = new TableLayout(new int[]{TableLayout.FILL}, new int[]{TableLayout.DOUBLE_FILL,
				TableLayout.FILL});
		layout.setColumnSize(1, 0);
		setLayout(layout);
		add(namePanel.getComponent(), new Rectangle(0, 0, 1, 1));
	}

	@Override
	public void load()
	{
		final HarvesterProfile havProfile = state.getEntity();

		Date lastRun = havProfile.getLastRun();
		if( lastRun == null )
		{
			lastRun = new Date(0);
		}

		dateSelect.setDate(lastRun);
	}

	@Override
	public void save()
	{
		final HarvesterProfile havProfile = state.getEntity();

		Date lastRun = dateSelect.getDate();
		if( lastRun == null )
		{
			lastRun = new Date(0);
		}

		havProfile.setLastRun(lastRun);
	}

	public void setPlugin(HarvesterPlugin<?> plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public void validation() throws EditorException
	{
		// Nothing to do here
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();
		if( src.equals(runBtn) )
		{
			int option = JOptionPane.showConfirmDialog(namePanel.getComponent(), getString("alert"), getString("sure"),
				JOptionPane.YES_NO_OPTION);

			if( option == JOptionPane.YES_OPTION )
			{
				if( validateAndSave() )
				{
					final HarvesterProfile havProfile = state.getEntity();

					RemoteHarvesterProfileService harvesterService = clientService
						.getService(RemoteHarvesterProfileService.class);
					harvesterService.startHarvesterTask(havProfile.getUuid(), true);

					JOptionPane.showMessageDialog(namePanel.getComponent(), getString("running"));
				}
			}
		}
		else if( src.equals(testBtn) )
		{
			testCurrentHarvester();
		}
	}

	private void testCurrentHarvester()
	{
		if( validateAndSave() )
		{
			final HarvesterProfile havProfile = state.getEntity();
			final RemoteHarvesterProfileService harvesterService = clientService
				.getService(RemoteHarvesterProfileService.class);

			GlassSwingWorker<?> worker = new GlassSwingWorker<Integer>()
			{
				@Override
				public Integer construct() throws Exception
				{
					return harvesterService.testProfile(havProfile.getUuid());
				}

				@Override
				public void finished()
				{
					JOptionPane.showMessageDialog(namePanel.getComponent(), getString("testpass", get()));
				}

				@Override
				public void exception()
				{
					Exception ex = getException();
					ex.printStackTrace();
					Driver.displayInformation(HarvesterActionsTab.this.getComponent(),
						getString("testfailed", ex.getMessage()));
				}
			};
			worker.setComponent(namePanel.getComponent());
			worker.start();
		}
	}

	private boolean validateAndSave()
	{
		try
		{
			editor.validation();
			editor.save();
			return true;
		}
		catch( EditorException ex )
		{
			JOptionPane.showMessageDialog(namePanel.getComponent(), ex.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * @param partKey Will be prefixed by
	 *            com.tle.admin.harvester.tool.actionstab.
	 * @param params
	 * @return
	 */
	private String getString(String partKey, Object... params)
	{
		return getString("actionstab." + partKey, params);
	}
}
