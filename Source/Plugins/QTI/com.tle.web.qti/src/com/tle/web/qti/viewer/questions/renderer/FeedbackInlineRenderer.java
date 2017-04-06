package com.tle.web.qti.viewer.questions.renderer;

import uk.ac.ed.ph.jqtiplus.node.content.variable.FeedbackInline;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.base.FeedbackElementRenderer;

/**
 * @author Aaron
 */
public class FeedbackInlineRenderer extends FeedbackElementRenderer
{
	// private final FeedbackInline model;

	@AssistedInject
	public FeedbackInlineRenderer(@Assisted FeedbackInline model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		// this.model = model;
	}

	@Override
	protected String getTagName()
	{
		return "span";
	}
}
