/*
 * Copyright 2017 Apereo
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

package com.tle.web.sections.equella.component;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SimpleSectionId;
import com.tle.web.sections.ajax.AbstractDOMResult;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.ajax.FullAjaxCaptureResult;
import com.tle.web.sections.ajax.FullDOMResult;
import com.tle.web.sections.ajax.JSONResponseCallback;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.ajax.handler.AnonymousAjaxCallback;
import com.tle.web.sections.equella.component.model.BoxState;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.standard.AbstractRenderedComponent;

@NonNullByDefault
public class Box extends AbstractRenderedComponent<BoxState>
{
	@AjaxFactory
	private AjaxGenerator ajax;
	private JSCallable toggleFunction;
	private SectionId modalId;
	private ParameterizedEvent minimiseEvent;

	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(Box.class);

	private static final PreRenderable INCLUDE = new IncludeFile(
		resources.url("scripts/component/box.js"), AjaxGenerator.AJAX_LIBRARY); //$NON-NLS-1$
	public static final ExternallyDefinedFunction AJAX_CALLBACK = new ExternallyDefinedFunction("boxCallback", INCLUDE); //$NON-NLS-1$

	private boolean noMinMaxOnHeader;

	public Box()
	{
		super("box"); //$NON-NLS-1$
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		modalId = new SimpleSectionId(tree.getParentId(id));
		final JSCallable toggleAjax = ajax.getAjaxFunction("toggleMinimise"); //$NON-NLS-1$
		toggleFunction = new JSCallable()
		{

			@Override
			public void preRender(PreRenderContext info)
			{
				info.preRender(toggleAjax);

			}

			@Override
			public int getNumberOfParams(@Nullable RenderContext context)
			{
				return 1;
			}

			@Override
			public String getExpressionForCall(RenderContext info, JSExpression... params)
			{
				return toggleAjax.getExpressionForCall(info, new AnonymousAjaxCallback(new FunctionCallStatement(
					AJAX_CALLBACK, AjaxGenerator.RESULTS_VAR, params[0], getParameterId())));
			}
		};
	}

	@Override
	protected void prepareModel(RenderContext info)
	{
		super.prepareModel(info);
		final BoxState state = getState(info);
		state.setToggleMinimise(toggleFunction);
		state.setNoMinMaxOnHeader(noMinMaxOnHeader);
	}

	@AjaxMethod
	public JSONResponseCallback toggleMinimise(AjaxRenderContext context)
	{
		context.queueEvent(minimiseEvent.createEvent(context, new String[]{}));
		context.addAjaxDivs(getParameterId());
		context.setFormBookmarkEvent(new BookmarkEvent(modalId, true, context));
		context.setModalId(modalId.getSectionId());
		return new JSONResponseCallback()
		{
			@Override
			public Object getResponseObject(AjaxRenderContext context)
			{
				return new BoxUpdate(context.getFullDOMResult());
			}
		};
	}

	public class BoxUpdate extends AbstractDOMResult
	{
		private final String boxHtml;
		private final String boxScript;

		public BoxUpdate(FullDOMResult fullResult)
		{
			super(fullResult);
			FullAjaxCaptureResult captured = fullResult.getHtml().get(getParameterId());
			boxHtml = captured.getHtml();
			boxScript = captured.getScript();
		}

		public String getBoxHtml()
		{
			return boxHtml;
		}

		public String getBoxScript()
		{
			return boxScript;
		}
	}

	@Override
	public Class<BoxState> getModelClass()
	{
		return BoxState.class;
	}

	public void setTitle(SectionInfo info, Label label)
	{
		getState(info).setLabel(label);
	}

	public void setMinimised(SectionInfo info, boolean minimised)
	{
		getState(info).setMinimised(minimised);
	}

	public boolean isNoMinMaxOnHeader()
	{
		return noMinMaxOnHeader;
	}

	public void setNoMinMaxOnHeader(boolean noMinMaxOnHeader)
	{
		this.noMinMaxOnHeader = noMinMaxOnHeader;
	}

	public void addCloseHandler(JSHandler closeHandler)
	{
		setEventHandler("close", closeHandler); //$NON-NLS-1$
	}

	public void addCloseHandler(SectionInfo info, JSHandler closeHandler)
	{
		setEventHandler(info, "close", closeHandler); //$NON-NLS-1$
	}

	public void addMinimiseHandler(ParameterizedEvent event)
	{
		setEventHandler("minimise", new OverrideHandler()); //$NON-NLS-1$
		this.minimiseEvent = event;
	}

	public void addEditHandler(JSHandler editHandler)
	{
		setEventHandler("edit", editHandler); //$NON-NLS-1$
	}

	public void addEditHandler(SectionInfo info, JSHandler editHandler)
	{
		setEventHandler(info, "edit", editHandler); //$NON-NLS-1$
	}
}
