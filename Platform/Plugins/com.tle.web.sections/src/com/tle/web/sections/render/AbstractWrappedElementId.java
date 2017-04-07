package com.tle.web.sections.render;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.generic.SimpleElementId;

public abstract class AbstractWrappedElementId implements ElementId
{
	private ElementId elementId;
	private boolean used;
	private boolean changed;

	public AbstractWrappedElementId(ElementId elementId)
	{
		this.elementId = elementId;
	}

	@Override
	public String getElementId(SectionInfo info)
	{
		return elementId.getElementId(info);
	}

	public ElementId getWrappedElementId()
	{
		return elementId;
	}

	public String getId()
	{
		if( !elementId.isStaticId() )
		{
			throw new SectionsRuntimeException("Not a static id, use getElementId(SectionInfo) instead"); //$NON-NLS-1$
		}
		return elementId.getElementId(null);
	}

	public void setId(String id)
	{
		elementId = new SimpleElementId(id);
		registerUse();
	}

	public void setElementId(ElementId elementId)
	{
		this.elementId = elementId;
		if( used )
		{
			elementId.registerUse();
		}
		changed = true;
	}

	public boolean hasIdBeenSet()
	{
		return changed;
	}

	@Override
	public void registerUse()
	{
		used = true;
		elementId.registerUse();
	}

	@Override
	public boolean isElementUsed()
	{
		return used || elementId.isElementUsed();
	}

	@Override
	public boolean isStaticId()
	{
		return elementId.isStaticId();
	}

}
