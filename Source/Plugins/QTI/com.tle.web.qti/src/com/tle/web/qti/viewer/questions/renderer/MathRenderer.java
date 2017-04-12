package com.tle.web.qti.viewer.questions.renderer;

import java.util.Iterator;

import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.content.mathml.Math;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.base.FlowStaticRenderer;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.generic.statement.ScriptStatement;

/**
 * @author Aaron
 */
@NonNullByDefault
@SuppressWarnings("nls")
public class MathRenderer extends FlowStaticRenderer
{
	private final Math model;

	@AssistedInject
	public MathRenderer(@Assisted Math model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		super.preRender(info);
		if( !info.getBooleanAttribute(MathRenderer.class) )
		{
			info.addFooterStatements(new ScriptStatement("MathJax.Hub.Queue(['Typeset', MathJax.Hub]);"));
			info.setAttribute(MathRenderer.class, true);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T extends QtiNode> Iterator<T> getChildIterator()
	{
		return (Iterator<T>) model.getContent().iterator();
	}
}
