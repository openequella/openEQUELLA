/*
 * Created on Apr 26, 2005
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
