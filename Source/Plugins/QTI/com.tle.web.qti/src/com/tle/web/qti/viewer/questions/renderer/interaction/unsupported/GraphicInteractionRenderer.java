package com.tle.web.qti.viewer.questions.renderer.interaction.unsupported;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.GraphicInteraction;

import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.base.BlockInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.unsupported.UnsupportedQuestionException;
import com.tle.web.sections.render.SectionRenderable;

/**
 * NOT SUPPORTED
 */
public abstract class GraphicInteractionRenderer extends BlockInteractionRenderer
{
	@SuppressWarnings("unused")
	private final GraphicInteraction model;

	protected GraphicInteractionRenderer(GraphicInteraction model, QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		throw new UnsupportedQuestionException("graphicInteraction");
	}
}
