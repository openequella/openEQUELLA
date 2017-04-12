package com.tle.web.itemlist;

import java.util.Collection;

import com.tle.web.sections.render.DelimitedRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.renderers.SpanRenderer;

public class DelimitedMetadata implements MetadataEntry
{
	private Label label;
	private SectionRenderable renderable;

	@SuppressWarnings("nls")
	public DelimitedMetadata(Label label, Collection<SectionRenderable> vals)
	{
		this.label = label;
		renderable = new SpanRenderer("itemresult-meta-delim", new DelimitedRenderer(
			new SpanRenderer("separator", "|"), vals));
	}

	@Override
	public Label getLabel()
	{
		return label;
	}

	@Override
	public SectionRenderable getValue()
	{
		return renderable;
	}
}
