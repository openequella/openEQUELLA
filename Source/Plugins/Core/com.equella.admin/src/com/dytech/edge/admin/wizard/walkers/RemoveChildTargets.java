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

import com.dytech.edge.admin.wizard.model.AbstractControlModel;
import com.dytech.edge.admin.wizard.model.Control;

/**
 * @author Nicholas Read
 */
public class RemoveChildTargets extends ControlTreeWalker
{
	private boolean removedTargets = false;

	/**
	 * Constructs a new TreeToXmlWalker.
	 */
	public RemoveChildTargets()
	{
		super();
	}

	/**
	 * @return Returns the removedTargets.
	 */
	public boolean hasRemovedTargets()
	{
		return removedTargets;
	}

	@Override
	protected boolean onDescent(Control control)
	{
		// Check that we are not performing this on the top control.
		if( control != getBaseControl() )
		{
			// See if there are any targets to remove
			removedTargets = removedTargets || !control.getTargets().isEmpty();

			// Remove any targets
			if( control instanceof AbstractControlModel )
			{
				((AbstractControlModel<?>) control).getControl().getTargetnodes().clear();
			}
		}
		return true;
	}
}
