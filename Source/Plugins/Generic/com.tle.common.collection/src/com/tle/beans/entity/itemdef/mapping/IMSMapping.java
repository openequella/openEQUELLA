/*
 * Created on Jun 23, 2005
 */
package com.tle.beans.entity.itemdef.mapping;

import java.io.Serializable;

/**
 * Is actually a generic 'package mapping' but a rename would cause issues with
 * already serialised data.
 */
public class IMSMapping implements Serializable
{
	private static final long serialVersionUID = 1;

	public enum MappingType
	{
		SIMPLE, COMPOUND, REPEAT
	}

	private String ims;
	private String itemdef;
	private String type;
	private boolean replace;

	private boolean repeat;

	public IMSMapping()
	{
		ims = ""; //$NON-NLS-1$
		itemdef = ""; //$NON-NLS-1$
		type = ""; //$NON-NLS-1$
		replace = true;
	}

	public boolean isSimple()
	{
		return MappingType.SIMPLE.toString().equalsIgnoreCase(type);
	}

	public boolean isRepeat()
	{
		return MappingType.REPEAT.toString().equalsIgnoreCase(type) || repeat;
	}

	public boolean isCompound()
	{
		return MappingType.COMPOUND.toString().equalsIgnoreCase(type);
	}

	public boolean isReplace()
	{
		return replace;
	}

	public void setReplace(boolean replace)
	{
		this.replace = replace;
	}

	public String getIms()
	{
		return ims;
	}

	public void setIms(String ims)
	{
		this.ims = ims;
	}

	public String getItemdef()
	{
		return itemdef;
	}

	public void setItemdef(String itemdef)
	{
		this.itemdef = itemdef;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}
}
