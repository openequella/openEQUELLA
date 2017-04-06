package com.tle.web.qti.viewer.questions.renderer;

import uk.ac.ed.ph.jqtiplus.node.content.variable.FeedbackBlock;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.base.FeedbackElementRenderer;

/**
 * @author Aaron
 */
public class FeedbackBlockRenderer extends FeedbackElementRenderer
{
	// private final FeedbackBlock model;

	@AssistedInject
	public FeedbackBlockRenderer(@Assisted FeedbackBlock model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		// this.model = model;
	}

	@Override
	protected String getTagName()
	{
		return "div";
	}
}
