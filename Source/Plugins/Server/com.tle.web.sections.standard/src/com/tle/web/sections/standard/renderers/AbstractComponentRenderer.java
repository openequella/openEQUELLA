package com.tle.web.sections.standard.renderers;

import java.io.IOException;
import java.util.Map;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.standard.model.HtmlComponentState;

public abstract class AbstractComponentRenderer extends TagRenderer
{
	protected HtmlComponentState state;

	public AbstractComponentRenderer(HtmlComponentState state)
	{
		super(null, state);
		this.state = state;
	}

	@SuppressWarnings("nls")
	@Override
	protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		if( isDisabled() )
		{
			attrs.put("disabled", "disabled");
		}
	}

	public boolean isDisabled()
	{
		return state.isDisabled();
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		state.setBeenRendered(true);
		super.realRender(writer);
	}

	protected boolean isStillAddClickHandler()
	{
		return true;
	}

	@Override
	protected void processHandler(SectionWriter writer, Map<String, String> attrs, String event, JSHandler handler)
	{
		if( !isStillAddClickHandler() && isDisabled() && JSHandler.EVENT_CLICK.equals(event) )
		{
			return;
		}
		super.processHandler(writer, attrs, event, handler);
	}

	@Override
	public SectionRenderable getNestedRenderable()
	{
		if( nestedRenderable != null )
		{
			return nestedRenderable;
		}
		else if( state.getLabel() != null )
		{
			nestedRenderable = state.createLabelRenderer();
		}
		return nestedRenderable;
	}

	public String getLabelText()
	{
		return state.getLabelText();
	}

	@Override
	protected abstract String getTag();

	public HtmlComponentState getHtmlState()
	{
		return state;
	}
}
