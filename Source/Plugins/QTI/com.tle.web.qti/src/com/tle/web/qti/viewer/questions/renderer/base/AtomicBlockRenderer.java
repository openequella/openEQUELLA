package com.tle.web.qti.viewer.questions.renderer.base;

import uk.ac.ed.ph.jqtiplus.node.content.basic.AtomicBlock;

import com.tle.web.qti.viewer.QtiViewerContext;

/**
 * @author Aaron
 */
public abstract class AtomicBlockRenderer extends FlowStaticRenderer
{
	@SuppressWarnings("unused")
	private final AtomicBlock model;

	protected AtomicBlockRenderer(AtomicBlock model, QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}
}
