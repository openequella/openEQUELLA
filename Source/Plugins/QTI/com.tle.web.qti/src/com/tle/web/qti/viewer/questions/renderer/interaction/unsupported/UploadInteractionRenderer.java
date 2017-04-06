package com.tle.web.qti.viewer.questions.renderer.interaction.unsupported;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.UploadInteraction;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.QtiNodeRenderer;
import com.tle.web.qti.viewer.questions.renderer.unsupported.UnsupportedQuestionException;
import com.tle.web.sections.render.SectionRenderable;

public class UploadInteractionRenderer extends QtiNodeRenderer
{
	@AssistedInject
	public UploadInteractionRenderer(@Assisted UploadInteraction model, @Assisted QtiViewerContext context)
	{
		super(model, context);
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		throw new UnsupportedQuestionException("uploadInteraction");
	}
}
