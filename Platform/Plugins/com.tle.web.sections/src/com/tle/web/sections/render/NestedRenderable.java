package com.tle.web.sections.render;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

@NonNullByDefault
public interface NestedRenderable extends SectionRenderable
{
	/**
	 * @param nested
	 * @return Itself
	 */
	NestedRenderable setNestedRenderable(@Nullable SectionRenderable nested);

	@Nullable
	SectionRenderable getNestedRenderable();
}
