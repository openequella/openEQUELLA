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

package com.tle.web.viewitem.attachments;

import java.util.ArrayList;
import java.util.List;

import com.tle.beans.ReferencedURL;
import com.tle.common.Check;
import com.tle.core.url.URLCheckerService;
import com.tle.core.url.URLCheckerService.URLCheckMode;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.render.WrappedLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.resource.SimpleUrlResource;

public class DetailUrlResource extends SimpleUrlResource
{
	static
	{
		PluginResourceHandler.init(DetailUrlResource.class);
	}

	@PlugKey("linkresource.details.type")
	private static Label TYPE;
	@PlugKey("linkresource.details.mimetype")
	private static Label MIMETYPE;
	@PlugKey("linkresource.details.url")
	private static Label URL;
	@PlugKey("linkresource.details.status")
	private static Label STATUS;
	@PlugKey("linkresource.details.status.bad")
	private static Label STATUS_BAD;
	@PlugKey("linkresource.details.status.unknown")
	private static Label STATUS_UNKNOWN;

	private final URLCheckerService urlCheckerService;

	public DetailUrlResource(ViewableResource resource, String url, String description,
		URLCheckerService urlCheckerService)
	{
		super(resource, url, description, urlCheckerService.isUrlDisabled(url));
		this.urlCheckerService = urlCheckerService;
	}

	@Override
	public List<AttachmentDetail> getCommonAttachmentDetails()
	{
		List<AttachmentDetail> commonDetails = new ArrayList<AttachmentDetail>();

		// Type
		commonDetails.add(makeDetail(TYPE, MIMETYPE));

		// URL
		String url = getAttachment().getUrl();
		if( !Check.isEmpty(url))
		{
			HtmlLinkState link = new HtmlLinkState(new SimpleBookmark(url));
			link.setLabel(new WrappedLabel(new TextLabel(url), -1, true, false));
			link.setDisabled(isDisabled());
			commonDetails.add(makeDetail(URL, new LinkRenderer(link)));

			// Bad Status
			ReferencedURL urlStatus = urlCheckerService.getUrlStatus(url, URLCheckMode.RECORDS_FIRST);
			if( !urlStatus.isSuccess() )
			{
				commonDetails.add(makeDetail(STATUS, urlStatus.getTries() == 0 ? STATUS_UNKNOWN : STATUS_BAD));
			}
		}

		return commonDetails;
	}
}
