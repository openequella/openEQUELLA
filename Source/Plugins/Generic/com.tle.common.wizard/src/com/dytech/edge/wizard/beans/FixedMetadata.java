/*
 * Created on Jun 22, 2005
 */
package com.dytech.edge.wizard.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FixedMetadata implements Serializable
{
	private static final long serialVersionUID = 1;

	private List<Metadata> data = new ArrayList<Metadata>();
	private String script;

	public String getScript()
	{
		return script;
	}

	public void setScript(String script)
	{
		this.script = script;
	}

	public List<Metadata> getData()
	{
		return data;
	}
	
	
}
