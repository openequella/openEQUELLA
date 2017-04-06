package com.tle.web.qti.viewer.questions.renderer.interaction.unsupported;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.HotspotInteraction;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;

/**
 * NOT SUPPORTED
 */
public class HotspotInteractionRenderer extends GraphicInteractionRenderer
{
	@AssistedInject
	public HotspotInteractionRenderer(@Assisted HotspotInteraction model, @Assisted QtiViewerContext context)
	{
		super(model, context);
	}
}
