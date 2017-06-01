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

import com.dytech.edge.admin.wizard.model.Control;
import com.google.common.base.Objects;

/**
 * @author Nicholas Read
 */
public class FindOtherPagesWithTarget extends ControlTreeWalker
{
	private boolean found;
	private Control ignorePage;
	private String target;

	/**
	 * Constructs a new FindTargets
	 */
	public FindOtherPagesWithTarget(String target, Control ignorePage)
	{
		this.target = target;
		this.ignorePage = ignorePage;
	}

	/**
	 * @return Returns the found.
	 */
	public boolean targetFoundOnOtherPage()
	{
		return found;
	}

	@Override
	protected boolean onDescent(Control control)
	{
		// Don't descend if we're ignoring this page, or we have already found a
		// page.
		if( Objects.equal(control, ignorePage) || found )
		{
			return false;
		}
		if( control.getTargets().contains(target) )
		{
			found = true;
		}
		// Continue if no target found yet.
		return !found;

	}
}
