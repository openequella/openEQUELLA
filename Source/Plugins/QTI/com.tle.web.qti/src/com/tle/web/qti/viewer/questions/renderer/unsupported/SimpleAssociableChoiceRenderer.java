package com.tle.web.qti.viewer.questions.renderer.unsupported;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.QtiNodeRenderer;

/**
 * NOT SUPPORTED
 */
public class SimpleAssociableChoiceRenderer extends QtiNodeRenderer
{
	@AssistedInject
	protected SimpleAssociableChoiceRenderer(@Assisted SimpleAssociableChoice model, @Assisted QtiViewerContext context)
	{
		super(model, context);
	}
}
