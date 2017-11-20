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

package com.tle.mypages.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.Constants;
import com.dytech.edge.common.FileInfo;
import com.dytech.edge.common.ScriptContext;
import com.dytech.edge.wizard.WizardException;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.google.inject.Provider;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.quota.exception.QuotaExceededException;
import com.tle.common.workflow.Workflow;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.guice.BindFactory;
import com.tle.core.hibernate.equella.service.InitialiserService;
import com.tle.core.item.navigation.NavigationNodeHelper;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.item.service.ItemLockingService;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.item.standard.operations.EditMetadataOperation;
import com.tle.core.item.standard.operations.workflow.StatusOperation;
import com.tle.core.quota.service.QuotaService;
import com.tle.core.services.FileSystemService;
import com.tle.mycontent.MyContentConstants;
import com.tle.mycontent.service.MyContentFields;
import com.tle.mycontent.workflow.operations.OperationFactory;
import com.tle.mypages.MyPagesConstants;
import com.tle.mypages.parse.ConvertHtmlService;
import com.tle.mypages.parse.conversion.HrefConversion;
import com.tle.mypages.parse.conversion.StagingConversion;
import com.tle.mypages.web.MyPagesRepository;
import com.tle.mypages.web.MyPagesState;
import com.tle.mypages.web.event.SaveItemEvent;
import com.tle.mypages.web.event.SavePageEvent;
import com.tle.mypages.workflow.operation.UnusedContentCleanupOperation.UnusedContentCleanupOperationFactory;
import com.tle.web.htmleditor.service.HtmlEditorService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;
import com.tle.web.viewurl.attachments.ItemNavigationService;
import com.tle.web.wizard.WizardService;
import com.tle.web.wizard.WizardStateInterface;

/**
 * @author aholland
 */
// TODO: make subclass of WizardService and override methods
@Bind(MyPagesService.class)
@Singleton
public class MyPagesServiceImpl implements MyPagesService
{
	@Inject
	private ItemService itemService;
	@Inject
	private ItemLockingService lockService;
	@Inject
	private ItemFileService itemFileService;
	@Inject
	private ItemNavigationService itemNavService;
	@Inject
	private NavigationNodeHelper navHelper;
	@Inject
	private QuotaService quotaService;
	@Inject
	private InitialiserService initialiserService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ViewableItemFactory viewableItemFactory;
	@Inject
	private ConvertHtmlService convertHtmlService;
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private WizardService wizardService;
	@Inject
	private Provider<MyPagesRepository> repoProvider;
	@Inject
	private UnusedContentCleanupOperationFactory unusedFactory;
	@Inject
	private ItemOperationFactory workflowFactory;
	@Inject
	private OperationFactory myContentFactory;
	@Inject
	private com.tle.mypages.workflow.operation.OperationFactory myPagesFactory;
	@Inject
	private MyPagesStateFactory myPagesStateFactory;

	@Override
	public WorkflowOperation getEditOperation(MyContentFields fields, String filename, InputStream inputStream,
		boolean removeExistingAttachments, boolean useExistingAttachments)
	{
		return myPagesFactory.create(fields, filename, inputStream, removeExistingAttachments, useExistingAttachments);
	}

	@Override
	public WizardStateInterface getState(SectionInfo info, String sessionId)
	{
		final WizardStateInterface state = wizardService.getFromSession(info, sessionId);
		if( state instanceof MyPagesState )
		{
			MyPagesState myPagesState = (MyPagesState) state;
			if( myPagesState.getWorkflowStatus() == null )
			{
				StatusOperation statOp = myContentFactory.status();
				try
				{
					itemService.operation(state.getItemId(), new WorkflowOperation[]{statOp});
				}
				catch( Exception e )
				{
					throw new RuntimeException(e);
				}
				myPagesState.setWorkflowStatus(statOp.getStatus());
			}
		}
		return state;
	}

	@Override
	public void updateSession(SectionInfo info, WizardStateInterface state)
	{
		wizardService.updateSession(info, state);
	}

	@Override
	public void removeFromSession(SectionInfo info, String id)
	{
		wizardService.removeFromSession(info, id, false);
	}

	@Override
	public void convertPreviewUrlsToItemUrls(final WizardStateInterface state)
	{
		// TODO: Make this only execute if we're actually editing the html
		final List<HtmlAttachment> htmls = state.getAttachments().getList(AttachmentType.HTML);
		for( HtmlAttachment htmlAttach : htmls )
		{
			final FileHandle staging = state.getFileHandle();
			final String pageFilename = htmlAttach.getFilename();

			String newHtml = forFile(staging, pageFilename, new Function<Reader, String>()
			{
				@Override
				@Nullable
				public String apply(@Nullable Reader input)
				{
					final String wizid = state.getWizid();
					return convertHtmlService.convert(input, false,
						new StagingConversion(false, state.getItemId(), wizid, state.getStagingId()).getConversions());
				}
			});

			saveHtml(staging, pageFilename, newHtml);
		}
	}

	@Override
	public void commitDraft(final WizardStateInterface state, final boolean leaveAsPreview, SectionInfo info)
	{
		// copy stuff over from the draft folder:
		final StagingFile staging = (StagingFile) state.getFileHandle();
		final List<HtmlAttachment> htmls = state.getAttachments().getList(AttachmentType.HTML);
		for( HtmlAttachment htmlAttach : htmls )
		{
			final String draftFolder = htmlAttach.getDraftFolder();
			final String normalFolder = htmlAttach.getNormalFolder();

			if( htmlAttach.isDelete() )
			{
				state.getAttachments().removeAttachment(htmlAttach);
				fileSystemService.removeFile(staging, draftFolder);
				fileSystemService.removeFile(staging, normalFolder);
			}
			else if( htmlAttach.isDraft() )
			{
				final String draftFilename = htmlAttach.getFilename();

				String newHtml = forFile(staging, draftFilename, new Function<Reader, String>()
				{
					@Override
					@Nullable
					public String apply(@Nullable Reader input)
					{
						ItemKey destItem = (leaveAsPreview ? null : state.getItemId());
						StagingConversion conversion = new StagingConversion(false, destItem, state.getWizid(),
							state.getStagingId(), draftFolder, normalFolder);

						return convertHtmlService.convert(input, false, conversion.getConversions());
					}
				});

				saveHtml(staging, draftFilename, newHtml);

				// FIXME: this is dangerous, if move fails below we are in a
				// world of pain
				if( fileSystemService.fileExists(staging, normalFolder) )
				{
					fileSystemService.removeFile(staging, normalFolder);
				}
				fileSystemService.move(staging, draftFolder, normalFolder);

				htmlAttach.setDraft(false);
			}

			htmlAttach.setNew(false);
		}

		// delete the root draft folder
		fileSystemService.removeFile(staging, HtmlAttachment.DRAFT_FOLDER);
	}

	@Override
	public void clearDraft(WizardStateInterface state)
	{
		final List<Attachment> newAttachments = new ArrayList<Attachment>();

		// if it is a new attachment (i.e. hasn't been saved before) get rid of
		// it
		// first.
		final List<Attachment> allAttachments = state.getAttachments().getList(AttachmentType.HTML);
		for( Attachment attachment : allAttachments )
		{
			final HtmlAttachment htmlAttach = (HtmlAttachment) attachment;
			if( htmlAttach.isDelete() )
			{
				htmlAttach.setDelete(false);
			}
			else if( htmlAttach.isNew() )
			{
				newAttachments.add(attachment);
			}
			if( htmlAttach.isDraft() )
			{
				final StagingFile staging = (StagingFile) state.getFileHandle();
				final String draftFolder = htmlAttach.getDraftFolder();
				if( fileSystemService.fileExists(staging, draftFolder) )
				{
					fileSystemService.removeFile(staging, draftFolder);
				}
				htmlAttach.setDraft(false);
			}
		}
		state.getAttachments().removeAll(newAttachments);
	}

	@Override
	public void commitDraft(SectionInfo info, String sessionId)
	{
		WizardStateInterface state = this.getState(info, sessionId);
		this.commitDraft(state, true, info);
	}

	@Override
	public void clearDraft(SectionInfo info, String sessionId)
	{
		WizardStateInterface state = this.getState(info, sessionId);
		this.clearDraft(state);
	}

	protected MyPagesState doSave(MyPagesState state, SectionInfo info)
	{
		try
		{
			commitDraft(state, false, info);

			final String stagingId = state.getStagingId();
			if( !Check.isEmpty(stagingId) )
			{
				try
				{
					quotaService.checkQuotaAndReturnNewItemSize(state.getItemPack().getItem(),
						new StagingFile(stagingId));
				}
				catch( QuotaExceededException e )
				{
					throw new WizardException(e.getMessage());
				}
			}

			final List<WorkflowOperation> oplist = new ArrayList<WorkflowOperation>();
			oplist.add(myContentFactory.status());
			EditMetadataOperation editMeta = workflowFactory.editMetadata(state.getItemPack());
			editMeta.setInitialStatus(ItemStatus.PERSONAL);
			oplist.add(editMeta);
			oplist.add(workflowFactory.saveWithOperations(true, new ArrayList<WorkflowOperation>(),
				Collections.singletonList((WorkflowOperation) unusedFactory.create())));

			final ItemPack pack = itemService.operation(state.getItemId(),
				oplist.toArray(new WorkflowOperation[oplist.size()]));
			setItemPack(pack, state);
			state.setNewItem(false);
			return state;
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public MyPagesState loadItem(SectionInfo info, ItemId id)
	{
		try
		{
			MyPagesState state = myPagesStateFactory.createState();
			state.setNewItem(false);
			state.setItemId(id);

			// always unlock a myPages item
			lockService.unlock(itemService.get(id), true);

			StatusOperation statop = myContentFactory.status();
			List<WorkflowOperation> ops = new ArrayList<WorkflowOperation>();
			ops.add(workflowFactory.startEdit(true));
			ops.add(statop);

			itemService.operation(state.getItemId(), ops.toArray(new WorkflowOperation[ops.size()]));

			ItemPack itemPack = statop.getItemPack();
			setItemPack(itemPack, state);

			wizardService.addToSession(info, state, false);

			return state;
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public MyPagesState newItem(SectionInfo info, ItemDefinition itemDef)
	{
		try
		{
			ItemPack<Item> pack = itemService.operation(null, workflowFactory.create(itemDef, ItemStatus.PERSONAL));
			pack.getXml().setNode(MyContentConstants.CONTENT_TYPE_NODE, MyPagesConstants.MYPAGES_CONTENT_TYPE);

			MyPagesState state = myPagesStateFactory.createState();
			Item item = pack.getItem();
			item.setItemDefinition(itemDef);
			setItemPack(pack, state);
			state.setItemId(new ItemId(item.getUuid(), item.getVersion()));
			state.setNewItem(true);

			wizardService.addToSession(info, state, false);

			return state;
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	private void setItemPack(ItemPack<Item> pack, MyPagesState state)
	{
		Item item = pack.getItem();
		ItemDefinition collection = item.getItemDefinition();
		Schema schema = initialiserService.initialise(collection.getSchema());
		Workflow workflow = initialiserService.initialise(collection.getWorkflow());
		collection = initialiserService.initialise(collection);
		item = initialiserService.initialise(item);
		item.setItemDefinition(collection);
		collection.setSchema(schema);
		collection.setWorkflow(workflow);
		state.setItemPack(pack);
	}

	@Override
	public HtmlAttachment newPage(SectionInfo info, String sessionId, String pageName)
	{
		try
		{
			final HtmlAttachment page = new HtmlAttachment();
			page.setDescription(pageName);
			page.setNew(true);
			page.setDraft(true);

			final MyPagesRepository repos = repoProvider.get();
			repos.setState(getState(info, sessionId));
			repos.getAttachments().addAttachment(page);

			return page;
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);

		}
	}

	@Override
	public HtmlAttachment clonePage(final WizardStateInterface state, final Item sourceItem,
		final HtmlAttachment sourcePage, boolean draft)
	{
		final HtmlAttachment newPage = (HtmlAttachment) sourcePage.clone();
		newPage.setUuid(UUID.randomUUID().toString());
		newPage.setId(0);
		newPage.setNew(draft);
		newPage.setDraft(draft);

		ItemFile itemFile = itemFileService.getItemFile(sourceItem);

		final String newHtml = forFile(itemFile, sourcePage.getFilename(), new Function<Reader, String>()
		{
			@Override
			@Nullable
			public String apply(@Nullable Reader input)
			{
				List<HrefConversion> conversions = new StagingConversion(true, sourceItem.getItemId(), state.getWizid(),
					state.getStagingId(), sourcePage.getFolder(), newPage.getFolder()).getConversions();
				return convertHtmlService.convert(input, false, conversions);
			}
		});

		final FileHandle destHandle = state.getFileHandle();
		// clone attachments first
		fileSystemService.copy(itemFile, sourcePage.getFolder(), destHandle, newPage.getFolder());

		saveHtml(destHandle, newPage.getFilename(), newHtml);

		return newPage;
	}

	@Override
	public String getDraftHtml(final WizardStateInterface state, SectionInfo info, final HtmlAttachment attachment,
		final ItemKey itemId)
	{
		final String sessionId = state.getWizid();
		final FileHandle staging = state.getFileHandle();
		if( !attachment.isDraft() )
		{
			// copy files to the draft folder and then make it draft.
			fileSystemService.copy(staging, attachment.getNormalFolder(), attachment.getDraftFolder());
			attachment.setDraft(true);
		}

		if( !fileSystemService.fileExists(staging, attachment.getFilename()) )
		{
			return Constants.BLANK;
		}

		return forFile(staging, attachment.getFilename(), new Function<Reader, String>()
		{
			@Override
			@Nullable
			public String apply(@Nullable Reader input)
			{
				StagingConversion draftConversion = new StagingConversion(true, itemId, sessionId, state.getStagingId(),
					attachment.getNormalFolder(), attachment.getDraftFolder());
				StagingConversion stagingConversion = new StagingConversion(true, itemId, sessionId,
					state.getStagingId());

				final List<HrefConversion> conversions = new ArrayList<HrefConversion>();
				conversions.addAll(draftConversion.getConversions());
				conversions.addAll(stagingConversion.getConversions());

				return convertHtmlService.convert(input, true, conversions);
			}
		});
	}

	@SuppressWarnings("nls")
	@Override
	public void setHtml(SectionInfo info, String sessionId, HtmlAttachment attachment, String html)
	{
		try
		{
			final FileInfo finfo = saveHtml(getState(info, sessionId).getFileHandle(), attachment.getFilename(),
				(html == null ? "" : html));
			attachment.setSize(finfo.getLength());
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("nls")
	@Override
	public FileAttachment uploadStream(SectionInfo info, String sessionId, String pageUuid, String fileName,
		String description, InputStream input)
	{
		try
		{
			MyPagesRepository repos = repoProvider.get();
			repos.setState(getState(info, sessionId));

			String fullpath;
			if( pageUuid != null && !pageUuid.equals(HtmlEditorService.CONTENT_DIRECTORY) )
			{
				HtmlAttachment page = getPageAttachment(info, sessionId, null, pageUuid);
				if( page != null )
				{
					fullpath = PathUtils.filePath(page.getFolder(), fileName);
				}
				else
				{
					throw new IllegalArgumentException("Page '" + pageUuid + "' does not exist");
				}
			}
			else
			{
				fullpath = PathUtils.filePath(HtmlEditorService.CONTENT_DIRECTORY, fileName);
			}

			FileInfo finfo = repos.uploadStream(fullpath, input, false);
			FileAttachment newAttach = new FileAttachment();
			newAttach.setFilename(finfo.getFilename());
			newAttach.setDescription(description);
			newAttach.setSize(finfo.getLength());
			newAttach.setUrl(newAttach.getFilename());
			return newAttach;
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean saveItem(SectionInfo info, SectionTree tree, String sessionId)
	{
		MyPagesState state = (MyPagesState) getState(info, sessionId);
		treeMeUp(sessionId, info, state.getItem());

		final Set<String> handled = new HashSet<String>();
		navHelper.save(state.getItemxml(), state.getItemPack(), handled);
		for( String h : handled )
		{
			state.getItemxml().deleteNode(h);
		}

		// throw out the event
		SaveItemEvent save = new SaveItemEvent(state.getItemPack(), sessionId);
		info.processEvent(save, tree);

		if( save.isCommit() )
		{
			doSave(state, info);
			return true;
		}
		return false;
	}

	protected void treeMeUp(String sessionId, SectionInfo info, Item item)
	{
		item.getTreeNodes().clear();

		itemNavService.populateTreeNavigationFromAttachments(item, item.getTreeNodes(),
			getPageAttachments(info, sessionId, null), new ItemNavigationService.NodeAddedCallback()
			{
				@Override
				public void execute(int index, ItemNavigationNode node)
				{
					for( ItemNavigationTab tab : node.getTabs() )
					{
						tab.setViewer("myPagesViewer"); //$NON-NLS-1$ TODO: constantize
					}
				}
			});
	}

	@Override
	public void savePage(SectionInfo info, SectionTree tree, String sessionId, String pageUuid)
	{
		if( !Check.isEmpty(pageUuid) )
		{
			HtmlAttachment page = getPageAttachment(info, sessionId, null, pageUuid);
			SavePageEvent event = new SavePageEvent(page, sessionId);
			info.processEvent(event, tree);
		}
	}

	@Override
	public HtmlAttachment getFirstAvailablePage(SectionInfo info, String sessionId)
	{
		// move to the first available page
		List<HtmlAttachment> allPages = getPageAttachments(info, sessionId, null);
		if( allPages != null )
		{
			for( HtmlAttachment page : allPages )
			{
				if( !page.isDelete() )
				{
					return page;
				}
			}
		}
		return null;
	}

	@Override
	public HtmlAttachment findNextAvailablePage(SectionInfo info, String sessionId, String pageUuid)
	{
		final List<HtmlAttachment> allPages = getPageAttachments(info, sessionId, null);
		if( allPages != null )
		{
			HtmlAttachment prev = null;
			boolean returnNext = false;
			for( HtmlAttachment page : allPages )
			{
				if( !page.isDelete() )
				{
					if( returnNext )
					{
						return page;
					}
					prev = page;
				}
				if( page.getUuid().equals(pageUuid) )
				{
					returnNext = true;
				}
			}
			return prev;
		}
		return null;
	}

	@Override
	public void deletePage(SectionInfo info, String sessionId, String pageUuid)
	{
		HtmlAttachment page = getPageAttachment(info, sessionId, null, pageUuid);
		page.setDelete(true);
	}

	@Override
	public void deletePageFiles(SectionInfo info, String sessionId, HtmlAttachment htmlAttach)
	{
		WizardStateInterface state = getState(info, sessionId);
		final StagingFile staging = (StagingFile) state.getFileHandle();

		final String draftFolder = htmlAttach.getDraftFolder();
		final String normalFolder = htmlAttach.getNormalFolder();

		fileSystemService.removeFile(staging, draftFolder);
		fileSystemService.removeFile(staging, normalFolder);
	}

	@Override
	public HtmlAttachment getPageAttachment(SectionInfo info, String sessionId, String itemId, String pageUuid)
	{
		for( HtmlAttachment attachment : getPageAttachments(info, sessionId, itemId) )
		{
			if( attachment.getUuid().equals(pageUuid) )
			{
				return attachment;
			}
		}
		return null;
	}

	@Override
	public List<HtmlAttachment> getPageAttachments(SectionInfo info, String sessionId, String itemId)
	{
		return getAttachments(info, sessionId, itemId).getList(AttachmentType.HTML);
	}

	@Override
	public List<HtmlAttachment> getNonDeletedPageAttachments(SectionInfo info, String sessionId, String itemId)
	{
		final List<HtmlAttachment> pageAttachments = getPageAttachments(info, sessionId, itemId);
		List<HtmlAttachment> nonDeletedAttachments = new ArrayList<HtmlAttachment>();
		for( HtmlAttachment attach : pageAttachments )
		{
			if( !attach.isDelete() )
			{
				nonDeletedAttachments.add(attach);
			}
		}

		return nonDeletedAttachments;
	}

	@Override
	public Attachments getAttachments(SectionInfo info, String sessionId, String itemId)
	{
		return new UnmodifiableAttachments(getItem(info, sessionId, itemId));
	}

	protected Item getItem(SectionInfo info, String sessionId, String itemId)
	{
		if( !Check.isEmpty(sessionId) )
		{
			return getState(info, sessionId).getItem();
		}
		else
		{
			return itemService.get(new ItemId(itemId));
		}
	}

	@Override
	public ViewableResource cloneMyContent(SectionInfo info, ViewableResource vres, String sessionId, String pageUuid)
	{
		FileHandle fileHandle = vres.getViewableItem().getFileHandle();
		ItemKey itemId = vres.getViewableItem().getItemId();
		String originalFilename = vres.getFilepath();
		// Page may be null when using the html editor control
		HtmlAttachment page = getPageAttachment(info, sessionId, itemId.toString(), pageUuid);
		String folder = ""; //$NON-NLS-1$
		if( page != null )
		{
			folder = page.getFolder();
		}
		else
		{
			folder = HtmlEditorService.CONTENT_DIRECTORY;
		}
		// Use the scrapbook item's uuid and version to avoid filename clashes
		String newFilename = PathUtils.filePath(folder, itemId.getUuid(), Integer.toString(itemId.getVersion()),
			originalFilename);
		WizardStateInterface state = getState(info, sessionId);
		FileHandle staging = state.getFileHandle();
		assert staging instanceof StagingFile;
		fileSystemService.copy(fileHandle, originalFilename, staging, newFilename);
		ViewableItem vitem = viewableItemFactory.createPreviewItem(sessionId, state.getStagingId());
		FileAttachment fakeFile = new FileAttachment();
		fakeFile.setFilename(newFilename);
		return attachmentResourceService.getViewableResource(info, vitem, fakeFile);
	}

	@Override
	public FileInfo saveHtml(FileHandle handle, String filename, String html)
	{
		return saveHtml(handle, filename, new StringReader(html));
	}

	@Override
	public FileInfo saveHtml(FileHandle handle, String filename, Reader html)
	{
		try( Writer wrt = getWriter(handle, filename) )
		{
			CharStreams.copy(html, wrt);
		}
		catch( IOException ex )
		{
			throw Throwables.propagate(ex);
		}

		return fileSystemService.getFileInfo(handle, filename);
	}

	@Override
	public <T> T forFile(FileHandle handle, String filename, Function<Reader, T> withReader)
	{
		try( Reader reader = new InputStreamReader(fileSystemService.read(handle, filename), Constants.UTF8) )
		{
			return withReader.apply(reader);
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	protected Writer getWriter(FileHandle handle, String filename)
	{
		try
		{
			return new OutputStreamWriter(fileSystemService.getOutputStream(handle, filename, false), Constants.UTF8);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public ScriptContext createScriptContext(WizardStateInterface state)
	{
		return wizardService.createScriptContext(state.getItemPack(), null, null, null);
	}

	@BindFactory
	public interface MyPagesStateFactory
	{
		MyPagesState createState();
	}
}
