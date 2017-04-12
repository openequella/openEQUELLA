package com.tle.web.sections.standard.renderers;

import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.NestedRenderable;
import com.tle.web.sections.render.StyleableRenderer;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.model.HtmlLinkState;

public interface LinkTagRenderer extends JSDisableable, NestedRenderable, ElementId, StyleableRenderer
{
	void setTarget(String target);

	void setRel(String rel);

	void setTitle(Label title);

	void setLabel(Label label);

	void setDisabled(boolean disabled);

	void setElementId(ElementId elemId);

	void ensureClickable();

	HtmlLinkState getLinkState();
}
