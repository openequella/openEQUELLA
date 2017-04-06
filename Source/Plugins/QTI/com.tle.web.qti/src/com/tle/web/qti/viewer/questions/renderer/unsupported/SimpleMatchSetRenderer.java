package com.tle.web.qti.viewer.questions.renderer.unsupported;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleMatchSet;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.QtiNodeRenderer;
import com.tle.web.sections.render.SectionRenderable;

/**
 * NOT SUPPORTED
 */
public class SimpleMatchSetRenderer extends QtiNodeRenderer
{
	@AssistedInject
	protected SimpleMatchSetRenderer(@Assisted SimpleMatchSet model, @Assisted QtiViewerContext context)
	{
		super(model, context);
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		throw new UnsupportedQuestionException("simpleMatchSet", true);
	}
}
