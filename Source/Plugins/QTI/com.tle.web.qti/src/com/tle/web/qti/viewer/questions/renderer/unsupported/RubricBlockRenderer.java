package com.tle.web.qti.viewer.questions.renderer.unsupported;

import uk.ac.ed.ph.jqtiplus.node.content.variable.RubricBlock;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.base.FlowBodyElementRenderer;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author Aaron
 */
public class RubricBlockRenderer extends FlowBodyElementRenderer
{
	@AssistedInject
	public RubricBlockRenderer(@Assisted RubricBlock model, @Assisted QtiViewerContext context)
	{
		super(model, context);
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		// throw new UnsupportedQuestionException("rubricBlock", true);
		return null;
	}
}
