package com.tle.beans.entity.itemdef;

import java.io.Serializable;
import java.util.Objects;

import com.tle.common.Check;

/**
 * @author Nicholas Read
 */
public class ItemMetadataRule implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String id;
	private String name;
	private String script;

	public ItemMetadataRule()
	{
		super();
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

	public String getScript()
	{
		return script;
	}

	public void setScript(String script)
	{
		this.script = script;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof ItemMetadataRule) )
		{
			return false;
		}

		return Objects.equals(id, ((ItemMetadataRule) obj).id);
	}

	@Override
	public int hashCode()
	{
		return Check.getHashCode(id, name, script);
	}
}
