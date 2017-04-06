package com.tle.web.sections.standard.renderers.popup;

import java.util.Map;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.ButtonRenderer;

public class PopupButtonRenderer extends ButtonRenderer
{
	private PopupHelper helper = new PopupHelper();
	private HtmlLinkState linkState;

	public void setHeight(String height)
	{
		helper.setHeight(height);
	}

	public void setTarget(String target)
	{
		helper.setTarget(target);
	}

	public void setWidth(String width)
	{
		helper.setWidth(width);
	}

	public PopupButtonRenderer(HtmlComponentState state)
	{
		super(state);
	}

	public PopupButtonRenderer(HtmlLinkState state)
	{
		super(state);
		this.linkState = state;
	}

	@Override
	protected void processHandler(SectionWriter writer, Map<String, String> attrs, String event, JSHandler handler)
	{
		if( !event.equals(JSHandler.EVENT_CLICK) )
		{
			super.processHandler(writer, attrs, event, handler);
		}
	}

	@Override
	protected void prepareLastAttributes(SectionWriter writer, Map<String, String> attrs)
	{
		if( !isDisabled() )
		{
			String href = null;
			if( linkState != null && linkState.getBookmark() != null )
			{
				href = linkState.getBookmark().getHref();
			}
			writer.bindHandler(JSHandler.EVENT_CLICK, attrs,
				helper.createClickHandler(writer, href, state.getHandler(JSHandler.EVENT_CLICK)));
		}
	}

}
