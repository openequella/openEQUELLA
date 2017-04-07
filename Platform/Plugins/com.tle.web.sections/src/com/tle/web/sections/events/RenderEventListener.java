package com.tle.web.sections.events;

import java.util.EventListener;

import com.tle.annotation.NonNullByDefault;

@NonNullByDefault
public interface RenderEventListener extends EventListener
{
	void render(RenderEventContext context);
}
