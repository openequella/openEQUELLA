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

/**
 * @author Nicholas Read
 */
public class ControlTreeWalker
{
	private Control baseControl;

	/**
	 * Constructs a new ControlTreeWalker
	 */
	public ControlTreeWalker()
	{
		super();
	}

	/**
	 * @return Returns the base.
	 */
	public Control getBaseControl()
	{
		return baseControl;
	}

	/**
	 * Starts the tree walker at the given base control.
	 * 
	 * @param control the control to start at.
	 */
	public void execute(Control control)
	{
		if( control != null )
		{
			baseControl = control;
			if( onStart(control) )
			{
				processControl(control);
				onFinish(control);
			}
		}
	}

	/**
	 * Processes the current control.
	 * 
	 * @param control the current control.
	 */
	private void processControl(Control control)
	{
		if( onDescent(control) )
		{
			processChildren(control);
			onAscent(control);
		}
	}

	/**
	 * Processes the children of the current control.
	 * 
	 * @param control the current control.
	 */
	private void processChildren(Control control)
	{
		for( Control child : control.getChildren() )
		{
			if( forChild(control, child) )
			{
				processControl(child);
			}
		}
	}

	/**
	 * Called for each child of a control.
	 * 
	 * @param parent the parent control.
	 * @param child the child control.
	 * @return true to continue walking the child control.
	 */
	protected boolean forChild(Control parent, Control child)
	{
		return true;
	}

	/**
	 * Called once when starting a tree walker.
	 * 
	 * @param control the base control.
	 * @return true to continue walking over the rest of the tree.
	 */
	protected boolean onStart(Control control)
	{
		return true;
	}

	/**
	 * Called when the tree walking has completed.
	 * 
	 * @param control the base control.
	 */
	protected void onFinish(Control control)
	{
		// Nothing to do here
	}

	/**
	 * Called while descending the tree, ie, from the root to the leaves.
	 * 
	 * @param control the current control.
	 * @return true to continue walking over the children of this control. false
	 *         will skip all children, and will not invoke
	 *         <code>onAscent<code> for this control.
	 */
	protected boolean onDescent(Control control)
	{
		return true;
	}

	/**
	 * Called while ascending the tree, ie, from the leaves to the root.
	 * 
	 * @param control the current control.
	 * @return true to
	 */
	protected void onAscent(Control control)
	{
		// Nothing to do here
	}
}
