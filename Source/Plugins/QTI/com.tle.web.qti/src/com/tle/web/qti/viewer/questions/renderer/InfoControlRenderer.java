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

package com.tle.web.qti.viewer.questions.renderer;

import javax.inject.Inject;

import uk.ac.ed.ph.jqtiplus.node.content.InfoControl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.DivRenderer;

@SuppressWarnings("nls")
public class InfoControlRenderer extends QtiNodeRenderer
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(InfoControlRenderer.class);
	private static final IncludeFile JS_INCLUDE = new IncludeFile(resources.url("scripts/qtiplayviewer.js"));
	private static final ExternallyDefinedFunction TOGGLE_HINT = new ExternallyDefinedFunction("toggleHint", JS_INCLUDE);

	/**
	 * Standard sections components factory
	 */
	@Inject
	private RendererFactory renderFactory;

	private final InfoControl model;
	private final HtmlComponentState state = new HtmlComponentState();

	@AssistedInject
	public InfoControlRenderer(@Assisted InfoControl model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
		state.setId("hint");
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		final HtmlLinkState linkState = new HtmlLinkState();
		linkState.addClass("hintlink");
		linkState.setClickHandler(Js.handler(TOGGLE_HINT, Jq.$(state)));
		linkState.setLabel(new TextLabel(model.getTitle()));
		return renderFactory.getRenderer(getContext().getRenderContext(), linkState);
	}

	@Override
	protected boolean isNestedTop()
	{
		return false;
	}

	@Override
	public SectionRenderable getNestedRenderable()
	{
		return new DivRenderer(state, "hint alert alert-info", super.getNestedRenderable());
	}
}
