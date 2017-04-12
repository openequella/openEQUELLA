package com.tle.web.sections.standard.js;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.RenderContext;

@NonNullByDefault
public interface DelayedRenderer<T>
{
	@Nullable
	T getSelectedRenderer(RenderContext info);
}
