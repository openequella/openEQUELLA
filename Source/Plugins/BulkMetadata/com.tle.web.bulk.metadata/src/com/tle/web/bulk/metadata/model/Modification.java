package com.tle.web.bulk.metadata.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


public class Modification implements Serializable
{
	public static enum ModificationKeys
	{
		ACTION, SET_TEXT, SET_TEXT_OPTION, REPLACE_FIND, REPLACE_WITH, ADD_XML
	}

	private String info;
	private List<String> nodes;
	private String nodeDisplay;
	private Map<ModificationKeys, String> parmas;

	public Modification(List<String> nodes, String info, String nodeDisplay, Map<ModificationKeys, String> parmas)
	{
		this.nodes = nodes;
		this.info = info;
		this.nodeDisplay = nodeDisplay;
		this.parmas = parmas;
	}

	public Modification()
	{
		// prevent 400 error
	}

	public List<String> getNodes()
	{
		return nodes;
	}

	public String getNodeDisplay()
	{
		return nodeDisplay;
	}

	public void setNodes(List<String> nodes)
	{
		this.nodes = nodes;
	}

	public void setNodeDisplay(String nodeDisplay)
	{
		this.nodeDisplay = nodeDisplay;
	}

	public String getInfo()
	{
		return info;
	}

	public void setInfo(String info)
	{
		this.info = info;
	}

	public Map<ModificationKeys, String> getParmas()
	{
		return parmas;
	}

	public void setParmas(Map<ModificationKeys, String> parmas)
	{
		this.parmas = parmas;
	}

}
