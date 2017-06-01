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

package com.tle.admin.common.gui.tree;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.tle.admin.gui.EditorException;
import com.tle.common.LazyTreeNode;

/**
 * @author Nicholas Read
 */
public class BasicMessageEditor extends AbstractTreeNodeEditor
{
	public BasicMessageEditor(String message)
	{
		JLabel messageLabel = new JLabel(message);
		messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

		setLayout(new GridLayout(1, 1));
		add(messageLabel);
	}

	// None of the abstract methods should be invoked, as this editor is does
	// nothing.

	@Override
	protected LazyTreeNode getUpdatedNode()
	{
		throw new RuntimeException();
	}

	@Override
	protected void save()
	{
		throw new RuntimeException();
	}

	@Override
	protected void validation() throws EditorException
	{
		// nothing to validate
	}
}
