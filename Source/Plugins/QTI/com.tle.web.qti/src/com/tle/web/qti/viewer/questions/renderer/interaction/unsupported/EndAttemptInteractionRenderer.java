package com.tle.web.qti.viewer.questions.renderer.interaction.unsupported;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.EndAttemptInteraction;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.QtiNodeRenderer;
import com.tle.web.sections.render.SectionRenderable;

/**
 * Not supported until individual submission mode is supported.
 * 
 * @author Aaron
 */
public class EndAttemptInteractionRenderer extends QtiNodeRenderer
{
	@SuppressWarnings("unused")
	private final EndAttemptInteraction model;

	@AssistedInject
	public EndAttemptInteractionRenderer(@Assisted EndAttemptInteraction model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		return null;
	}

	@Override
	protected SectionRenderable createNestedRenderable()
	{
		return null;
	}
}