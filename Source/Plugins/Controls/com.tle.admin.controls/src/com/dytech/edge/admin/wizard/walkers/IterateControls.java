/*
 * Created on Apr 26, 2005
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
