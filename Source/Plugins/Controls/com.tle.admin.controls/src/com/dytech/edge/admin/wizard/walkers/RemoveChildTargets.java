/*
 * Created on Apr 26, 2005
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
