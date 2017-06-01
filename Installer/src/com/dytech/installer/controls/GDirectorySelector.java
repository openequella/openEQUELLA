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

package com.dytech.installer.controls;

import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.InstallerException;

public class GDirectorySelector extends GResourceSelector
{
	public GDirectorySelector(PropBagEx controlBag) throws InstallerException
	{
		super(controlBag);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle(title);

		int returnVal = chooser.showOpenDialog(field);
		if( returnVal == JFileChooser.APPROVE_OPTION )
		{
			field.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}
}
