package com.tle.web.qti.viewer.questions.renderer.interaction.unsupported;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.GraphicAssociateInteraction;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;

/**
 * @author Aaron
 */
public class GraphicAssociateInteractionRenderer extends GraphicInteractionRenderer
{
	@AssistedInject
	protected GraphicAssociateInteractionRenderer(@Assisted GraphicAssociateInteraction model,
		@Assisted QtiViewerContext context)
	{
		super(model, context);
	}
}
