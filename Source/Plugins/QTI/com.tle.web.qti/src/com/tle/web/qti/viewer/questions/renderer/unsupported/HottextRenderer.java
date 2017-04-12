package com.tle.web.qti.viewer.questions.renderer.unsupported;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.content.Hottext;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.QtiNodeRenderer;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author Aaron
 */
public class HottextRenderer extends QtiNodeRenderer
{
	@AssistedInject
	public HottextRenderer(@Assisted Hottext model, @Assisted QtiViewerContext context)
	{
		super(model, context);
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		throw new UnsupportedQuestionException("hottext", true);
	}
}
