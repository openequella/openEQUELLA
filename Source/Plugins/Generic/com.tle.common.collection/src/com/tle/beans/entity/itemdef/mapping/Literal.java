/*
 * Created on Jun 23, 2005
 */
package com.tle.beans.entity.itemdef.mapping;

import java.io.Serializable;

public class Literal implements Serializable
{
	private static final long serialVersionUID = 1;

	private String value;
	private String script;

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public String getScript()
	{
		return script;
	}

	public void setScript(String script)
	{
		this.script = script;
	}
}
