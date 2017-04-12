package com.tle.web.qti.viewer.questions.renderer.base;

import uk.ac.ed.ph.jqtiplus.node.content.basic.FlowStatic;

import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.QtiNodeRenderer;

/**
 * @author Aaron
 */
public abstract class FlowStaticRenderer extends QtiNodeRenderer
{
	@SuppressWarnings("unused")
	private final FlowStatic flowStatic;

	protected FlowStaticRenderer(FlowStatic flowStatic, QtiViewerContext context)
	{
		super(flowStatic, context);
		this.flowStatic = flowStatic;
	}
}
