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

package com.tle.web.controls.universal.handlers;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.ReferencedURL;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.LinkAttachment;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.url.URLCheckerService;
import com.tle.core.url.URLCheckerService.URLCheckMode;
import com.tle.web.controls.universal.AbstractDetailsAttachmentHandler.AbstractAttachmentHandlerModel;
import com.tle.web.controls.universal.AttachmentHandlerLabel;
import com.tle.web.controls.universal.BasicAbstractAttachmentHandler;
import com.tle.web.controls.universal.DialogRenderOptions;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@Bind
@NonNullByDefault
@SuppressWarnings("nls")
public class UrlHandler extends BasicAbstractAttachmentHandler<AbstractAttachmentHandlerModel>
{
	@PlugKey("handlers.url.name")
	private static Label NAME;
	@PlugKey("handlers.url.description")
	private static Label DESCRIPTION;
	@PlugKey("handlers.url.add.title")
	private static Label TITLE_ADD;
	@PlugKey("handlers.url.edit.title")
	private static Label TITLE_EDIT;

	@PlugKey("handlers.url.details.lastchecked")
	private static Label LAST_CHECKED;
	@PlugKey("handlers.url.details.timeschecked")
	private static Label TIMES_CHECKED;
	@PlugKey("handlers.url.details.error")
	private static Label ERROR;
	@PlugKey("handlers.url.details.nooftimes")
	private static String NUMBER;

	@PlugKey("handlers.url.details.viewlink")
	private static Label VIEW_LINK_LABEL;

	@PlugKey("handlers.url.error.invalidurl")
	private static Label LABEL_ERROR_INVALIDURL;

	@Inject
	private URLCheckerService urlCheckerService;
	@Inject
	private DateRendererFactory dateRendererFactory;
	@Inject
	private AttachmentResourceService attachmentResourceService;

	@Component
	private TextField url;

	@Override
	public String getHandlerId()
	{
		return "urlHandler";
	}

	@Override
	public AttachmentHandlerLabel getLabel()
	{
		return new AttachmentHandlerLabel(NAME, DESCRIPTION);
	}

	@Override
	public boolean supports(IAttachment attachment)
	{
		return attachment instanceof LinkAttachment;
	}

	@Override
	protected SectionRenderable renderAdd(RenderContext context, DialogRenderOptions renderOptions)
	{
		return viewFactory.createResult("url/url-add.ftl", this);
	}

	@Override
	public boolean isMultipleAllowed(SectionInfo info)
	{
		return false;
	}

	@Override
	protected List<Attachment> createAttachments(SectionInfo info)
	{
		LinkAttachment link = new LinkAttachment();
		String urlString = url.getValue(info);
		link.setUrl(urlString);
		link.setDescription(urlString);
		return Collections.singletonList((Attachment) link);
	}

	@Override
	protected boolean validateAddPage(SectionInfo info)
	{
		return validateUrlField(info);
	}

	@Override
	protected boolean validateDetailsPage(SectionInfo info)
	{
		boolean valid = super.validateDetailsPage(info);
		valid &= validateUrlField(info);
		return valid;
	}

	private boolean validateUrlField(SectionInfo info)
	{
		try
		{
			URL test = new URL(url.getValue(info));
			test.toURI();
			return true;
		}
		catch( MalformedURLException | URISyntaxException e )
		{
			getModel(info).addError("url", LABEL_ERROR_INVALIDURL);
			return false;
		}
	}

	@Override
	protected void saveDetailsToAttachment(SectionInfo info, Attachment attachment)
	{
		super.saveDetailsToAttachment(info, attachment);
		attachment.setUrl(url.getValue(info));
	}

	@Override
	protected void setupDetailEditing(SectionInfo info)
	{
		super.setupDetailEditing(info);
		Attachment attachment = getDetailsAttachment(info);
		url.setValue(info, attachment.getUrl());
	}

	@Override
	public Label getTitleLabel(RenderContext context, boolean editing)
	{
		return editing ? TITLE_EDIT : TITLE_ADD;
	}

	@Override
	protected SectionRenderable renderDetails(RenderContext context, DialogRenderOptions renderOptions)
	{
		AbstractAttachmentHandlerModel model = getModel(context);

		// Common details
		final Attachment a = getDetailsAttachment(context);
		ItemSectionInfo itemInfo = context.getAttributeForClass(ItemSectionInfo.class);
		ViewableResource resource = attachmentResourceService.getViewableResource(context, itemInfo.getViewableItem(),
			a);
		addAttachmentDetails(context, resource.getCommonAttachmentDetails());

		// Additional details
		ReferencedURL urlStatus = urlCheckerService.getUrlStatus(a.getUrl(), URLCheckMode.RECORDS_FIRST);
		if( urlStatus.getTries() > 0 )
		{
			addAttachmentDetail(context, LAST_CHECKED,
				dateRendererFactory.createDateRenderer(urlStatus.getLastChecked()));
			if( !urlStatus.isSuccess() )
			{
				addAttachmentDetail(context, TIMES_CHECKED, new PluralKeyLabel(NUMBER, urlStatus.getTries()));
				addAttachmentDetail(context, ERROR, new TextLabel(urlStatus.getMessage()));
			}
		}

		if( Check.isEmpty(model.getEditTitle()) )
		{
			model.setEditTitle(url.getValue(context));
		}

		// View link
		HtmlLinkState linkState = new HtmlLinkState(VIEW_LINK_LABEL, new SimpleBookmark(a.getUrl()));
		linkState.setTarget(HtmlLinkState.TARGET_BLANK);
		model.setViewlink(new LinkRenderer(linkState));

		return viewFactory.createResult("url/url-edit.ftl", this);
	}

	public TextField getUrl()
	{
		return url;
	}

	@Override
	public Class<AbstractAttachmentHandlerModel> getModelClass()
	{
		return AbstractAttachmentHandlerModel.class;
	}

	@Override
	public String getMimeType(SectionInfo info)
	{
		return MimeTypeConstants.MIME_LINK;
	}
}
