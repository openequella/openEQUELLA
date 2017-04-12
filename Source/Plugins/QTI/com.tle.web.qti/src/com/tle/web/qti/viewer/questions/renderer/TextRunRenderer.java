package com.tle.web.qti.viewer.questions.renderer;

import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;

/**
 * @author Aaron
 */
public class TextRunRenderer extends QtiNodeRenderer
{
	private final TextRun model;

	@AssistedInject
	public TextRunRenderer(@Assisted TextRun model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		return new LabelRenderer(new TextLabel(model.getTextContent()));
	}

	@Override
	protected boolean isNestedTop()
	{
		return false;
	}
}
