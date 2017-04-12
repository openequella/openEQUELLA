package com.tle.web.qti.viewer.questions.renderer;

import java.util.Iterator;
import java.util.Map;

import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.test.TestFeedback;
import uk.ac.ed.ph.jqtiplus.node.test.TestFeedbackAccess;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author Dongsheng
 */
public class TestFeedbackRenderer extends QtiNodeRenderer
{
	private final TestFeedback model;

	@AssistedInject
	public TestFeedbackRenderer(@Assisted TestFeedback model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		final QtiViewerContext context = getContext();
		final TestSessionState testSessionState = context.getSessionState();
		if( model.isVisible(testSessionState, TestFeedbackAccess.AT_END) )
		{
			return super.createTopRenderable();
		}
		return null;
	}

	@Override
	protected String getTagName()
	{
		return "div";
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T extends QtiNode> Iterator<T> getChildIterator()
	{
		return (Iterator<T>) model.getChildren().iterator();
	}

	@Override
	protected void addAttributes(Map<String, String> attrs)
	{
		super.addAttributes(attrs);
		attrs.put("class", "testfeedback");
	}
}
