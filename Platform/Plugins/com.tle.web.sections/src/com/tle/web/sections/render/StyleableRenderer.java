package com.tle.web.sections.render;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

@NonNullByDefault
public interface StyleableRenderer
{
	void setStyles(@Nullable String style, @Nullable String styleClass, @Nullable String id);

	StyleableRenderer addClass(@Nullable String extraClass);
}
