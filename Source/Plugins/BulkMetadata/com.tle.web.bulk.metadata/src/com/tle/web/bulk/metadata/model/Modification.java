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
