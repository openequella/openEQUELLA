package com.tle.web.template.section.event;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.events.BroadcastEventListener;
import com.tle.web.sections.events.RenderContext;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface BlueBarEventListener extends BroadcastEventListener
{
	void addBlueBarResults(RenderContext context, BlueBarEvent event);
}
