package com.tle.web.sections.render;

import java.util.Map;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;

public class LabelTagProcessor implements TagProcessor
{
	private final String attribute;
	private final Label label;

	public LabelTagProcessor(String attribute, Label label)
	{
		this.attribute = attribute;
		this.label = label;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		// nothing
	}

	@Override
	public void processAttributes(SectionWriter writer, Map<String, String> attrs)
	{
		Label l = getLabel();
		attrs.put(getAttribute(), l.getText());
	}

	public String getAttribute()
	{
		return attribute;
	}

	public Label getLabel()
	{
		return label;
	}

}
