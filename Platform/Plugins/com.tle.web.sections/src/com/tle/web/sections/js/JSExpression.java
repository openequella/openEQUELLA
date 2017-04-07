package com.tle.web.sections.js;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.PreRenderable;

@NonNullByDefault
public interface JSExpression extends PreRenderable
{
	/**
	 * Get javascript for this statement, it must remain stable for all calls
	 * with the same {@link SectionInfo}.
	 * 
	 * @param info The {@code SectionInfo}
	 * @return The javascript expression
	 */
	String getExpression(RenderContext info);
}
