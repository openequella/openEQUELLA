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

package com.tle.web.search.actions;

import javax.inject.Inject;

import com.tle.core.fedsearch.FederatedSearchService;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.EquellaButtonExtension;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
public class RemoteRepoSearchAction extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	private static final String SESSION_KEY = "REMOTE-REPO-BUTTON";

	@EventFactory
	protected EventGenerator events;

	@Component(name = "b")
	@PlugKey("actions.remoterepo")
	private Button button;

	@Inject
	private FederatedSearchService federatedSearchService;

	@Inject
	private UserSessionService userSessionService;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		button.setClickHandler(events.getNamedHandler("remoteRepositories"));
		button.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
		button.setStyleClass("remote-repo");
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Boolean showButton = userSessionService.getAttribute(SESSION_KEY);
		if( showButton == null )
		{
			showButton = federatedSearchService.listEnabledSearchable().size() > 0;
			userSessionService.setAttribute(SESSION_KEY, showButton);
		}

		return !showButton ? null : SectionUtils.renderSectionResult(context, button);
	}

	@EventHandlerMethod
	public void remoteRepositories(SectionInfo info)
	{
		info.forwardToUrl(info.createForward("/access/remoterepo.do").getPublicBookmark().getHref());
	}
}
