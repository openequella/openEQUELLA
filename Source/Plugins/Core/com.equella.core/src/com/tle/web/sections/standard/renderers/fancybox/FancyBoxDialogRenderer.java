/*
 * Copyright 2019 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.sections.standard.renderers.fancybox;

import java.io.IOException;
import java.util.Map;

import com.tle.web.DebugSettings;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.libraries.JQueryFancyBox;
import com.tle.web.sections.js.JSBookmarkModifier;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSFunction;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.expression.ArrayExpression;
import com.tle.web.sections.js.generic.expression.CurrentForm;
import com.tle.web.sections.js.generic.expression.ElementIdExpression;
import com.tle.web.sections.js.generic.expression.StringExpression;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.function.DoNothing;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.ParentFrameFunction;
import com.tle.web.sections.js.generic.function.PrependedParameterFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.standard.dialog.model.DialogState;
import com.tle.web.sections.standard.dialog.renderer.DialogRenderer;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.AbstractComponentRenderer;

@SuppressWarnings("nls")
public class FancyBoxDialogRenderer extends AbstractComponentRenderer implements DialogRenderer
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(JQueryFancyBox.class);
	private static final PreRenderable INCLUDE = new IncludeFile(resources.url("js/fancybox.js"));

	public static final JSCallable RESIZE = new ExternallyDefinedFunction("$.fancybox.resize", 0,
		JQueryFancyBox.PRERENDER);
	public static final JSCallable FOCUS = new ExternallyDefinedFunction("focusOnLoad", 1, INCLUDE);

	private final DialogState diagState;
	private final FancyBoxOptions options;

	@Override
	protected String getTag()
	{
		return "div";
	}

	private FancyBoxDialogRenderer(DialogState state, FancyBoxOptions options)
	{
		super(state);
		this.options = options;
		this.diagState = state;
		if( diagState.isAjax() || diagState.isInline() )
		{
			options.setType("inline");
			options.setInlineElement(diagState.isAjax() ? new AppendedElementId(this, "wrap") : this);
		}
		else
		{
			options.setHref(diagState.getContentsUrl());
			options.setType("iframe");
		}
	}

	public FancyBoxDialogRenderer(DialogState state)
	{
		this(state, new FancyBoxOptions());
		options.setHeight(state.getHeight());
		options.setWidth(state.getWidth());
		options.setModal(state.isModal());
		JSFunction openCallback = !DebugSettings.isAutoTestMode() ? Js.function(Js.call_s(FOCUS,
			state.getDialogOpenedCallback())) : state.getDialogOpenedCallback();
		options.setDialogOpenedCallback(openCallback);
		options.setDialogClosedCallback(state.getDialogClosedCallback());
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		if( !diagState.isAjax() || diagState.getContents() != null )
		{
			super.realRender(writer);
		}
	}

	@Override
	protected void writeStart(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		if( diagState.isInline() )
		{
			writer.writeTag("div", "style", "display:none;");
		}
		super.writeStart(writer, attrs);
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		if( diagState.getContents() != null )
		{
			writer.render(diagState.getContents());
		}
		else
		{
			super.writeMiddle(writer);
		}
	}

	@Override
	protected void writeEnd(SectionWriter writer) throws IOException
	{
		super.writeEnd(writer);
		if( diagState.isInline() )
		{
			writer.endTag("div");
		}
	}

	@Override
	public JSCallable createCloseFunction()
	{
		if( !diagState.isAjax() && !diagState.isInline() && diagState.getContents() != null )
		{
			return new ParentFrameFunction(JQueryFancyBox.CLOSE);
		}
		else if( diagState.isAjax() )
		{
			return new JSCallable()
			{
				@Override
				public String getExpressionForCall(RenderContext info, JSExpression... params)
				{
					JSCallable closeCall = AjaxGenerator.CLOSE_AJAX_DIALOG;
					JSExpression[] newparams = new JSExpression[2];
					newparams[0] = new ElementIdExpression(options.getInlineElement());
					if( params.length == 1 )
					{
						newparams[1] = new AnonymousFunction(JQueryFancyBox.CLOSE, params[0]);
					}
					else
					{
						newparams[1] = JQueryFancyBox.CLOSE;
					}
					return closeCall.getExpressionForCall(info, newparams);
				}

				@Override
				public int getNumberOfParams(RenderContext context)
				{
					return 1;
				}

				@Override
				public void preRender(PreRenderContext info)
				{
					info.preRender(AjaxGenerator.OPEN_AJAX_DIALOG, JQueryFancyBox.CLOSE);
				}
			};
		}
		return JQueryFancyBox.CLOSE;
	}

	@Override
	public JSCallable createOpenFunction()
	{
		if( diagState.isAjax() )
		{
			if( diagState.getContents() != null )
			{
				return null;
			}
			return new JSCallable()
			{
				private JSExpression[] fancyOptions;

				@Override
				public String getExpressionForCall(RenderContext info, JSExpression... params)
				{
					JSCallable openCall = AjaxGenerator.OPEN_AJAX_DIALOG;
					JSExpression secondParam = CurrentForm.EXPR;
					Bookmark bookmark = diagState.getContentsUrl();
					if( bookmark != null )
					{
						secondParam = new StringExpression(bookmark.getHref());
						openCall = AjaxGenerator.OPEN_AJAX_DIALOGURL;
					}
					final JSBookmarkModifier modifier = diagState.getOpenModifier();
					JSExpression[] newparams = new JSExpression[7];
					newparams[0] = new ElementIdExpression(options.getInlineElement());
					newparams[1] = secondParam;
					newparams[2] = new StringExpression(modifier.getEventId());
					ArrayExpression evParams = new ArrayExpression();
					evParams.addAll(modifier.getParameters());
					evParams.addAll(params);
					newparams[3] = evParams;
					newparams[4] = new AnonymousFunction(JQueryFancyBox.FANCYBOX_STATIC, (Object[]) fancyOptions);
					newparams[5] = JQueryFancyBox.SHOW_ACTIVITY;
					newparams[6] = DoNothing.FUNCTION;
					return openCall.getExpressionForCall(info, newparams);
				}

				@Override
				public int getNumberOfParams(RenderContext context)
				{
					return -1;
				}

				@Override
				public void preRender(PreRenderContext info)
				{
					info.preRender(AjaxGenerator.OPEN_AJAX_DIALOG, JQueryFancyBox.FANCYBOX_STATIC,
						JQueryFancyBox.SHOW_ACTIVITY);
					fancyOptions = JSUtils.convertExpressions(options.getParameters());
					info.preRender(fancyOptions);
				}
			};
		}
		return new PrependedParameterFunction(JQueryFancyBox.FANCYBOX_STATIC, options.getParameters());
	}

	@Override
	public void setHeight(String height)
	{
		options.setHeight(height);
	}

	@Override
	public void setWidth(String width)
	{
		options.setWidth(width);
	}

	@Override
	public void setupOpener(HtmlLinkState opener)
	{
		JSHandler clickHandler = opener.getHandler(JSHandler.EVENT_CLICK);
		if( clickHandler != null )
		{
			clickHandler = new StatementHandler(clickHandler, new FunctionCallStatement(createOpenFunction()))
			{
				@Override
				public boolean isOverrideDefault()
				{
					return true;
				}
			};
		}
		else
		{
			clickHandler = new OverrideHandler(createOpenFunction());
		}
		opener.setClickHandler(clickHandler);
	}

	@Override
	public void setTitle(String title)
	{
		options.setTitle(title);
	}

	public void setPadding(int padding)
	{
		options.setPadding(padding);
	}

	public void setMargin(int margin)
	{
		options.setMargin(margin);
	}

	@Override
	public DialogRenderer createNewRenderer(DialogState state)
	{
		FancyBoxDialogRenderer renderer = new FancyBoxDialogRenderer(state, options);
		renderer.style = style;
		renderer.styleClasses = styleClasses;
		renderer.attrs = attrs;
		return renderer;
	}
}