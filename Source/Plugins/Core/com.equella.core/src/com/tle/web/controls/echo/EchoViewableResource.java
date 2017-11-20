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

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.common.Check;
import com.tle.core.echo.service.EchoService;
import com.tle.web.echo.EchoUtils;
import com.tle.web.echo.data.EchoAttachmentData;
import com.tle.web.echo.data.EchoData;
import com.tle.web.echo.data.EchoPresenter;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.render.JQueryTimeAgo;
import com.tle.web.sections.render.DelimitedRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.selection.SelectionService;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.resource.AbstractWrappedResource;

@SuppressWarnings("nls")
public class EchoViewableResource extends AbstractWrappedResource
{
	static
	{
		PluginResourceHandler.init(EchoViewableResource.class);
	}

	@PlugKey("echo.details.type")
	private static Label TYPE;
	@PlugKey("echo.details.mimetype")
	private static Label MIMETYPE;
	@PlugKey("echo.details.title")
	private static Label NAME;
	@PlugKey("echo.details.course")
	private static Label COURSE;
	@PlugKey("echo.details.section")
	private static Label SECTION;
	@PlugKey("echo.details.duration")
	private static Label DURATION;
	@PlugKey("echo.details.presenters")
	private static Label PRESENTERS;
	@PlugKey("echo.details.published")
	private static Label PUBLISHED;
	@PlugKey("echo.details.modified")
	private static Label CAPTURED;

	private final CustomAttachment echoAttachment;

	private final EchoService echoService;

	@Override
	public String getMimeType()
	{
		return EchoUtils.MIME_TYPE;
	}

	@Override
	public Bookmark createCanonicalUrl()
	{
		throw new UnsupportedOperationException("Must use Echo Viewer");
	}

	@Override
	public boolean isExternalResource()
	{
		return true;
	}

	@Override
	public boolean hasContentStream()
	{
		return false;
	}

	public EchoViewableResource(ViewableResource resource, CustomAttachment attachment, EchoService echoService,
		SelectionService selection, SectionInfo info)
	{
		super(resource);
		this.echoAttachment = attachment;
		this.echoService = echoService;

		if( selection.getCurrentSession(info) != null )
		{
			resource.setAttribute(ViewableResource.PREFERRED_LINK_TARGET, "_blank");
		}
	}

	@Override
	public List<AttachmentDetail> getCommonAttachmentDetails()
	{
		List<AttachmentDetail> commonDetails = new ArrayList<AttachmentDetail>();

		// Type
		commonDetails.add(makeDetail(TYPE, MIMETYPE));

		EchoAttachmentData ed = getEchoAttachmentData();

		if( ed != null )
		{
			EchoData echoData = ed.getEchoData();

			// Echo Name
			addDetail(commonDetails, echoData.getEchoTitle(), NAME);

			// Course Name
			addDetail(commonDetails, echoData.getCourseName(), COURSE);

			// Section Name
			addDetail(commonDetails, echoData.getSectionName(), SECTION);

			// Duration
			addDetail(commonDetails, EchoUtils.formatDuration(echoData.getEchoDuration()), DURATION);

			// Presenters
			List<SectionRenderable> presenterList = Lists.transform(ed.getPresenters(),
				new Function<EchoPresenter, SectionRenderable>()
				{
					@Override
					public SectionRenderable apply(EchoPresenter input)
					{
						TextLabel userLabel = new TextLabel(input.getFirstname() + " " + input.getLastname());
						String emailAddress = input.getEmail();
						if( Check.isEmpty(emailAddress) )
						{
							return new LabelRenderer(userLabel);
						}
						return new LinkRenderer(new HtmlLinkState(userLabel, new SimpleBookmark("mailto:"
							+ emailAddress)));
					}
				});
			commonDetails.add(makeDetail(PRESENTERS, new DelimitedRenderer(", ", presenterList.toArray())));

			// Published
			TagRenderer published = JQueryTimeAgo.timeAgoTag(echoData.getEchoPublishedDate());
			commonDetails.add(makeDetail(PUBLISHED, published));

			// Captured
			TagRenderer modified = JQueryTimeAgo.timeAgoTag(echoData.getEchoCapturedDate());
			commonDetails.add(makeDetail(CAPTURED, modified));
		}

		return commonDetails;
	}

	private EchoAttachmentData getEchoAttachmentData()
	{
		EchoAttachmentData ed = null;
		try
		{
			ed = echoService.getMapper().readValue((String) echoAttachment.getData(EchoUtils.PROPERTY_ECHO_DATA),
				EchoAttachmentData.class);

		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}

		return ed;
	}

	private void addDetail(List<AttachmentDetail> commonDetails, String detail, Label label)
	{
		if( !Check.isEmpty(detail) )
		{
			commonDetails.add(makeDetail(label, new TextLabel(detail)));
		}
	}

}
