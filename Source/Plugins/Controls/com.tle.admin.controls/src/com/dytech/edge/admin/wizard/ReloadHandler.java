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

package com.dytech.edge.admin.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import com.tle.common.i18n.CurrentLocale;

public class ReloadHandler implements ActionListener
{
	private JCheckBox reload = null;

	public ReloadHandler(JCheckBox refresh)
	{
		this.reload = refresh;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( reload.isSelected() )
		{
			int result = JOptionPane.showConfirmDialog(reload,
				CurrentLocale.get("com.dytech.edge.admin.wizard.reloadhandler.selecting"),
				CurrentLocale.get("com.dytech.edge.admin.wizard.reloadhandler.warning"), JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);

			if( result == JOptionPane.NO_OPTION )
			{
				reload.setSelected(false);
			}
		}
	}
}
