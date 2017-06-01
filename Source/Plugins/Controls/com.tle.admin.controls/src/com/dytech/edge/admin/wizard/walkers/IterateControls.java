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

package com.dytech.edge.admin.wizard.walkers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.dytech.edge.admin.wizard.model.Control;

/**
 * @author Nicholas Read
 */
public class IterateControls extends ControlTreeWalker
{
	private final List<Control> controls = new ArrayList<Control>();

	/**
	 * Constructs a new IterateControls.
	 */
	public IterateControls()
	{
		super();
	}

	/**
	 * @return Returns the targets.
	 */
	public Iterator<Control> iterate()
	{
		return controls.iterator();
	}

	public List<Control> getControls()
	{
		return controls;
	}

	@Override
	protected boolean onDescent(Control control)
	{
		controls.add(control);
		return true;
	}
}
