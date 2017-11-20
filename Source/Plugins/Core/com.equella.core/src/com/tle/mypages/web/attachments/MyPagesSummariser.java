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

package com.tle.mypages.web.attachments;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.Constants;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.common.FileSizeUtils;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.mimetypes.RegisterMimeTypeExtension;
import com.tle.core.services.FileSystemService;
import com.tle.web.htmleditor.service.HtmlEditorService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.stream.ByteArrayContentStream;
import com.tle.web.stream.ContentStream;
import com.tle.web.stream.WrappedContentStream;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.servlet.ThumbServlet.GalleryParameter;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceExtension;
import com.tle.web.viewurl.resource.AbstractWrappedResource;

/**
 * Only used when looking at the item from advanced item management OR when
 * you've used the MyPages web control
 * 
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class MyPagesSummariser
	implements
		AttachmentResourceExtension<HtmlAttachment>,
		RegisterMimeTypeExtension<HtmlAttachment>
{
	static
	{
		PluginResourceHandler.init(MyPagesSummariser.class);
	}

	@PlugKey("details.type")
	private static Label TYPE;
	@PlugKey("mypages.details.mimetype")
	private static Label MIMETYPE;
	@PlugKey("details.name")
	private static Label NAME;
	@PlugKey("details.size")
	private static Label SIZE;
	@PlugURL("css/mypagesviewer.css")
	private static String CSS;

	@Inject
	private HtmlEditorService htmlEditorService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private ExtendedFreemarkerFactory viewFactory;
	@Inject
	private FileSystemService fileSystemService;

	@Override
	public ViewableResource process(SectionInfo info, ViewableResource resource, HtmlAttachment attachment)
	{
		return new MyPagesResource(info, resource, attachment);
	}

	/**
	 * This only exists for the IMS exporter. Don't use it.
	 * 
	 * @param title
	 * @param fileHandle
	 * @param filename
	 * @return
	 */
	public MyPagesResource createMyPagesResourceFromHtml(SectionInfo info, HtmlAttachment page, String html)
	{
		return new MyPagesResource(info, page, html, true);
	}

	public class MyPagesResource extends AbstractWrappedResource
	{
		private final SectionInfo info;
		private final FileHandle handle;
		private final HtmlAttachment attachment;
		private final ContentStream stream;
		private final boolean noBaseHref;
		private long rawLength;

		public MyPagesResource(SectionInfo info, ViewableResource resource, HtmlAttachment attachment)
		{
			super(resource);
			this.info = info;
			this.handle = resource.getViewableItem().getFileHandle();
			this.attachment = attachment;
			stream = new MyPagesContentStream(fileSystemService
				.getContentStream(resource.getViewableItem().getFileHandle(), attachment.getFilename(), "text/html"));
			noBaseHref = false;
			rawLength = -1;
		}

		protected MyPagesResource(SectionInfo info, HtmlAttachment page, String html, boolean noBaseHref)
		{
			super(new NullResource());
			this.info = info;
			this.handle = null;
			this.attachment = page;
			try
			{
				stream = new MyPagesContentStream(
					new ByteArrayContentStream(html.getBytes(Constants.UTF8), "", "text/html"));
			}
			catch( UnsupportedEncodingException use )
			{
				throw new RuntimeException(use);
			}
			this.noBaseHref = noBaseHref;
			rawLength = 1; // prevent handle from being referenced
		}

		@Override
		public String getMimeType()
		{
			return "text/html";
		}

		@Override
		public boolean hasContentStream()
		{
			return true;
		}

		@Override
		public ContentStream getContentStream()
		{
			return stream;
		}

		@Override
		public ViewAuditEntry getViewAuditEntry()
		{
			return new ViewAuditEntry("htmlpage", attachment.getDescription());
		}

		public class MyPagesContentStream extends WrappedContentStream
		{
			private byte[] contentBytes;

			public MyPagesContentStream(ContentStream inner)
			{
				super(inner);
			}

			@Override
			public File getDirectFile()
			{
				return null;
			}

			@Override
			public String getFilenameWithoutPath()
			{
				return "page.html";
			}

			@Override
			public long getContentLength()
			{
				if( contentBytes == null )
				{
					return -1;
				}
				return contentBytes.length;
			}

			@Override
			public long getEstimatedContentLength()
			{
				if( getContentLength() == -1 )
				{
					if( rawLength == -1 )
					{
						try
						{
							rawLength = fileSystemService.fileLength(handle, attachment.getFilename());
						}
						catch( FileNotFoundException e )
						{
							rawLength = 0;
						}
					}
					return rawLength;
				}
				return getContentLength();
			}

			private byte[] getContentBytes()
			{
				if( contentBytes == null )
				{
					final MyPagesResourceModel model = new MyPagesResourceModel();
					InputStreamReader reader;
					try
					{
						reader = new InputStreamReader(inner.getInputStream(), Constants.UTF8);
						StringWriter writer = new StringWriter();
						CharStreams.copy(reader, writer);
						Closeables.close(reader, false);
						model.setHtml(writer.toString());
						model.setTitle(attachment.getDescription());
						model.setUseBaseHref(!noBaseHref);

						final List<String> styles = Lists.newArrayList();
						if( !noBaseHref )
						{
							styles.add(CSS);
						}
						final String userCss = htmlEditorService.getStylesheetRelativeUrl();
						if( userCss != null )
						{
							// stylesheet is copied to root of package,
							// therefore need ..
							styles.add(noBaseHref ? PathUtils.filePath("..", PathUtils.getFilenameFromFilepath(userCss))
								: institutionService.institutionalise(userCss));
						}
						model.setStyles(styles);

						String finishedPage = SectionUtils.renderToString(info.getRootRenderContext(),
							viewFactory.createResultWithModel("viewpage.ftl", model));
						contentBytes = finishedPage.getBytes(Constants.UTF8);
					}
					catch( Exception e )
					{
						throw Throwables.propagate(e);
					}
				}
				return contentBytes;
			}

			@Override
			public InputStream getInputStream() throws IOException
			{
				return new ByteArrayInputStream(getContentBytes());
			}
		}

		@Override
		public List<AttachmentDetail> getCommonAttachmentDetails()
		{
			List<AttachmentDetail> commonDetails = new ArrayList<AttachmentDetail>();

			// Type
			commonDetails.add(makeDetail(TYPE, MIMETYPE));

			// Name
			commonDetails.add(makeDetail(NAME, new TextLabel(getDescription())));

			// Size
			if( hasContentStream() )
			{
				final ContentStream contentStream = getContentStream();
				if( contentStream.exists() )
				{
					final long length = contentStream.getEstimatedContentLength();
					String readableFileSize = length >= 0 ? FileSizeUtils.humanReadableFileSize(length) : "0";
					// makeDetail throws a nullPointerException if 2nd Label is
					// null, so ensure it isn't
					commonDetails.add(makeDetail(SIZE, new TextLabel(readableFileSize)));
				}
			}

			return commonDetails;
		}
	}

	public static class MyPagesResourceModel
	{
		private String html;
		private String title;
		private List<String> styles;
		private boolean useBaseHref;

		public String getHtml()
		{
			return html;
		}

		public String getTitle()
		{
			return title;
		}

		public void setTitle(String title)
		{
			this.title = title;
		}

		public void setHtml(String html)
		{
			this.html = html;
		}

		public boolean isUseBaseHref()
		{
			return useBaseHref;
		}

		public void setUseBaseHref(boolean useBaseHref)
		{
			this.useBaseHref = useBaseHref;
		}

		public List<String> getStyles()
		{
			return styles;
		}

		public void setStyles(List<String> styles)
		{
			this.styles = styles;
		}
	}

	public static class NullResource implements ViewableResource
	{
		private final Map<Object, Object> attributes = new HashMap<Object, Object>();

		protected NullResource()
		{
		}

		@Override
		public SectionInfo getInfo()
		{
			return null;
		}

		@Override
		public ViewableItem getViewableItem()
		{
			return null;
		}

		@Override
		public boolean isExternalResource()
		{
			return false;
		}

		@Override
		public Bookmark createCanonicalUrl()
		{
			return null;
		}

		@Override
		public ViewItemUrl createDefaultViewerUrl()
		{
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAttribute(Object key)
		{
			return (T) attributes.get(key);
		}

		@Override
		public boolean getBooleanAttribute(Object key)
		{
			Boolean attr = getAttribute(key);
			return attr != null ? attr : false;
		}

		@Override
		public void setAttribute(Object key, Object value)
		{
			attributes.put(key, value);
		}

		@Override
		public Attachment getAttachment()
		{
			return null;
		}

		@Override
		public String getDescription()
		{
			return null;
		}

		@Override
		public ViewAuditEntry getViewAuditEntry()
		{
			return null;
		}

		@Override
		public boolean hasContentStream()
		{
			return false;
		}

		@Override
		public ContentStream getContentStream()
		{
			return null;
		}

		@Override
		public String getMimeType()
		{
			return null;
		}

		@Override
		public String getFilepath()
		{
			return null;
		}

		@Override
		public void wrappedBy(ViewableResource resource)
		{
		}

		@Override
		public boolean isCustomThumb()
		{
			return false;
		}

		@Override
		public ThumbRef getThumbnailReference(SectionInfo info, GalleryParameter gallery)
		{
			return null;
		}

		@Override
		public ImageRenderer createStandardThumbnailRenderer(Label label)
		{
			return null;
		}

		@Override
		public String getDefaultViewer()
		{
			return null;
		}

		@Override
		public boolean isDisabled()
		{
			return false;
		}

		@Override
		public List<AttachmentDetail> getCommonAttachmentDetails()
		{
			return null;
		}

		@Override
		public List<AttachmentDetail> getExtraAttachmentDetails()
		{
			return null;
		}

		@Override
		public ImageRenderer createGalleryThumbnailRenderer(Label label)
		{
			return null;
		}

		@Override
		public ImageRenderer createVideoThumbnailRenderer(Label label, TagState tag)
		{
			return null;
		}

		@Override
		public String getGalleryUrl(boolean preview, boolean original)
		{
			return null;
		}
	}

	@Override
	public String getMimeType(HtmlAttachment attachment)
	{
		return "text/html";
	}
}
