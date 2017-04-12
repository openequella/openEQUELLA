package com.tle.web.sections.standard.model;

import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;

public interface HtmlTreeNode
{
	String getId();

	SectionRenderable getRenderer();

	Label getLabel();

	boolean isLeaf();
}
