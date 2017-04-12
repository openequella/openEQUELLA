/*
 * Created on Jun 22, 2005
 */
package com.dytech.edge.wizard.beans.control;

public class Button extends WizardControl
{
	private static final long serialVersionUID = 1L;

	public static final String CLASS = "button"; //$NON-NLS-1$

	private String action;

	@Override
	public String getClassType()
	{
		return CLASS;
	}

	public String getAction()
	{
		return action;
	}

	public void setAction(String actionScript)
	{
		this.action = actionScript;
	}
}
