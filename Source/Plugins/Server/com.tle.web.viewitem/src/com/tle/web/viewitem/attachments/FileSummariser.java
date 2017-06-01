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
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.ZipAttachment;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.common.FileSizeUtils;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.mimetypes.RegisterMimeTypeExtension;
import com.tle.core.services.FileSystemService;
import com.tle.freetext.SupportedVideoMimeTypeExtension;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.render.JQueryTimeAgo;
import com.tle.web.sections.render.ExtraAttributes;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.render.WrappedLabel;
import com.tle.web.sections.standard.renderers.SpanRenderer;
import com.tle.web.stream.ContentStream;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceExtension;
import com.tle.web.viewurl.resource.AbstractRealFileResource;

@NonNullByDefault
@Bind
@Singleton
public class FileSummariser
	implements
		AttachmentResourceExtension<Attachment>,
		RegisterMimeTypeExtension<Attachment>,
		SupportedVideoMimeTypeExtension
{
	static
	{
		PluginResourceHandler.init(FileSummariser.class);
	}

	@PlugKey("fileresource.details.type")
	private static Label TYPE;
	@PlugKey("fileresource.details.filename")
	private static Label FILENAME;
	@PlugKey("fileresource.details.filesize")
	private static Label SIZE;

	@PlugKey("file.msoffice.details.author")
	private static Label AUTHOR;
	@PlugKey("file.msoffice.details.publisher")
	private static Label PUBLISHER;
	@PlugKey("file.msoffice.details.lastmodified")
	private static Label LAST_MODIFIED;

	@PlugKey("file.msoffice.word.details.pages")
	private static Label PAGES;
	@PlugKey("file.msoffice.word.details.words")
	private static Label WORDS;

	@Inject
	private MimeTypeService mimeService;
	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	private FileSystemService fileSystemService;

	@Override
	public ViewableResource process(SectionInfo info, ViewableResource resource, Attachment attachment)
	{
		final String filename = attachment.getUrl();
		if( attachment instanceof ZipAttachment )
		{
			ZipAttachment zipAttachment = (ZipAttachment) attachment;
			resource.setAttribute(ViewableResource.KEY_HIDDEN, !zipAttachment.isAttachZip());
		}
		return new FileResource(resource, filename, mimeService.getMimeTypeForFilename(filename));
	}

	public class FileResource extends AbstractRealFileResource
	{
		public FileResource(ViewableResource inner, String filePath, String mimeType)
		{
			super(inner, filePath, mimeType, urlFactory, fileSystemService);
		}

		@Override
		@SuppressWarnings("nls")
		public List<AttachmentDetail> getCommonAttachmentDetails()
		{
			List<AttachmentDetail> commonDetails = new ArrayList<AttachmentDetail>();

			String mimetype = getMimeType();
			final MimeEntry mimeEntry = mimeService.getEntryForMimeType(mimetype);
			TextLabel mimeLabel = new TextLabel(mimeEntry == null || Check.isEmpty(mimeEntry.getDescription())
				? mimetype : mimeEntry.getDescription());
			// Have to render a span, td elements don't have title or alt attr.
			// otherwise it would just be done in the wrapped label constructor
			if( mimeLabel.getText().length() > 30 )
			{
				String longText = mimeLabel.getText();
				TagState spanState = new TagState();
				spanState.addTagProcessor(new ExtraAttributes("title", longText, "alt", longText));
				SpanRenderer span = new SpanRenderer(spanState, longText.substring(0, 30) + "...");
				commonDetails.add(makeDetail(TYPE, span));
			}
			else
			{
				commonDetails.add(makeDetail(TYPE, new WrappedLabel(mimeLabel, -1, true, false)));
			}
			// Name (Filename) and Size
			if( hasContentStream() )
			{
				final ContentStream stream = getContentStream();

				if( stream.exists() )
				{
					final long length = stream.getEstimatedContentLength();
					String readableFileSize = length >= 0 ? FileSizeUtils.humanReadableFileSize(length) : "0";
					// makeDetail throws a nullPointerException if 2nd Label is
					// null, so ensure it isn't
					commonDetails.add(makeDetail(FILENAME, new TextLabel(stream.getFilenameWithoutPath())));
					commonDetails.add(makeDetail(SIZE, new TextLabel(readableFileSize)));
				}
			}

			// TODO More file info... TIKA?
			// Use attachment for details...? Setup fake attachment
			IAttachment attachment = getAttachment();

			String author = (String) attachment.getData("author");
			if( !Check.isEmpty(author) )
			{
				commonDetails.add(makeDetail(AUTHOR, new TextLabel(author)));
			}

			String publisher = (String) attachment.getData("publisher");
			if( !Check.isEmpty(publisher) )
			{
				commonDetails.add(makeDetail(PUBLISHER, new TextLabel(publisher)));
			}

			Date lastmod = (Date) attachment.getData("lastmodified");
			if( lastmod != null )
			{
				TagRenderer lastedit = JQueryTimeAgo.timeAgoTag(lastmod);
				commonDetails.add(makeDetail(LAST_MODIFIED, lastedit));
			}

			String pcount = (String) attachment.getData("pagecount");
			if( !Check.isEmpty(pcount) )
			{
				commonDetails.add(makeDetail(PAGES, new TextLabel(pcount)));
			}

			String wcount = (String) attachment.getData("wordcount");
			if( !Check.isEmpty(wcount) )
			{
				commonDetails.add(makeDetail(WORDS, new TextLabel(wcount)));
			}

			return commonDetails;
		}
	}

	@Override
	public String getMimeType(Attachment attachment)
	{
		final String filename = attachment.getUrl();
		return mimeService.getMimeTypeForFilename(filename);
	}

	@Override
	public boolean isSupportedMimeType(@Nullable String mimeType)
	{
		if( mimeType != null && mimeType.startsWith("video") )
		{
			return true;
		}
		return false;
	}
}
