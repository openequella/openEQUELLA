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

package com.tle.web.copyright.section;

import javax.inject.Inject;

import com.tle.beans.activation.ActivateRequest;
import com.tle.common.Check;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.viewitem.section.PathMapper.Type;
import com.tle.web.viewitem.section.RootItemFileSection;
import com.tle.web.viewitem.section.ViewAttachmentSection;
import com.tle.web.viewurl.ItemUrlExtender;
import com.tle.web.viewurl.ViewItemFilter;
import com.tle.web.viewurl.ViewItemResource;

@Bind
public class ViewByRequestSection extends AbstractPrototypeSection<ViewByRequestSection.Model>
	implements
		ViewItemFilter
{
	@Inject
	private ActivationService activationService;
	@TreeLookup
	private RootItemFileSection rootSection;
	@TreeLookup
	private ViewAttachmentSection attachmentSection;

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		rootSection.addFilterMapping(Type.ALWAYS, this);
	}

	public static class ViewRequestUrl implements ItemUrlExtender
	{
		private static final long serialVersionUID = 1L;

		private final String requestUuid;

		public ViewRequestUrl(String requestUuid)
		{
			this.requestUuid = requestUuid;
		}

		@Override
		public void execute(SectionInfo info)
		{
			ViewByRequestSection filter = info.lookupSection(ViewByRequestSection.class);
			filter.setRequestUuid(info, requestUuid);
		}
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public static class Model
	{
		@Bookmarked(parameter = "cf.act", supported = true)
		private String requestUuid;

		public String getRequestUuid()
		{
			return requestUuid;
		}

		public void setRequestUuid(String requestUuid)
		{
			this.requestUuid = requestUuid;
		}
	}

	public void setRequestUuid(SectionInfo info, String requestUuid)
	{
		getModel(info).setRequestUuid(requestUuid);
	}

	@Override
	public ViewItemResource filter(SectionInfo info, ViewItemResource resource)
	{
		Model model = getModel(info);
		String requestUuid = model.getRequestUuid();
		if( !Check.isEmpty(requestUuid) )
		{
			model.setRequestUuid(null);
			ActivateRequest request = activationService.getRequest(requestUuid);
			if( request != null )
			{
				attachmentSection.setAttachmentToView(info, request.getAttachment());
				info.forwardToUrl(info.getPublicBookmark().getHref());
				return null;
			}
		}
		return resource;
	}

	@Override
	public int getOrder()
	{
		return 0;
	}
}
