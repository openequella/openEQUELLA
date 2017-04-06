package com.tle.web.qti.viewer.questions.renderer.interaction.unsupported;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.base.BlockInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.unsupported.UnsupportedQuestionException;
import com.tle.web.sections.render.SectionRenderable;

/**
 * NOT SUPPORTED
 */
public class MatchInteractionRenderer extends BlockInteractionRenderer
{
	@AssistedInject
	protected MatchInteractionRenderer(@Assisted MatchInteraction model, @Assisted QtiViewerContext context)
	{
		super(model, context);
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		throw new UnsupportedQuestionException("matchInteraction");
	}
}
