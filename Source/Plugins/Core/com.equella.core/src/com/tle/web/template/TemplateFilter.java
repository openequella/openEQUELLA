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

package com.tle.web.template;

import java.net.URI;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.UrlService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionFilter;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.EventAuthoriser;
import com.tle.web.sections.registry.TreeRegistry;

@Bind
@Singleton
public class TemplateFilter implements SectionFilter
{
	@Inject
	private InstitutionService institutionService;
	@Inject
	private UrlService urlService;
	@Inject
	private TreeRegistry treeRegistry;
	private static final String TEMPLATE_TREE = "$TEMPLATE$"; //$NON-NLS-1$

	@Override
	public void filter(MutableSectionInfo info)
	{
		HttpServletRequest request = info.getRequest();
		if( request != null )
		{
			info.setAttribute(SectionInfo.KEY_BASE_HREF, urlService.getBaseUriFromRequest(request));
		}
		else
		{
			info.setAttribute(SectionInfo.KEY_BASE_HREF, URI.create(institutionService.getInstitutionUrl().toString()));
		}
		SectionTree tree = treeRegistry.getTreeForPath(TEMPLATE_TREE);
		info.setAttribute(EventAuthoriser.class, new EventAuthoriser()
		{

			@Override
			public void checkAuthorisation(SectionInfo info)
			{
				if( !CurrentUser.getSessionID().equals(info.getRequest().getParameter(RenderTemplate.XSRF_PARAM)) )
				{
					throw new AccessDeniedException(
						"XSRF Prevention: session parameter must be included for this request"); //$NON-NLS-1$
				}
			}

			@Override
			public void addToBookmark(SectionInfo info, Map<String, String[]> bookmarkState)
			{
				bookmarkState.put(RenderTemplate.XSRF_PARAM, new String[]{CurrentUser.getSessionID()});
			}
		});
		info.addTree(tree);
		info.queueTreeEvents(tree);
	}
}
