package com.tle.web.qti.viewer.questions.renderer;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.InlineChoice;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.web.qti.viewer.QtiViewerContext;

/**
 * UNUSED
 * 
 * @author Aaron
 */
@NonNullByDefault
public class InlineChoiceRenderer extends QtiNodeRenderer
{
	@AssistedInject
	public InlineChoiceRenderer(@Assisted InlineChoice model, @Assisted QtiViewerContext context)
	{
		super(model, context);
	}

	@SuppressWarnings("nls")
	@Override
	protected String getTagName()
	{
		return "option";
	}
}
