package com.tle.web.viewurl;

import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;

public class AttachmentDetail
{
	private final Label name;
	private final SectionRenderable description;

	public AttachmentDetail(Label name, Label description)
	{
		this(name, new LabelRenderer(description));
	}

	public AttachmentDetail(Label name, SectionRenderable description)
	{
		this.name = name;
		this.description = description;
	}

	public SectionRenderable getDescription()
	{
		return description;
	}

	public Label getName()
	{
		return name;
	}
}
