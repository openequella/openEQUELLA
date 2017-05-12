package com.tle.web.sections.render;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.PreRenderContext;

@NonNullByDefault
public interface PreRenderable extends SectionResult
{
	/**
	 * Include or generate any header resources needed for this result.
	 * 
	 * @param info The {@code SectionInfo}
	 */
	void preRender(PreRenderContext info);
}
