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

import java.awt.Component;

import javax.swing.JOptionPane;

import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.admin.wizard.walkers.FindOtherPagesWithTarget;
import com.tle.admin.schema.TargetListener;
import com.tle.common.i18n.CurrentLocale;

public class CheckDuplicateTargetsHandler implements TargetListener
{
	private final Component parent;
	private final Control control;

	public CheckDuplicateTargetsHandler(Component parent, Control control)
	{
		this.parent = parent;
		this.control = control;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.dytech.edge.admin.schema.TargetListener#targetAdded(java.lang.String)
	 */
	@Override
	public void targetAdded(String target)
	{
		Control page = WizardHelper.getPage(control);
		if( page != null )
		{
			Control root = WizardHelper.getRoot(page);
			if( root != null )
			{
				FindOtherPagesWithTarget walker = new FindOtherPagesWithTarget(target, page);
				walker.execute(root);

				if( walker.targetFoundOnOtherPage() )
				{
					JOptionPane.showMessageDialog(parent, CurrentLocale.get("wizard.prompt.targetonmanypages"));
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.dytech.edge.admin.schema.TargetListener#targetRemoved(java.lang.String
	 * )
	 */
	@Override
	public void targetRemoved(String target)
	{
		// We don't care about this event.
	}
}
