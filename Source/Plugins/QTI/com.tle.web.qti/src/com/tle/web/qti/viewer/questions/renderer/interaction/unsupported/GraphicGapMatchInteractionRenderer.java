package com.tle.web.qti.viewer.questions.renderer.interaction.unsupported;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.GraphicGapMatchInteraction;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.unsupported.UnsupportedQuestionException;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author Aaron
 */
public class GraphicGapMatchInteractionRenderer extends GraphicInteractionRenderer
{
	@AssistedInject
	protected GraphicGapMatchInteractionRenderer(@Assisted GraphicGapMatchInteraction model,
		@Assisted QtiViewerContext context)
	{
		super(model, context);
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		throw new UnsupportedQuestionException("graphicGapMatchInteraction");
	}
}
