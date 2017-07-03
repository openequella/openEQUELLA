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

package com.tle.web.controls.universal.handlers.fileupload.details;

import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.MSOffice;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import com.google.inject.Inject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.common.wizard.controls.universal.handlers.FileUploadSettings;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.guice.Bind;
import com.tle.core.services.FileSystemService;
import com.tle.core.workflow.thumbnail.service.ThumbnailService;
import com.tle.core.workflow.video.VideoService;
import com.tle.web.controls.universal.DialogRenderOptions;
import com.tle.web.controls.universal.handlers.FileConstants;
import com.tle.web.controls.universal.handlers.FileUploadHandler;
import com.tle.web.controls.universal.handlers.fileupload.UploadedFile;
import com.tle.web.inplaceeditor.service.InPlaceEditorWebService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.JQuerySelector.Type;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.wizard.impl.WebRepository;

@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class FileDetails extends AbstractDetailsEditor<FileDetails.FileDetailsModel>
{
	private static final String TOHTML_VIEWER = "tohtml";
	private static final Logger LOGGER = Logger.getLogger(FileDetails.class);

	private static final String INPLACE_APPLET_ID = "inplace_applet";
	private static final String INPLACE_APPLET_HEIGHT = "50px";
	private static final String INPLACE_APPLET_WIDTH = "320px";

	enum AppletMode
	{
		OPEN, OPENWITH
	}

	@Inject
	private InPlaceEditorWebService inplaceEditorService;
	@Inject
	private StagingService stagingService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ThumbnailService thumbnailService;
	@Inject
	private VideoService videoService;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Component
	private SingleSelectionList<NameValue> viewers;
	@Component(name = "e")
	private Div editFileDiv;
	@Component
	@PlugKey("handlers.file.details.link.editfile")
	private Link editFileLink;
	@Component
	@PlugKey("handlers.file.details.link.editfilewith")
	private Link editFileWithLink;
	@Component(name = "fc")
	private Checkbox allowFileConversion;
	@Component(name = "st")
	private Checkbox suppressThumbnails;
	private JSHandler saveClickHandler;

	@Override
	public FileDetailsModel instantiateModel(SectionInfo info)
	{
		return new FileDetailsModel();
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		JSCallable editFileAjaxFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("editFile"),
			"editFileAjaxDiv");

		editFileLink.setClickHandler(inplaceEditorService.createOpenHandler(INPLACE_APPLET_ID, false,
			Js.function(Js.call_s(editFileAjaxFunction, false))));
		editFileWithLink.setClickHandler(inplaceEditorService.createOpenHandler(INPLACE_APPLET_ID, true,
			Js.function(Js.call_s(editFileAjaxFunction, true))));

		editFileLink.addReadyStatements(
			inplaceEditorService.createHideLinksStatements(Jq.$(Type.CLASS, "editLinks"), Jq.$(editFileWithLink)));

		saveClickHandler = inplaceEditorService.createUploadHandler(INPLACE_APPLET_ID,
			events.getSubmitValuesFunction("inplaceSave"));
	}

	@Override
	public void onRegister(SectionTree tree, String parentId, FileUploadHandler handler)
	{
		super.onRegister(tree, parentId, handler);
		viewers.setListModel(handler.createViewerModel());
	}

	@Override
	public SectionRenderable renderDetailsEditor(RenderContext context, DialogRenderOptions renderOptions,
		UploadedFile uploadedFile)
	{
		FileDetailsModel model = getModel(context);
		FileUploadSettings fileSettings = getFileUploadHandler().getFileSettings();
		model.setShowPreview(fileSettings.isAllowPreviews());
		boolean showRestrict = getFileUploadHandler().canRestrictAttachments();
		model.setShowRestrict(showRestrict);
		model.setEditTitle(new TextLabel(displayName.getValue(context)));
		model.setShowViewers(!(viewers.getListModel().getOptions(context).size() == 2));

		// A bit dodge
		boolean conversion = false;
		List<Option<NameValue>> vwrs = viewers.getListModel().getOptions(context);
		for( Option<NameValue> vwr : vwrs )
		{
			String v = vwr.getValue();
			if( v.equals(TOHTML_VIEWER) )
			{
				conversion = true;
				break;
			}
		}
		model.setShowFileConversion(conversion);
		FileAttachment attachment = (FileAttachment) uploadedFile.getAttachment();
		model.setInplaceFilepath(attachment.getFilename());
		if( model.getAppletMode() != null )
		{
			editFileDiv.addReadyStatements(context, createInplaceApplet(context));
		}

		model.setShowThumbnailOption(fileSettings.isShowThumbOption());
		if( model.isShowThumbnailOption() )
		{
			if( attachment.getThumbnail() == null )
			{
				suppressThumbnails.setChecked(context, fileSettings.isSuppressThumbnails());
			}
			else
			{
				suppressThumbnails.setChecked(context, attachment.getThumbnail().equals("suppress"));
			}
		}
		else
		{
			suppressThumbnails.setDisplayed(context, false);
		}

		renderOptions.setSaveClickHandler(saveClickHandler);
		return viewFactory.createResult("file/file-edit.ftl", this);
	}

	private JSCallable createInplaceApplet(SectionInfo info)
	{
		FileDetailsModel model = getModel(info);
		final ItemId wizardStagingId = new ItemId(
			getFileUploadHandler().getDialogState().getRepository().getStagingid(), 0);
		return inplaceEditorService.createAppletFunction(INPLACE_APPLET_ID, wizardStagingId,
			ensureInplaceStagingId(info), model.getInplaceFilepath(), model.getAppletMode() == AppletMode.OPENWITH,
			"invoker/file.inplaceedit.service", Jq.$(editFileDiv), INPLACE_APPLET_WIDTH, INPLACE_APPLET_HEIGHT);
	}

	@EventHandlerMethod
	public void inplaceSave(SectionInfo info)
	{
		getFileUploadHandler().getDialogState().save(info);
	}

	@EventHandlerMethod
	public void editFile(SectionInfo info, boolean openWith)
	{
		final FileDetailsModel model = getModel(info);
		model.setAppletMode(openWith ? AppletMode.OPENWITH : AppletMode.OPEN);
	}

	@Override
	public void initialiseFromUpload(SectionInfo info, UploadedFile uploadedFile, boolean resolved)
	{
		FileAttachment file = new FileAttachment();
		file.setFilename(uploadedFile.getFilepath());
		file.setMd5sum(uploadedFile.getMd5());
		file.setDescription(uploadedFile.getDescription());
		file.setSize(uploadedFile.getSize());
		uploadedFile.setAttachment(file);
		if( resolved )
		{
			String mimeType = uploadedFile.getMimeType();

			if( mimeType != null && (mimeType.equalsIgnoreCase(FileConstants.TYPE_DOC)
				|| mimeType.equalsIgnoreCase(FileConstants.TYPE_DOCX)) )
			{
				processDocument(uploadedFile);
			}
		}
	}

	private void processDocument(UploadedFile file)
	{
		FileAttachment attachment = (FileAttachment) file.getAttachment();
		Metadata meta = new Metadata();
		meta.set(TikaMetadataKeys.RESOURCE_NAME_KEY, file.getFilename());

		StagingFile stageHandle = getFileUploadHandler().getStagingFile();
		String filepath = file.getFilepath();
		if( fileSystemService.fileExists(stageHandle, filepath) )
		{
			try( InputStream fileInputStream = fileSystemService.read(stageHandle, filepath); )
			{
				ContentHandler bcHandler = new BodyContentHandler();
				Parser parser = new AutoDetectParser(new TikaConfig(getClass().getClassLoader()));
				parser.parse(fileInputStream, bcHandler, meta, new ParseContext());

				// Author
				String author = meta.get(MSOffice.AUTHOR);
				attachment.setData("author", author != null ? author : "");

				// Publisher
				String publisher = meta.get(DublinCore.PUBLISHER);
				attachment.setData("publisher", publisher != null ? publisher : "");

				// Modified Date
				String modified = meta.get(HttpHeaders.LAST_MODIFIED);
				if( !Check.isEmpty(modified) )
				{
					UtcDate utcDate = new UtcDate(modified, Dates.ISO_NO_TIMEZONE);
					attachment.setData("lastmodified", utcDate.toDate());
				}

				// Page Count
				String pcount = meta.get(MSOffice.PAGE_COUNT);
				attachment.setData("pagecount", pcount != null ? pcount : "");

				// Word Count
				String wcount = meta.get(MSOffice.WORD_COUNT);
				attachment.setData("wordcount", wcount != null ? wcount : "");
			}
			catch( Exception ex )
			{
				// Don't throw an exception or the user won't be able to upload
				// the attachment at all. Just log it and continue.
				LOGGER.error("Error retrieving details for attachment", ex);
			}
		}
	}

	@Override
	public void prepareForEdit(SectionInfo info, UploadedFile uploadedFile)
	{
		uploadedFile.setResolvedType(FileUploadHandler.FILE_TYPE_FILE);
	}

	@Override
	public void cleanup(SectionInfo info, UploadedFile uploadedFile)
	{
		FileDetailsModel model = getModel(info);
		String inplaceStagingId = model.getInplaceStagingId();
		if( !Check.isEmpty(inplaceStagingId) )
		{
			stagingService.removeStagingArea(new StagingFile(inplaceStagingId), true);
			model.setInplaceStagingId(null);
		}
	}

	@Override
	public void removeAttachment(SectionInfo info, Attachment attachment, boolean willBeReplaced)
	{
		super.removeAttachment(info, attachment, willBeReplaced);
		FileAttachment file = (FileAttachment) attachment;
		final StagingFile wizardStaging = getFileUploadHandler().getStagingFile();
		String filename = file.getFilename();
		fileSystemService.removeFile(wizardStaging, filename);
		String thumbnail = file.getThumbnail();
		if( !Check.isEmpty(thumbnail) && !thumbnail.equals("suppress") )
		{
			removeThumbnail(wizardStaging, thumbnail, filename);
		}
	}

	private void removeThumbnail(StagingFile staging, String thumbnail, String filename)
	{
		fileSystemService.removeFile(staging, thumbnail);
		thumbnail = thumbnail.replace(FileSystemService.THUMBNAIL_EXTENSION,
			FileSystemService.GALLERY_THUMBNAIL_EXTENSION);
		fileSystemService.removeFile(staging, thumbnail);
		thumbnail = thumbnail.replace(FileSystemService.GALLERY_THUMBNAIL_EXTENSION,
			FileSystemService.GALLERY_PREVIEW_EXTENSION);
		fileSystemService.removeFile(staging, thumbnail);
		fileSystemService.removeFile(staging, FileSystemService.VIDEO_PREVIEW_FOLDER + "/" + filename + ".mp4");
		fileSystemService.removeFile(staging, FileSystemService.TILES_FOLDER + "/" + filename);
	}

	private String ensureInplaceStagingId(SectionInfo info)
	{
		final FileDetailsModel model = getModel(info);
		String inplaceStagingId = model.getInplaceStagingId();
		if( Check.isEmpty(inplaceStagingId) )
		{
			inplaceStagingId = stagingService.createStagingArea().getUuid();
			model.setInplaceStagingId(inplaceStagingId);
		}
		return inplaceStagingId;
	}

	@Override
	public void setupDetailsForEdit(SectionInfo info, UploadedFile uploadedFile)
	{
		super.setupDetailsForEdit(info, uploadedFile);
		FileAttachment attachment = (FileAttachment) uploadedFile.getAttachment();
		viewers.setSelectedStringValue(info, attachment.getViewer());
		allowFileConversion.setChecked(info, attachment.isConversion());
	}

	@Override
	public void commitNew(SectionInfo info, UploadedFile uploadedFile, String replacementUuid)
	{
		FileAttachment file = (FileAttachment) uploadedFile.getAttachment();
		FileUploadHandler hdlr = getFileUploadHandler();
		WebRepository repo = hdlr.getDialogState().getRepository();
		StagingFile wizardStaging = hdlr.getStagingFile();
		String targetFilePath = uploadedFile.getIntendedFilepath();
		String filepath = uploadedFile.getFilepath();
		StagingFile sourceStaging = getInplaceStagingForFilepath(info, filepath);
		if( sourceStaging == null )
		{
			sourceStaging = wizardStaging;
		}
		move(sourceStaging, filepath, wizardStaging, targetFilePath);
		file.setFilename(targetFilePath);

		FileUploadSettings fileSettings = getFileUploadHandler().getFileSettings();
		boolean generateThumbs = (!uploadedFile.isDetailEditing() || !fileSettings.isShowThumbOption())
			&& !fileSettings.isSuppressThumbnails();

		if( generateThumbs || (uploadedFile.isDetailEditing() && fileSettings.isShowThumbOption()
			&& !suppressThumbnails.isChecked(info)) )
		{
			ViewableItem<?> viewableItem = getFileUploadHandler().getDialogState().getViewableItem(info);
			String thumbnail = thumbnailService.submitThumbnailRequest(viewableItem.getItemId(), wizardStaging,
				targetFilePath, true, false);
			file.setThumbnail(thumbnail);

			if( videoService.canConvertVideo(targetFilePath) )
			{
				videoService.makeGalleryVideoPreviews(wizardStaging, targetFilePath);
			}
		}
		else
		{
			file.setThumbnail("suppress");
		}

		if( FileUploadHandler.isWebPage(targetFilePath) )
		{
			repo.getState().getWizardMetadataMapper().addHtmlMappedFile(targetFilePath);
		}
		if( uploadedFile.isDetailEditing() )
		{
			saveDetailsToAttachment(info, uploadedFile, file);
		}
		super.commitNew(info, uploadedFile, replacementUuid);
	}

	@Nullable
	private StagingFile getInplaceStagingForFilepath(SectionInfo info, String filepath)
	{
		String inplaceStagingId = getModel(info).getInplaceStagingId();
		if( !Check.isEmpty(inplaceStagingId) )
		{
			StagingFile inplaceStagingFile = new StagingFile(inplaceStagingId);
			if( fileSystemService.fileExists(inplaceStagingFile, filepath) )
			{
				return inplaceStagingFile;
			}
		}
		return null;
	}

	@Override
	public void saveDetailsToAttachment(SectionInfo info, UploadedFile uploadedFile, Attachment attachment)
	{
		super.saveDetailsToAttachment(info, uploadedFile, attachment);
		FileAttachment file = (FileAttachment) uploadedFile.getAttachment();
		file.setViewer(viewers.getSelectedValueAsString(info));
		file.setConversion(allowFileConversion.isChecked(info));
	}

	@Override
	public void commitEdit(SectionInfo info, UploadedFile ua, Attachment attachment)
	{
		ua.setAttachment(attachment);
		saveDetailsToAttachment(info, ua, attachment);
		String filepath = attachment.getUrl();
		String thumb = attachment.getThumbnail();
		StagingFile wizardStaging = getFileUploadHandler().getStagingFile();

		if( getFileUploadHandler().getFileSettings().isShowThumbOption() )
		{
			if( !suppressThumbnails.isChecked(info) && (Check.isEmpty(thumb) || thumb.equals("suppress")) )
			{
				ViewableItem<?> viewableItem = getFileUploadHandler().getDialogState().getViewableItem(info);
				String thumbnail = thumbnailService.submitThumbnailRequest(viewableItem.getItemId(), wizardStaging,
					filepath, true, false);
				attachment.setThumbnail(thumbnail);
				if( videoService.canConvertVideo(filepath) )
				{
					videoService.makeGalleryVideoPreviews(wizardStaging, filepath);
				}
			}
			else if( suppressThumbnails.isChecked(info) && (!Check.isEmpty(thumb) || !thumb.equals("suppress")) )
			{
				removeThumbnail(wizardStaging, thumb, filepath);
				attachment.setThumbnail("suppress");
			}
		}
		StagingFile inplaceStaging = getInplaceStagingForFilepath(info, filepath);
		if( inplaceStaging != null )
		{
			move(inplaceStaging, filepath, getFileUploadHandler().getStagingFile(), filepath);
		}
	}

	@NonNullByDefault(false)
	public static class FileDetailsModel extends AbstractDetailsEditor.Model
	{
		private boolean showFileConversion;
		private boolean showThumbnailOption;
		@Bookmarked(name = "ips")
		private String inplaceStagingId;
		private String inplaceFilepath;
		@Bookmarked(name = "am")
		private AppletMode appletMode;

		public boolean isShowFileConversion()
		{
			return showFileConversion;
		}

		public void setShowFileConversion(boolean showFileConversion)
		{
			this.showFileConversion = showFileConversion;
		}

		public String getInplaceStagingId()
		{
			return inplaceStagingId;
		}

		public void setInplaceStagingId(String inplaceStagingId)
		{
			this.inplaceStagingId = inplaceStagingId;
		}

		public String getInplaceFilepath()
		{
			return inplaceFilepath;
		}

		public void setInplaceFilepath(String inplaceFilepath)
		{
			this.inplaceFilepath = inplaceFilepath;
		}

		public AppletMode getAppletMode()
		{
			return appletMode;
		}

		public void setAppletMode(AppletMode appletMode)
		{
			this.appletMode = appletMode;
		}

		public boolean isShowThumbnailOption()
		{
			return showThumbnailOption;
		}

		public void setShowThumbnailOption(boolean showThumbnailOption)
		{
			this.showThumbnailOption = showThumbnailOption;
		}
	}

	public SingleSelectionList<NameValue> getViewers()
	{
		return viewers;
	}

	public Div getEditFileDiv()
	{
		return editFileDiv;
	}

	public Link getEditFileLink()
	{
		return editFileLink;
	}

	public Link getEditFileWithLink()
	{
		return editFileWithLink;
	}

	public Checkbox getAllowFileConversion()
	{
		return allowFileConversion;
	}

	public Checkbox getSuppressThumbnails()
	{
		return suppressThumbnails;
	}
}
