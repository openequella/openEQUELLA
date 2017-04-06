package com.tle.web.itemlist;

import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;

public interface MetadataEntry
{
	Label getLabel();

	SectionRenderable getValue();
}
