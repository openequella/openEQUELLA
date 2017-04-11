package com.tle.web.sections.js;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.PreRenderable;

@NonNullByDefault
public interface JSStatements extends PreRenderable
{
	/**
	 * Get javascript for this statement, mustn't change upon calling twice.
	 * 
	 * @param info
	 * @return The JavaScript for the statments
	 */
	String getStatements(RenderContext info);
}
