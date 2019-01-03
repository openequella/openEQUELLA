/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.dytech.installer.controls;

import java.awt.Component;

import javax.swing.JComponent;

import com.dytech.devlib.PropBagEx;
import com.dytech.gui.JShuffleBox;
import com.dytech.installer.InstallerException;

public class GShuffleBox extends GuiControl
{
	protected JShuffleBox box;

	public GShuffleBox(PropBagEx controlBag) throws InstallerException
	{
		super(controlBag);
	}

	@Override
	public String getSelection()
	{
		StringBuilder buff = new StringBuilder();

		int count = box.getRightCount();
		for( int i = 0; i < count; i++ )
		{
			buff.append(box.getRightAt(i).toString());
			buff.append(", ");
		}

		if( buff.length() == 0 )
			return buff.toString();
		else
			return buff.substring(0, buff.lastIndexOf(","));
	}

	@Override
	public JComponent generateControl()
	{
		box = new JShuffleBox();
		box.addToLeft(items);
		box.setAlignmentX(Component.LEFT_ALIGNMENT);

		return box;
	}
}