package com.tle.web.sections.render;

import java.io.IOException;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionWriter;

@NonNullByDefault
public interface SectionRenderable extends PreRenderable
{
	/**
	 * Render this result to the given writer. <b>This method should have no
	 * side effects.</b> You should be able to render a result as many times as
	 * you want.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	void realRender(SectionWriter writer) throws IOException;
}
