/*
 * Created on Jun 22, 2005
 */
package com.dytech.edge.wizard.beans;

import java.io.Serializable;

public class Metadata implements Serializable
{
	private static final long serialVersionUID = 1;

	private String target;
	private String value;

	public Metadata(String target, String value)
	{
		this.target = target;
		this.value = value;
	}

	public String getTarget()
	{
		return target;
	}

	public void setTarget(String target)
	{
		this.target = target;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}
}
