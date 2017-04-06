/*
 * Created on Apr 22, 2005
 */
package com.dytech.edge.admin.wizard.model;

import java.util.List;

import com.dytech.edge.wizard.beans.FixedMetadata;
import com.dytech.edge.wizard.beans.Metadata;
import com.tle.admin.controls.repository.ControlDefinition;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class MetadataModel extends BasicAbstractControl
{
	private FixedMetadata fixed;

	/**
	 * Constructs a new MetadataModel
	 */
	public MetadataModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public void setWrappedObject(Object wrappedObject)
	{
		super.setWrappedObject(wrappedObject);
		this.fixed = (FixedMetadata) wrappedObject;
	}

	/**
	 * @return Returns the metadata.
	 */
	public List<Metadata> getMetadata()
	{
		return fixed.getData();
	}

	@Override
	public String getControlClass()
	{
		return "metadata";
	}

	@Override
	public Object save()
	{
		return fixed;
	}

	@Override
	public void setScript(String script)
	{
		fixed.setScript(script);
	}

	@Override
	public String getScript()
	{
		return fixed.getScript();
	}

}
