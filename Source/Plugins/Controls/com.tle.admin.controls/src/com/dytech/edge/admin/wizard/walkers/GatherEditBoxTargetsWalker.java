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
import java.util.Collection;

import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.model.Control;

/**
 * @author Nicholas Read
 */
public class GatherEditBoxTargetsWalker extends ControlTreeWalker
{
	private final Collection<String> targets = new ArrayList<String>();

	/**
	 * Constructs a new TreeToXmlWalker.
	 */
	public GatherEditBoxTargetsWalker()
	{
		super();
	}

	/**
	 * @return Returns the targets.
	 */
	public Collection<String> getTargets()
	{
		return targets;
	}

	@Override
	protected boolean onDescent(Control control)
	{
		if( WizardHelper.isEditBox(control) )
		{
			targets.addAll(control.getTargets());
		}
		return true;
	}
}
