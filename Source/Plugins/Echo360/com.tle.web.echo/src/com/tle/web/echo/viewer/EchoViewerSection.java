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

package com.tle.web.echo.viewer;

import java.io.IOException;
import java.util.Collection;

import javax.inject.Inject;

import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.echo.service.EchoService;
import com.tle.core.guice.Bind;
import com.tle.web.echo.EchoUtils;
import com.tle.web.echo.data.EchoAttachmentData;
import com.tle.web.echo.data.EchoData;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.viewitem.viewer.AbstractViewerSection;
import com.tle.web.viewurl.ViewItemResource;

@Bind
@SuppressWarnings("nls")
public class EchoViewerSection extends AbstractViewerSection<EchoViewerSection.EchoModel>
{
	@PlugKey("viewer.error.noserver")
	private static String NO_SERVER_ERROR;

	@Inject
	private EchoService echoService;

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new EchoModel();
	}

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV;
	}

	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource) throws IOException
	{
		String viewerId = getModel(info).getViewerId();
		String url = "";

		IAttachment a = getAttachment(info, resource);
		EchoAttachmentData ead = getEchoAttachmentData(a);
		EchoData ed = ead.getEchoData();

		switch( viewerId )
		{
			case "echoCenterViewer":
				url = ed.getEchoCenterUrl();
				break;
			case "echoPlayerViewer":
				url = ed.getEchoLinkUrl();
				break;
			case "echoVodcastViewer":
				url = ed.getVodcastUrl();
				break;
			case "echoPodcastViewer":
				url = ed.getPodcastUrl();
				break;
			default:
				url = ed.getEchoCenterUrl();
				break;
		}

		if( Check.isEmpty(url) )
		{
			url = ed.getEchoCenterUrl();
		}

		// Forward to authenticated (echo seamless auth)
		String authenticatedUrl = echoService.getAuthenticatedUrl(ed.getEchoSystemID(), url);
		if( authenticatedUrl != null )
		{
			info.forwardToUrl(authenticatedUrl);
		}
		else
		{
			throw new RuntimeException(CurrentLocale.get(NO_SERVER_ERROR));
		}

		// No result to render
		return null;
	}

	protected static class EchoModel
	{
		String viewerId;

		public String getViewerId()
		{
			return viewerId;
		}

		public void setViewerId(String viewerId)
		{
			this.viewerId = viewerId;
		}
	}

	public void setViewerId(SectionInfo info, String viewerId)
	{
		getModel(info).setViewerId(viewerId);
	}

	private EchoAttachmentData getEchoAttachmentData(IAttachment a)
	{
		EchoAttachmentData ed = null;
		try
		{
			ed = echoService.getMapper().readValue((String) a.getData(EchoUtils.PROPERTY_ECHO_DATA),
				EchoAttachmentData.class);

		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}

		return ed;
	}
}
