/*
 * Created on Jun 22, 2005
 */
package com.dytech.edge.wizard.beans.control;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractControlsWizardControl extends WizardControl implements ContainerControl
{
	private static final long serialVersionUID = 1;

	private final List<WizardControl> controls = new ArrayList<WizardControl>();

	@Override
	public List<WizardControl> getControls()
	{
		return controls;
	}
}
