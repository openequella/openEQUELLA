package com.tle.web.sections.standard.renderers.toggle;

import java.io.IOException;
import java.util.Map;

import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.ElementByIdExpression;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.renderers.AbstractElementRenderer;

@SuppressWarnings("nls")
public abstract class AbstractHiddenToggler extends AbstractElementRenderer
{
	private static final String JS_URL = ResourcesService.getResourceHelper(AbstractHiddenToggler.class).url(
		"js/toggler.js");

	protected HtmlBooleanState bstate;
	protected OverrideHandler clickHandler = new OverrideHandler();

	public AbstractHiddenToggler(HtmlBooleanState state)
	{
		super(state);
		this.bstate = state;
		clickHandler.addStatements(new FunctionCallStatement("toggleHidden", new ElementByIdExpression(this), bstate
			.getValue()));
	}

	@Override
	protected void writeStart(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		SectionInfo info = writer.getInfo();
		writer.writeTag("input", "id", getElementId(info), "type", "hidden", "name", getName(info), "value",
			bstate.isChecked() ? bstate.getValue() : null);
		super.writeStart(writer, attrs);
	}

	@Override
	protected void prepareLastAttributes(SectionWriter writer, Map<String, String> attrs)
	{
		super.prepareLastAttributes(writer, attrs);
		attrs.remove("id");
		attrs.remove("name");
		writer.bindHandler(JSHandler.EVENT_CLICK, attrs, clickHandler);
		attrs.put("href", "javascript:void();");//$NON-NLS-2$
	}

	@Override
	protected void processHandler(SectionWriter writer, Map<String, String> attrs, String event, JSHandler handler)
	{
		if( event.equals(JSHandler.EVENT_CHANGE) || event.equals(JSHandler.EVENT_CLICK) )
		{
			clickHandler.addStatements(handler);
		}
		else
		{
			super.processHandler(writer, attrs, event, handler);
		}
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		super.preRender(info);
		info.preRender(clickHandler);
		info.addJs(JS_URL);
	}

	@Override
	protected String getTag()
	{
		return "a";
	}
}
