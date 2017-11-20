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

package com.tle.web.controls.echo;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.echo.service.EchoService;
import com.tle.core.guice.Bind;
import com.tle.web.controls.universal.AbstractDetailsAttachmentHandler;
import com.tle.web.controls.universal.AttachmentHandlerLabel;
import com.tle.web.controls.universal.BasicAbstractAttachmentHandler;
import com.tle.web.controls.universal.DialogRenderOptions;
import com.tle.web.echo.EchoUtils;
import com.tle.web.echo.data.EchoAttachmentData;
import com.tle.web.echo.data.EchoData;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@Bind
@SuppressWarnings("nls")
public class EchoHandler extends BasicAbstractAttachmentHandler<EchoHandler.EchoHandlerModel>
{
	@PlugKey("echo.name")
	private static Label NAME_LABEL;
	@PlugKey("echo.description")
	private static Label DESCRIPTION_LABEL;

	@PlugKey("echo.edit.title")
	private static Label EDIT_TITLE_LABEL;

	@PlugKey("details.error.noserver")
	private static String NO_SERVER_ERROR;

	@Inject
	private EchoService echoService;
	@Inject
	private AttachmentResourceService attachmentResourceService;

	@Component
	@PlugKey("details.echocenter.link")
	private Link echoCenter;

	@Component
	@PlugKey("details.echoplayer.link")
	private Link echoPlayer;

	@Component
	@PlugKey("details.echovodcast.link")
	private Link echoVodcast;

	@Component
	@PlugKey("details.echopodcast.link")
	private Link echoPodcast;

	@Override
	protected List<Attachment> createAttachments(SectionInfo info)
	{
		return null;
	}

	@Override
	protected SectionRenderable renderAdd(RenderContext context, DialogRenderOptions renderOptions)
	{
		return null;
	}

	@Override
	public AttachmentHandlerLabel getLabel()
	{
		return new AttachmentHandlerLabel(NAME_LABEL, DESCRIPTION_LABEL);
	}

	@Override
	public String getHandlerId()
	{
		return "echoHandler";
	}

	@Override
	public boolean supports(IAttachment attachment)
	{
		if( attachment instanceof CustomAttachment )
		{
			CustomAttachment ca = (CustomAttachment) attachment;
			return EchoUtils.ATTACHMENT_TYPE.equals(ca.getType());
		}
		return false;
	}

	@Override
	public Label getTitleLabel(RenderContext context, boolean editing)
	{
		return EDIT_TITLE_LABEL;
	}

	@Override
	public Class<EchoHandlerModel> getModelClass()
	{
		return EchoHandlerModel.class;
	}

	public static class EchoHandlerModel extends AbstractDetailsAttachmentHandler.AbstractAttachmentHandlerModel
	{
		private final List<Link> links = Lists.newArrayList();

		public List<Link> getLinks()
		{
			return links;
		}

		public void addLink(Link link)
		{
			this.links.add(link);
		}
	}

	@Override
	protected SectionRenderable renderDetails(RenderContext context, DialogRenderOptions renderOptions)
	{
		ItemSectionInfo itemInfo = context.getAttributeForClass(ItemSectionInfo.class);
		Attachment attachment = getDetailsAttachment(context);
		ViewableResource resource = attachmentResourceService.getViewableResource(context, itemInfo.getViewableItem(),
			attachment);
		addAttachmentDetails(context, resource.getCommonAttachmentDetails());

		EchoAttachmentData ead = getEchoAttachmentData(attachment);
		EchoData ed = ead.getEchoData();

		makeLink(context, ed, echoCenter, ed.getEchoCenterUrl());
		makeLink(context, ed, echoPlayer, ed.getEchoLinkUrl());
		makeLink(context, ed, echoVodcast, ed.getVodcastUrl());
		makeLink(context, ed, echoPodcast, ed.getPodcastUrl());

		return viewFactory.createResult("edit-echo.ftl", this);
	}

	private void makeLink(SectionInfo info, EchoData ed, Link link, String url)
	{
		EchoHandlerModel model = getModel(info);

		if( !Check.isEmpty(url) )
		{
			HtmlLinkState state = link.getState(info);
			link.setBookmark(info,
				new BookmarkAndModify(info, events.getNamedModifier("openLink", url, ed.getEchoSystemID())));
			state.setTarget(HtmlLinkState.TARGET_BLANK);
			model.addLink(link);
		}
	}

	@EventHandlerMethod
	public void openLink(SectionInfo info, String url, String sysId)
	{
		String authenticatedUrl = echoService.getAuthenticatedUrl(sysId, url);
		if( authenticatedUrl != null )
		{
			info.forwardToUrl(authenticatedUrl);
		}
		throw new RuntimeException(CurrentLocale.get(NO_SERVER_ERROR));
	}

	private EchoAttachmentData getEchoAttachmentData(Attachment a)
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

	@Override
	public boolean show()
	{
		return false;
	}

	@Override
	public String getMimeType(SectionInfo info)
	{
		return EchoUtils.MIME_TYPE;
	}
}
