package com.tle.web.qti.viewer.questions.renderer.interaction.unsupported;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.GraphicOrderInteraction;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;

/**
 * @author Aaron
 */
public class GraphicOrderInteractionRenderer extends GraphicInteractionRenderer
{
	@AssistedInject
	public GraphicOrderInteractionRenderer(@Assisted GraphicOrderInteraction model, @Assisted QtiViewerContext context)
	{
		super(model, context);
	}
}
