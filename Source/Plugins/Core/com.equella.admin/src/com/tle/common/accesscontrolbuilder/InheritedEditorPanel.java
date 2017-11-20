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

package com.tle.common.accesscontrolbuilder;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;

import com.dytech.gui.JLinkButton;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.remoting.RemoteTLEAclManager;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class InheritedEditorPanel extends JComponent implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private final Object domainObj;
	private final String privilege;

	private JLinkButton showAll;
	private final RemoteTLEAclManager aclManager;
	private final RemoteUserService userService;

	public InheritedEditorPanel(RemoteTLEAclManager aclManager, RemoteUserService userService, Object domainObj,
		String privNode)
	{
		this.aclManager = aclManager;
		this.userService = userService;
		this.domainObj = domainObj;
		this.privilege = privNode;

		setupGui();
	}

	private void setupGui()
	{
		showAll = new JLinkButton(CurrentLocale.get("security.editor.showAllPrivileges")); //$NON-NLS-1$
		showAll.addActionListener(this);

		setLayout(new BorderLayout());
		add(showAll, BorderLayout.NORTH);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == showAll )
		{
			GlassSwingWorker<JComponent> worker = new GlassSwingWorker<JComponent>()
			{
				@Override
				public JComponent construct() throws Exception
				{
					return new OverrideDefaultAclViewer(aclManager, userService, domainObj, privilege);
				}

				@Override
				public void finished()
				{
					showAll.setEnabled(false);

					InheritedEditorPanel.this.add(get(), BorderLayout.CENTER);
					InheritedEditorPanel.this.updateUI();
				}

				@Override
				public void exception()
				{
					getException().printStackTrace();
				}
			};
			worker.setComponent(this);
			worker.start();
		}
	}
}
