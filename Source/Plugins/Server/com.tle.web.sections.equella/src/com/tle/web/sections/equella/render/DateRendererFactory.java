package com.tle.web.sections.equella.render;

import java.util.Date;

import com.tle.core.guice.BindFactory;

@BindFactory
public interface DateRendererFactory
{
	DateRenderer createDateRenderer(Date date);

	DateRenderer createDateRenderer(Date date, boolean suppressSuffix);
}
