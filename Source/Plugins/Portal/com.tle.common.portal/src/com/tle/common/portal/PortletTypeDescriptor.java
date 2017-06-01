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

package com.tle.common.portal;

import java.io.Serializable;

import com.tle.common.security.PrivilegeTree.Node;

/**
 * Just a simple bean for transport around the place
 * 
 * @author aholland
 */
public class PortletTypeDescriptor implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String type;
	private final String nameKey;
	private final String descriptionKey;
	private final Node node;
	private final PortletTypeTarget target;

	public PortletTypeDescriptor(String type, String nameKey, String descriptionKey, Node node)
	{
		this.type = type;
		this.nameKey = nameKey;
		this.descriptionKey = descriptionKey;
		this.node = node;
		this.target = new PortletTypeTarget(type);
	}

	public String getType()
	{
		return type;
	}

	public String getNameKey()
	{
		return nameKey;
	}

	public String getDescriptionKey()
	{
		return descriptionKey;
	}

	public Node getNode()
	{
		return node;
	}

	public PortletTypeTarget getTarget()
	{
		return target;
	}
}
