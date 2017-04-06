package com.tle.web.sections.standard.renderers.popup;

import com.tle.common.Check;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.BookmarkModifier;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSBookmarkModifier;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.ClientSideBookmarkExpression;
import com.tle.web.sections.js.generic.expression.StringExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;

@SuppressWarnings("nls")
public class PopupHelper extends AbstractPopupHelper
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(PopupHelper.class);
	private static final JSCallAndReference POPUP_FUNCTION = new ExternallyDefinedFunction("popup", new IncludeFile(
		resources.url("js/popup.js")));
	private static final String DEFAULT_SIZE = "80%";

	@Override
	public void setHeight(String height)
	{
		this.height = height;
	}

	@Override
	public void setWidth(String width)
	{
		this.width = width;
	}

	public JSStatements getPopupCall(RenderContext info, JSExpression href)
	{
		return new FunctionCallStatement(POPUP_FUNCTION, href, target, width, height);
	}

	public JSHandler createClickHandler(SectionInfo info, String href, JSHandler handler)
	{
		OverrideHandler clickHandler;
		if( handler != null )
		{
			clickHandler = new OverrideHandler(handler.getValidators());
		}
		else
		{
			clickHandler = new OverrideHandler();
		}
		JSExpression hrefExpr;
		if( !(handler instanceof BookmarkModifier) )
		{
			hrefExpr = new StringExpression(href);
		}
		else
		{
			hrefExpr = new ClientSideBookmarkExpression((JSBookmarkModifier) handler);
		}

		if( Check.isEmpty(width) )
		{
			width = DEFAULT_SIZE;
		}
		if( Check.isEmpty(height) )
		{
			height = DEFAULT_SIZE;
		}

		clickHandler.addStatements(new FunctionCallStatement(POPUP_FUNCTION, hrefExpr, target, width, height));
		return clickHandler;
	}

}
