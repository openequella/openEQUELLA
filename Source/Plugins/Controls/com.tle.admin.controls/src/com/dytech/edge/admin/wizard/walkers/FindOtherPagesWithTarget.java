/*
 * Created on Apr 22, 2005
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
