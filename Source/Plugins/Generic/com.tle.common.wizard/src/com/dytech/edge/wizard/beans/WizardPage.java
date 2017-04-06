/*
 * Created on Jun 22, 2005
 */
package com.dytech.edge.wizard.beans;

import java.io.Serializable;

public abstract class WizardPage implements Serializable
{
	private static final long serialVersionUID = 1;

	private String script;

	public abstract String getType();

	public String getScript()
	{
		return script;
	}

	public void setScript(String script)
	{
		this.script = script;
	}
}
