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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.ComponentHelper;
import com.dytech.gui.JShuffleEditList;
import com.dytech.gui.JShuffleEditList.DefaultShuffleComponent;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.gui.common.actions.CancelAction;
import com.tle.admin.gui.common.actions.OkAction;
import com.tle.beans.usermanagement.standard.LDAPSettings;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteLDAPService;

@SuppressWarnings("nls")
public class ManageDNsDialog
{
	private JPanel all;
	private JShuffleEditList<String> baseList;

	private JDialog dialog;
	private LDAPSettings settings;

	public ManageDNsDialog(final RemoteLDAPService ldapService)
	{
		DefaultShuffleComponent shuffleComp = new DefaultShuffleComponent("");
		baseList = new JShuffleEditList<String>(shuffleComp, s("enter"), false);
		shuffleComp.setParent(baseList);

		JButton fetch = new JButton(s("fetch"));
		fetch.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				GlassSwingWorker<?> worker = new GlassSwingWorker<List<String>>()
				{
					@Override
					public List<String> construct() throws Exception
					{
						return ldapService.getDNs(settings);
					}

					@Override
					public void finished()
					{
						final List<String> foundBases = get();
						foundBases.removeAll(baseList.getItems());
						baseList.getModel().addAll(foundBases);

						final int count = foundBases.size();
						JOptionPane.showMessageDialog(getComponent(),
							count == 1 ? s("fetch.found.singular") : s("fetch.found.plural", count));
					}

					@Override
					public void exception()
					{
						Driver.displayError(getComponent(), "com.tle.admin.usermanagement.standard.ldap.general.error",
							"com.tle.admin.usermanagement.standard.ldap.general.check", getException());
					}
				};
				worker.setComponent(dialog);
				worker.start();
			}
		});

		all = new JPanel(new MigLayout("wrap 1,fill", "[fill]", "[fill,grow][][]"));
		all.add(baseList);
		all.add(fetch, "alignx left");
		all.add(new JButton(new OkAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				settings.setBases(new ArrayList<String>(baseList.getItems()));
				dialog.dispose();
			}
		}), "gaptop unrelated, split, alignx right, tag ok");
		all.add(new JButton(new CancelAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dialog.dispose();
			}
		}), "tag cancel");
	}

	public void show(Component parent, LDAPSettings settings)
	{
		this.settings = settings;

		dialog = ComponentHelper.createJDialog(parent);
		dialog.setTitle(s("title"));
		dialog.setContentPane(all);
		dialog.setModal(true);
		dialog.setResizable(false);

		dialog.pack();
		ComponentHelper.ensureMinimumSize(dialog, 450, 0);

		ComponentHelper.centreOnScreen(dialog);

		dialog.setVisible(true);
	}

	private static String s(String keypart, Object... values)
	{
		return CurrentLocale.get("com.tle.admin.usermanagement.standard.ldap.managedns." + keypart, values);
	}
}
