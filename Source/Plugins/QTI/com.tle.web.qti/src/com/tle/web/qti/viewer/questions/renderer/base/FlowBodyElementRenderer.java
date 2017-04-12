package com.tle.web.qti.viewer.questions.renderer.base;

import uk.ac.ed.ph.jqtiplus.node.content.basic.AbstractFlowBodyElement;

import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.QtiNodeRenderer;

/**
 * @author Aaron
 */
public abstract class FlowBodyElementRenderer extends QtiNodeRenderer
{
	protected FlowBodyElementRenderer(AbstractFlowBodyElement model, QtiViewerContext context)
	{
		super(model, context);
	}
}
