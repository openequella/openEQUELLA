package com.tle.web.template.section.event;

import java.util.Collections;
import java.util.List;

import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author Aaron
 */
public class BlueBarRenderable
{
	private final String key;
	private final Label label;
	private SectionRenderable renderable;
	private final int priority;

	public BlueBarRenderable(String unprefixedKey, Label label, SectionRenderable renderable, int priority)
	{
		this.key = unprefixedKey;
		this.label = label;
		this.renderable = renderable;
		this.priority = priority;
	}

	public Label getLabel()
	{
		return label;
	}

	public String getKey()
	{
		return key;
	}

	public void combineWith(SectionRenderable renderable)
	{
		this.renderable = CombinedRenderer.combineResults(this.renderable, renderable);
	}

	public SectionRenderable getRenderable()
	{
		return renderable;
	}

	public int getPriority()
	{
		return priority;
	}

	public static List<BlueBarRenderable> help(SectionRenderable renderable)
	{
		return Collections.singletonList(BlueBarConstants.Type.HELP.content(renderable));
	}
}
