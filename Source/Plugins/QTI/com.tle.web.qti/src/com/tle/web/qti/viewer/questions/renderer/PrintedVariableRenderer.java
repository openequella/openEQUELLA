package com.tle.web.qti.viewer.questions.renderer;

import uk.ac.ed.ph.jqtiplus.node.content.variable.PrintedVariable;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.Value;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;

public class PrintedVariableRenderer extends QtiNodeRenderer
{
	private final PrintedVariable model;

	@AssistedInject
	public PrintedVariableRenderer(@Assisted PrintedVariable model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		final Identifier varId = model.getIdentifier();
		final Value value = getContext().evaluateVariable(model, varId);
		if( value.isNull() )
		{
			return null;
		}
		// TODO: various different formats based on Value type
		return new LabelRenderer(new TextLabel(value.toQtiString()));
	}

	@Override
	protected boolean isNestedTop()
	{
		return false;
	}
}
