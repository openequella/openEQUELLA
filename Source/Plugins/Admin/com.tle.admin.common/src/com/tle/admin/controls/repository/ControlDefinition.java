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

package com.tle.admin.controls.repository;

import java.util.HashSet;
import java.util.Set;

import org.java.plugin.registry.Extension;

/**
 * @author Nicholas Read
 */
public class ControlDefinition
{
	private Set<String> contexts = new HashSet<String>();

	private String id;
	private String name;
	private String editorFactoryClass;
	private Extension extension;

	public ControlDefinition()
	{
		super();
	}

	public Extension getExtension()
	{
		return extension;
	}

	public void setExtension(Extension extension)
	{
		this.extension = extension;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return getName();
	}

	public String getEditorFactoryClass()
	{
		return editorFactoryClass;
	}

	public void setEditorFactoryClass(String editorFactoryClass)
	{
		this.editorFactoryClass = editorFactoryClass;
	}

	public Set<String> getContexts()
	{
		return contexts;
	}

	public void setContexts(Set<String> contexts)
	{
		this.contexts = contexts;
	}

	public boolean hasContext(String context)
	{
		return contexts.contains(context);
	}
}
