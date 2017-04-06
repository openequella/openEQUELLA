package com.tle.web.qti.viewer.questions.renderer.base;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.BlockInteraction;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.QtiNodeRenderer;

/**
 * @author Aaron
 */
@NonNullByDefault
public abstract class BlockInteractionRenderer extends QtiNodeRenderer
{
	@SuppressWarnings("unused")
	private final BlockInteraction model;

	protected BlockInteractionRenderer(BlockInteraction model, QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}
}
