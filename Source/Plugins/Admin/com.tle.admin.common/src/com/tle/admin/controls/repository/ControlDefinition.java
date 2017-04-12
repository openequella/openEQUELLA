/*
 * Created on Apr 22, 2005
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
