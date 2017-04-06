package com.tle.web.qti.viewer.questions.renderer.unsupported;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.content.Gap;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.QtiNodeRenderer;
import com.tle.web.sections.render.SectionRenderable;

public class GapRenderer extends QtiNodeRenderer
{
	@AssistedInject
	protected GapRenderer(@Assisted Gap model, @Assisted QtiViewerContext context)
	{
		super(model, context);
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		throw new UnsupportedQuestionException("gap", true);
	}
}
