package com.tle.web.sections.generic;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.StandardRenderContext;

/**
 * Use in places where you don't have a render context (eg. 'registered'
 * methods)
 * 
 * @author Aaron
 */
@NonNullByDefault
public class DummyRenderContext extends StandardRenderContext
{
	public DummyRenderContext()
	{
		this(new DummySectionInfo());
	}

	public DummyRenderContext(SectionInfo info)
	{
		super(info);
	}
}
