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

package com.tle.web.htmleditor.tinymce.addon.tle.selection;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.common.Constants;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileEntry;
import com.tle.core.services.FileSystemService;
import com.tle.mypages.service.MyPagesService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.htmleditor.service.HtmlEditorService;
import com.tle.web.htmleditor.tinymce.addon.tle.AbstractSelectionAddon;
import com.tle.web.htmleditor.tinymce.addon.tle.selection.FileUploadSection.FileUploadModel.PageGroup;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.FileUpload;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;
import com.tle.web.wizard.WizardService;
import com.tle.web.wizard.WizardStateInterface;

/**
 * @author aholland
 */
public class FileUploadSection extends AbstractPrototypeSection<FileUploadSection.FileUploadModel>
	implements
		HtmlRenderer
{
	protected PluginResourceHelper resources = ResourcesService.getResourceHelper(FileUploadSection.class);

	@PlugKey("upload.select")
	private static Label LABEL_UPLOADSELECT;

	@Inject
	private SelectionService selectionService;
	@Inject
	private MyPagesService myPagesService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private WizardService wizardService;
	@Inject
	private AttachmentResourceService attachmentResourceService;

	@EventFactory
	protected EventGenerator events;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component
	private FileUpload fileUpload;
	@Component
	private TextField fileName;
	@Component
	private Button upload;

	@SuppressWarnings("nls")
	@Override
	public SectionResult renderHtml(RenderEventContext info)
	{
		SelectionSession session = selectionService.getCurrentSession(info);
		String wizardSessionId = session.getAttribute(AbstractSelectionAddon.SESSION_ID);
		String pageId = session.getAttribute(AbstractSelectionAddon.PAGE_ID);

		WizardStateInterface wizardState = myPagesService.getState(info, wizardSessionId);
		FileHandle staging = wizardState.getFileHandle();
		Item item = wizardState.getItem();

		List<PageGroup> pageAttachments = new ArrayList<PageGroup>();
		addGroup(pageAttachments, getPageGroup(staging, item, info, wizardState));

		if( pageId != null && !pageId.equals(HtmlEditorService.CONTENT_DIRECTORY) )
		{
			HtmlAttachment page = myPagesService.getPageAttachment(info, wizardSessionId, null, pageId);
			addGroup(pageAttachments, getPageGroup(staging, info, page, null, null, true, wizardState));
		}
		// also check metadata HTMLs
		if( fileSystemService.fileExists(staging, HtmlEditorService.CONTENT_DIRECTORY) )
		{
			addGroup(
				pageAttachments,
				getPageGroup(staging, info, null, resources.getString("upload.htmlcontrolattachments"),
					HtmlEditorService.CONTENT_DIRECTORY, true, wizardState));
		}

		FileUploadModel model = getModel(info);
		model.setPages(pageAttachments);

		return viewFactory.createTemplateResult("fileupload.ftl", info); //$NON-NLS-1$
	}

	private void addGroup(List<PageGroup> pageAttachments, PageGroup pageGroup)
	{
		if( !pageGroup.getFiles().isEmpty() )
		{
			pageAttachments.add(pageGroup);
		}
	}

	private PageGroup getPageGroup(FileHandle staging, Item item, SectionContext context,
		WizardStateInterface wizardState)
	{
		return getPageGroup(staging, context, null,
			resources.getString("upload.itemattachments"), "", false, wizardState); //$NON-NLS-1$//$NON-NLS-2$
	}

	private PageGroup getPageGroup(FileHandle staging, SectionContext context, HtmlAttachment page, String pageName,
		String pageFolder, boolean recurse, WizardStateInterface wizardState)
	{
		try
		{
			if( page != null )
			{
				pageName = page.getDescription();
				pageFolder = page.getFolder();
			}

			List<Attachment> attachments = new ArrayList<Attachment>();
			addFiles(fileSystemService.enumerate(staging, pageFolder, null), pageFolder, attachments, (recurse
				? staging : null), wizardState);
			return new PageGroup(pageName, summarise(attachments, context, wizardState));
		}
		catch( IOException io )
		{
			throw new RuntimeException(io);
		}
	}

	private void addFiles(FileEntry[] files, String path, List<Attachment> attachments, FileHandle recurse,
		WizardStateInterface wizardState)
	{
		for( FileEntry file : files )
		{
			String filename = file.getName();
			if( !file.isFolder() )
			{
				// dodgy
				if( !filename.toLowerCase().endsWith(".html") ) //$NON-NLS-1$
				{
					FileAttachment newb = new FileAttachment();
					newb.setDescription(filename);
					newb.setFilename(PathUtils.filePath(path, filename));
					attachments.add(newb);
				}
			}
			else if( recurse != null )
			{
				try
				{
					String subFolder = PathUtils.filePath(path, filename);
					addFiles(fileSystemService.enumerate(recurse, subFolder, null), subFolder, attachments, recurse,
						wizardState);
				}
				catch( IOException io )
				{
					throw new RuntimeException();
				}
			}
		}
	}

	private List<AttachmentView> summarise(List<Attachment> attachments, SectionInfo info,
		WizardStateInterface wizardState)
	{
		final List<AttachmentView> viewList = new ArrayList<FileUploadSection.AttachmentView>();
		ViewableItem vitem = wizardService.createViewableItem(wizardState);
		for( Attachment attachment : attachments )
		{
			final ViewableResource viewableResource = attachmentResourceService.getViewableResource(info, vitem,
				attachment);
			HtmlComponentState comModel = new HtmlComponentState(LABEL_UPLOADSELECT, events.getNamedHandler(
				"selectFile", attachment.getUrl(), attachment.getDescription())); //$NON-NLS-1$

			TextLabel label = new TextLabel(attachment.getDescription());
			HtmlLinkState viewLink = new HtmlLinkState(label, viewableResource.createDefaultViewerUrl());
			viewLink.setTarget("_blank"); //$NON-NLS-1$
			viewList
				.add(new AttachmentView(comModel, viewableResource.createStandardThumbnailRenderer(label), viewLink));
		}
		return viewList;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		upload.setClickHandler(events.getNamedHandler("uploadFile")); //$NON-NLS-1$
	}

	@EventHandlerMethod
	public void selectFile(SectionInfo info, String filename, String title)
	{
		final SelectionSession session = selectionService.getCurrentSession(info);
		final String wizardSessionId = session.getAttribute(AbstractSelectionAddon.SESSION_ID);
		returnResource(info, wizardSessionId, filename, title);
	}

	private void returnResource(SectionInfo info, String sessionId, String filename, String title)
	{
		WizardStateInterface wizardState = myPagesService.getState(info, sessionId);
		SelectedResource selection = new SelectedResource();
		selection.setPreviewId(sessionId, wizardState.getStagingId());
		selection.setUrl(filename);
		selection.setTitle(title);
		selection.setType(SelectedResource.TYPE_FILE);
		selectionService.addSelectedResource(info, selection, true);
		selectionService.returnFromSession(info);
	}

	@EventHandlerMethod
	public void uploadFile(SectionInfo info) throws IOException
	{
		final FileUploadModel model = getModel(info);

		String description = fileName.getValue(info);
		boolean hasFile = fileUpload.getFileSize(info) > 0;
		if( hasFile )
		{
			final String filename = fileUpload.getFilename(info);
			if( Check.isEmpty(description) )
			{
				description = filename;
			}

			final SelectionSession session = selectionService.getCurrentSession(info);
			final String wizardSessionId = session.getAttribute(AbstractSelectionAddon.SESSION_ID);
			final String pageId = session.getAttribute(AbstractSelectionAddon.PAGE_ID);

			try (InputStream in = fileUpload.getInputStream(info))
			{
				final FileAttachment newb = myPagesService.uploadStream(info, wizardSessionId, pageId,
					PathUtils.fileencode(filename), description, in);
				returnResource(info, wizardSessionId, newb.getUrl(), description);
			}
		}

		model.setNoFile(!hasFile);
	}

	@Override
	public Class<FileUploadModel> getModelClass()
	{
		return FileUploadModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return Constants.BLANK;
	}

	public FileUpload getFileUpload()
	{
		return fileUpload;
	}

	public TextField getFileName()
	{
		return fileName;
	}

	public Button getUpload()
	{
		return upload;
	}

	public static class FileUploadModel
	{
		private List<PageGroup> pages;
		@Bookmarked
		private boolean noFile;
		private String error;

		public List<PageGroup> getPages()
		{
			return pages;
		}

		public void setPages(List<PageGroup> pages)
		{
			this.pages = pages;
		}

		public boolean isNoFile()
		{
			return noFile;
		}

		public void setNoFile(boolean noFile)
		{
			this.noFile = noFile;
		}

		public String getError()
		{
			return error;
		}

		public void setError(String error)
		{
			this.error = error;
		}

		public static class PageGroup
		{
			private final String pageName;
			private final List<AttachmentView> files;

			public PageGroup(String pageName, List<AttachmentView> files)
			{
				this.pageName = pageName;
				this.files = files;
			}

			public String getPageName()
			{
				return pageName;
			}

			public List<AttachmentView> getFiles()
			{
				return files;
			}
		}
	}

	public static class AttachmentView
	{
		private final HtmlComponentState selectButton;
		private final ImageRenderer icon;
		private final HtmlLinkState viewLink;

		public AttachmentView(HtmlComponentState selectButton, ImageRenderer icon, HtmlLinkState viewLink)
		{
			this.selectButton = selectButton;
			this.icon = icon;
			this.viewLink = viewLink;
		}

		public HtmlComponentState getSelectButton()
		{
			return selectButton;
		}

		public ImageRenderer getIcon()
		{
			return icon;
		}

		public HtmlLinkState getViewLink()
		{
			return viewLink;
		}

	}
}
