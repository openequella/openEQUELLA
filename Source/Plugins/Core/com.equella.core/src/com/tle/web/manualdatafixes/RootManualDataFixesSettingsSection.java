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

package com.tle.web.manualdatafixes;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.ajax.FullDOMResult;
import com.tle.web.sections.ajax.JSONResponseCallback;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.libraries.JQueryProgression;
import com.tle.web.sections.jquery.libraries.JQueryTimer;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.registry.handler.CollectInterfaceHandler;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.HeadingRenderer;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@Bind
@SuppressWarnings("nls")
public class RootManualDataFixesSettingsSection extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	@PlugURL("js/taskupdate.js")
	private static String update_url;
	@PlugURL("css/manualdatafix.css")
	private static String css_url;
	@PlugKey("fix.title")
	private static Label TITLE_LABEL;

	@AjaxFactory
	private AjaxGenerator ajax;

	@Inject
	private ManualDataFixesPrivilegeTreeProvider securityProvider;

	private CollectInterfaceHandler<UpdateTaskStatus> children;

	@DirectEvent(priority = SectionEvent.PRIORITY_BEFORE_EVENTS)
	public void checkAuthorised(SectionInfo info)
	{
		securityProvider.checkAuthorised();
	}

	private final JSCallAndReference setupUpdate = new ExternallyDefinedFunction("setupUpdate", new IncludeFile(
		update_url, JQueryProgression.PRERENDER, JQueryTimer.PRERENDER, AjaxGenerator.AJAX_LIBRARY));

	private JSCallable updateCallback;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		children = new CollectInterfaceHandler<UpdateTaskStatus>(UpdateTaskStatus.class);
		updateCallback = ajax.getAjaxFunction("updateStatus");
		tree.addRegistrationHandler(children);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Decorations.getDecorations(context).setTitle(TITLE_LABEL);
		Breadcrumbs.get(context).add(SettingsUtils.getBreadcrumb());

		List<SectionRenderable> srs = renderChildren(context, new ResultListCollector()).getResultList();
		srs.add(0, HeadingRenderer.topLevel(new LabelRenderer(TITLE_LABEL)));

		if( checkForUnfinished(context) )
		{
			context.getBody()
				.addReadyStatements(new FunctionCallStatement(setupUpdate, updateCallback, getSectionId()));
		}

		return new CombinedRenderer(new DivRenderer("area manualdatafixes", new CombinedRenderer(srs)), CssInclude
			.include(css_url).hasRtl().make());
	}

	private boolean checkForUnfinished(SectionInfo info)
	{
		List<UpdateTaskStatus> allImplementors = children.getAllImplementors(info);
		boolean hasUnfinished = false;
		for( UpdateTaskStatus child : allImplementors )
		{
			if( !child.isFinished(info) )
			{
				hasUnfinished = true;
				break;
			}
		}
		return hasUnfinished;
	}

	@AjaxMethod
	public JSONResponseCallback updateStatus(final AjaxRenderContext info)
	{
		if( info.isRendered() )
		{
			return null;
		}

		// Get update divs from sections
		List<String> ids = Lists.newArrayList();

		for( UpdateTaskStatus fix : children.getAllImplementors(info) )
		{
			ids.add(fix.getAjaxId());
		}
		final boolean hasUnfinished = checkForUnfinished(info);
		info.addAjaxDivs(ids);

		return new JSONResponseCallback()
		{
			@Override
			public Object getResponseObject(AjaxRenderContext context)
			{
				FullDOMResult ajaxResult = context.getFullDOMResult();
				ManualDataFixStatusUpdate update = new ManualDataFixStatusUpdate(ajaxResult);
				update.setFinished(!hasUnfinished);
				update.setUpdates(ajaxResult.getHtml());
				return update;
			}
		};
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "rmdfs";
	}
}
