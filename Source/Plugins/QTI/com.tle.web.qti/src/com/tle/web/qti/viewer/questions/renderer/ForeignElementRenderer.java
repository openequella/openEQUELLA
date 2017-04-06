package com.tle.web.qti.viewer.questions.renderer;

import uk.ac.ed.ph.jqtiplus.node.ForeignElement;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author Aaron
 */
public class ForeignElementRenderer extends QtiNodeRenderer
{
	private final ForeignElement model;

	@AssistedInject
	public ForeignElementRenderer(@Assisted ForeignElement model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@Nullable
	@Override
	protected SectionRenderable createNestedRenderable()
	{
		SectionRenderable children = null;
		// Iterator doesn't work on ForeignElements, need to call getChildren
		for( QtiNode child : model.getChildren() )
		{
			children = CombinedRenderer.combineResults(children, qfac.chooseRenderer(child, getContext()));
		}
		return children;
	}
}
