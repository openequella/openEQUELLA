/**
 * 
 */
package com.tle.web.itemlist;

import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;

public class StdMetadataEntry implements MetadataEntry
{
	private final Label label;
	private final SectionRenderable value;

	public StdMetadataEntry(Label label, SectionRenderable value)
	{
		this.label = label;
		this.value = value;
	}

	@Override
	public Label getLabel()
	{
		return label;
	}

	@Override
	public SectionRenderable getValue()
	{
		return value;
	}

}