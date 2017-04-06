package com.tle.web.sections.standard.renderers;

import com.tle.web.sections.ajax.JSONResponseCallback;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlTreeState;

public interface TreeRenderer extends SectionRenderable
{
	JSONResponseCallback getJSONResponse();

	TreeRenderer createNewRenderer(HtmlTreeState state);
}
