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

package com.tle.web.mimetypes.section;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;

import javax.inject.Inject;

import com.dytech.devlib.Base64;
import com.google.common.io.ByteStreams;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.NameValue;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.guice.Bind;
import com.tle.core.imagemagick.ImageMagickException;
import com.tle.core.imagemagick.ImageMagickService;
import com.tle.core.imagemagick.ThumbnailOptions;
import com.tle.core.services.FileSystemService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.mimetypes.MimeEditExtension;
import com.tle.web.mimetypes.service.WebMimeTypeService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.ReloadHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.FileUpload;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.stream.ContentStreamWriter;

@SuppressWarnings("nls")
@Bind
public class EditIconSection extends AbstractPrototypeSection<EditIconSection.EditIconModel>
	implements
		HtmlRenderer,
		MimeEditExtension
{
	private static final String ICON_FILENAME = "icon.gif";
	private static final String PRE_THUMB_FILENAME = "thumb";

	@Inject
	private ImageMagickService imageMagickService;
	@Inject
	private StagingService stagingService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private WebMimeTypeService webMimeTypeService;
	@Inject
	private ContentStreamWriter contentStreamWriter;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Component(name = "iu")
	private FileUpload iconUpload;
	@Component(name = "ub")
	private Button uploadButton;
	@Component(name = "ri")
	private Button removeIconButton;

	@Override
	public Class<EditIconModel> getModelClass()
	{
		return EditIconModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "mei";
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		uploadButton.setClickHandler(new ReloadHandler());
		removeIconButton.setClickHandler(events.getNamedHandler("removeIcon"));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		BookmarkAndModify stagingIcon = new BookmarkAndModify(context, events.getNamedModifier("showCurrentIcon"));
		getModel(context).setDisplayIconUrl(stagingIcon.getHref());

		return viewFactory.createResult("iconedit.ftl", context);
	}

	@EventHandlerMethod
	public void showCurrentIcon(SectionInfo info)
	{
		final EditIconModel model = getModel(info);
		if( model.isHasCustomIcon() && model.getStagingId() != null )
		{
			info.setRendered();
			final StagingFile staging = new StagingFile(model.getStagingId());
			contentStreamWriter.outputStream(info.getRequest(), info.getResponse(),
				fileSystemService.getContentStream(staging, ICON_FILENAME, "image/gif"));
		}
		else
		{
			info.forwardToUrl(model.getIconUrl());
		}
	}

	@DirectEvent
	public void checkFileUpload(SectionInfo info)
	{
		if( iconUpload.getFileSize(info) > 0 )
		{
			final EditIconModel model = getModel(info);
			model.setErrorKey(null);

			final String stagingId = model.getStagingId();
			StagingFile stagingFile;
			if( stagingId == null )
			{
				stagingFile = stagingService.createStagingArea();
				model.setStagingId(stagingFile.getUuid());
			}
			else
			{
				stagingFile = new StagingFile(stagingId);
			}

			try( InputStream iconStream = iconUpload.getInputStream(info) )
			{
				fileSystemService.write(stagingFile, PRE_THUMB_FILENAME, iconStream, false);
				generateThumbnail(stagingFile);

				model.setHasCustomIcon(true);
				// Stop the image being cached - setting the cache control http
				// header doesn't seem to work
				model.setRand(new Random().nextInt(10000));
			}
			catch( ImageMagickException ime )
			{
				setIconError(model);
			}
			catch( IOException e )
			{
				setIconError(model);
			}
			catch( RuntimeException e )
			{
				setIconError(model);
			}
		}
	}

	private void setIconError(final EditIconModel model)
	{
		model.setErrorKey("upload");
		model.setStagingId(null);
	}

	@EventHandlerMethod
	public void removeIcon(SectionInfo info)
	{
		final EditIconModel model = getModel(info);
		model.setErrorKey(null);
		model.setHasCustomIcon(false);

		final MimeEntry entry = webMimeTypeService.getEntryForMimeType(model.getMimeType());
		final URL defaultIcon = webMimeTypeService.getDefaultIconForEntry(entry);
		if( defaultIcon != null )
		{
			model.setIconUrl(defaultIcon.toString());
		}
	}

	@Override
	public void loadEntry(SectionInfo info, MimeEntry entry)
	{
		final EditIconModel model = getModel(info);
		if( entry != null )
		{
			final URL icon = webMimeTypeService.getIconForEntry(entry, false);
			if( icon != null )
			{
				model.setIconUrl(icon.toString());
				model.setHasCustomIcon(webMimeTypeService.hasCustomIcon(entry));
			}
			model.setMimeType(entry.getType());
		}
		else
		{
			model.setIconUrl(webMimeTypeService.getDefaultIconForEntry(null).toString());
			model.setHasCustomIcon(false);
		}
	}

	@Override
	public void saveEntry(SectionInfo info, MimeEntry entry)
	{
		final EditIconModel model = getModel(info);
		final String stagingId = model.getStagingId();

		if( model.isHasCustomIcon() && stagingId != null )
		{
			final StagingFile stagingFile = new StagingFile(stagingId);
			try( InputStream iconStream = fileSystemService.read(stagingFile, ICON_FILENAME) )
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ByteStreams.copy(iconStream, baos);
				String base64Icon = new Base64().encode(baos.toByteArray());
				webMimeTypeService.setIconBase64(entry, base64Icon);
			}
			catch( IOException e )
			{
				throw new RuntimeException(e);
			}
			fileSystemService.removeFile(stagingFile);
		}
		else if( !model.isHasCustomIcon() )
		{
			webMimeTypeService.setIconBase64(entry, null);
		}
	}

	/**
	 * @param staging
	 * @throws ImageMagickException
	 */
	private void generateThumbnail(FileHandle staging)
	{
		File originalImage = fileSystemService.getExternalFile(staging, PRE_THUMB_FILENAME);
		File destImage = fileSystemService.getExternalFile(staging, ICON_FILENAME);
		ThumbnailOptions topts = new ThumbnailOptions();
		int height = 66;
		int width = 88;
		topts.setHeight(height);
		topts.setWidth(width);
		topts.setCropHeight(height);
		topts.setCropWidth(width);
		topts.setGravity("center");
		try
		{
			imageMagickService.generateThumbnailAdvanced(originalImage, destImage, topts);
		}
		finally
		{
			fileSystemService.removeFile(staging, PRE_THUMB_FILENAME);
		}
	}

	@Override
	public NameValue getTabToAppearOn()
	{
		return MimeTypesEditSection.TAB_DETAILS;
	}

	@Override
	public boolean isVisible(SectionInfo info)
	{
		return true;
	}

	public FileUpload getIconUpload()
	{
		return iconUpload;
	}

	public Button getUploadButton()
	{
		return uploadButton;
	}

	public Button getRemoveIconButton()
	{
		return removeIconButton;
	}

	public static class EditIconModel
	{
		@Bookmarked(name = "s")
		private String stagingId;
		@Bookmarked(name = "curl")
		private String iconUrl;
		@Bookmarked(name = "hc")
		private boolean hasCustomIcon;
		// for reference later
		@Bookmarked(name = "mt")
		private String mimeType;
		// Needed to stop damn image caching
		@Bookmarked(name = "r")
		private int rand;
		@Bookmarked(name = "e")
		private String errorKey;

		// render time
		private String displayIconUrl;

		public String getStagingId()
		{
			return stagingId;
		}

		public void setStagingId(String stagingId)
		{
			this.stagingId = stagingId;
		}

		public String getIconUrl()
		{
			return iconUrl;
		}

		public void setIconUrl(String iconUrl)
		{
			this.iconUrl = iconUrl;
		}

		public boolean isHasCustomIcon()
		{
			return hasCustomIcon;
		}

		public void setHasCustomIcon(boolean hasCustomIcon)
		{
			this.hasCustomIcon = hasCustomIcon;
		}

		public String getMimeType()
		{
			return mimeType;
		}

		public void setMimeType(String mimeType)
		{
			this.mimeType = mimeType;
		}

		public int getRand()
		{
			return rand;
		}

		public void setRand(int rand)
		{
			this.rand = rand;
		}

		public String getDisplayIconUrl()
		{
			return displayIconUrl;
		}

		public void setDisplayIconUrl(String displayIconUrl)
		{
			this.displayIconUrl = displayIconUrl;
		}

		public String getErrorKey()
		{
			return errorKey;
		}

		public void setErrorKey(String errorKey)
		{
			this.errorKey = errorKey;
		}
	}
}
