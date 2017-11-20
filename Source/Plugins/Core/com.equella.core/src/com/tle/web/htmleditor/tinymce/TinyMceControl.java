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

package com.tle.web.htmleditor.tinymce;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.tle.common.scripting.ScriptContextFactory;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.htmleditor.HtmlEditorControl;
import com.tle.web.htmleditor.tinymce.service.TinyMceService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author aholland
 */
@Bind
@SuppressWarnings("nls")
public class TinyMceControl extends AbstractPrototypeSection<TinyMceModel> implements HtmlEditorControl, PreRenderable
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@PlugKey("editor.link.fullscreen")
	@Component
	protected Link fullscreen;
	@Component(stateful = false)
	protected TextField html;

	private String defaultPropertyName = "mct";

	@Inject
	private TinyMceService tinyMceService;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		return viewFactory.createResult("editor/htmleditorctl.ftl", context, this);
	}

	@Override
	public void preRender(PreRenderContext context)
	{
		final TinyMceModel model = getModel(context);
		tinyMceService.preRender(context, html, model);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		html.setEventHandler(JSHandler.EVENT_PRESUBMIT, tinyMceService.getPreSubmitHandler(html));
		fullscreen.setClickHandler(tinyMceService.getToggleFullscreeenHandler(html, fullscreen));
	}

	@Override
	public void setData(SectionInfo info, Map<String, String> properties, boolean restrictedCollections,
		boolean restrictedDynacolls, boolean restrictedSearches, boolean restrictedContributables,
		Map<Class<?>, Set<String>> searchableUuids, Set<String> contributableUuids, String formId,
		ScriptContextFactory scriptContextFactory)
		throws Exception
	{
		properties.put("fid", formId);

		TinyMceModel model = getModel(info);
		model.setScriptContextFactory(scriptContextFactory);
		tinyMceService.populateModel(info, model, properties, restrictedCollections, restrictedDynacolls,
			restrictedSearches, restrictedContributables, searchableUuids, contributableUuids);

		html.setValue(info, properties.get("html"));
	}

	@Override
	public String getHtml(SectionInfo info)
	{
		return html.getValue(info);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return defaultPropertyName;
	}

	@Override
	public void setDefaultPropertyName(String defaultPropertyName)
	{
		this.defaultPropertyName = defaultPropertyName;
	}

	@Override
	public Class<TinyMceModel> getModelClass()
	{
		return TinyMceModel.class;
	}

	@Override
	public JSCallable createDisableFunction()
	{
		return tinyMceService.getDisableFunction(html, fullscreen);
	}

	public TextField getHtml()
	{
		return html;
	}

	public Link getFullscreenLink()
	{
		return fullscreen;
	}
}
