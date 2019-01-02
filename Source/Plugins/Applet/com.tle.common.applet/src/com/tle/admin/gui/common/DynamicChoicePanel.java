/*
 * Copyright 2019 Apereo
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

package com.tle.admin.gui.common;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.Changeable;

public abstract class DynamicChoicePanel<STATE_TYPE> extends JPanel implements Changeable, ActionListener
{
	protected final ChangeDetector changeDetector = new ChangeDetector();
	private final JComponent separator = new JSeparator();
	private String id;

	public DynamicChoicePanel()
	{
		super();
	}

	public DynamicChoicePanel(LayoutManager lm)
	{
		super(lm);
	}

	public final String getId()
	{
		return id;
	}

	public final void setId(String id)
	{
		this.id = id;
	}

	public void choiceSelected()
	{
		// Nothing by default
	}

	public void choiceDeselected()
	{
		// Nothing by default
	}

	@Override
	public void clearChanges()
	{
		changeDetector.clearChanges();
	}

	@Override
	public boolean hasDetectedChanges()
	{
		return changeDetector.hasDetectedChanges();
	}

	public JComponent getSeparator()
	{
		return separator;
	}

	public abstract void load(STATE_TYPE state);

	public void afterLoad(STATE_TYPE state)
	{
		// Nothing by default
	}

	public abstract void save(STATE_TYPE state);

	public abstract void removeSavedState(STATE_TYPE state);

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// blah
	}
}