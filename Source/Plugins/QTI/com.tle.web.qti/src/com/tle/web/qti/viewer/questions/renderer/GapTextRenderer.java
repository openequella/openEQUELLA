package com.tle.web.qti.viewer.questions.renderer;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.GapText;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;

public class GapTextRenderer extends QtiNodeRenderer
{
	@SuppressWarnings("unused")
	private final GapText model;

	@AssistedInject
	protected GapTextRenderer(@Assisted GapText model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}
}
