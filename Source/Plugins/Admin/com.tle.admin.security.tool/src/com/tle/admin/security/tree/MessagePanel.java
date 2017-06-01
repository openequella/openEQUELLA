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

package com.tle.admin.security.tree;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * @author Nicholas Read
 */
public class MessagePanel extends JPanel implements SecurityTreeTab
{
	private static final long serialVersionUID = 1L;

	public MessagePanel(String message)
	{
		JLabel label = new JLabel(message);
		label.setHorizontalAlignment(SwingConstants.CENTER);

		setLayout(new GridLayout(1, 1));
		add(label);
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.admin.security.tree.SecurityTreeTab#hasChanges()
	 */
	@Override
	public boolean hasChanges()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.admin.security.tree.SecurityTreeTab#saveChanges()
	 */
	@Override
	public void saveChanges()
	{
		// Nothing to save
	}
}
