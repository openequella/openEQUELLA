package com.tle.web.sections.js.generic;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.js.ElementId;

public class AppendedElementId implements ElementId
{
	private final ElementId basedOn;
	private final ElementId postfix;
	private boolean used;

	public AppendedElementId(ElementId basedOn, String postfix)
	{
		this.basedOn = basedOn;
		this.postfix = new SimpleElementId(postfix);
	}

	public AppendedElementId(ElementId basedOn, ElementId postfix)
	{
		this.basedOn = basedOn;
		this.postfix = postfix;
	}

	@Override
	public void registerUse()
	{
		used = true;
	}

	@Override
	public boolean isElementUsed()
	{
		return used;
	}

	@Override
	public String getElementId(SectionInfo info)
	{
		return basedOn.getElementId(info) + postfix.getElementId(info);
	}

	@Override
	public boolean isStaticId()
	{
		return basedOn.isStaticId() && postfix.isStaticId();
	}
}
