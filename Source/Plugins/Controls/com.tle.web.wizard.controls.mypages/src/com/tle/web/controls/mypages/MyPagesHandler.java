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

package com.tle.web.controls.mypages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.VersionSelection;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.services.FileSystemService;
import com.tle.mycontent.web.selection.MyContentSelectable;
import com.tle.mycontent.web.selection.MyContentSelectionSettings;
import com.tle.mypages.MyPagesConstants;
import com.tle.mypages.service.MyPagesService;
import com.tle.mypages.web.MyPagesPageFilter;
import com.tle.mypages.web.event.ChangePageEvent;
import com.tle.mypages.web.section.MyPagesContributeSection;
import com.tle.mypages.web.section.RootMyPagesSection;
import com.tle.mypages.workflow.operation.UnusedContentCleanup;
import com.tle.web.controls.universal.AbstractAttachmentHandler;
import com.tle.web.controls.universal.AttachmentHandlerLabel;
import com.tle.web.controls.universal.DialogRenderOptions;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.RegistrationController;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionNode;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.InfoEventListener;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.generic.DefaultSectionTree;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.generic.function.PassThroughFunction;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.selection.ParentFrameSelectionCallback;
import com.tle.web.selection.SelectedResourceDetails;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.template.Decorations;
import com.tle.web.wizard.WizardState;
import com.tle.web.wizard.impl.WebRepository;

import net.sf.json.JSONArray;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
public class MyPagesHandler extends AbstractAttachmentHandler<MyPagesHandler.MyPagesHandlerModel>
	implements
		InfoEventListener,
		MyPagesPageFilter
{
	@PlugKey("handlers.mypages.name")
	private static Label LABEL_NAME;
	@PlugKey("handlers.mypages.description")
	private static Label LABEL_DESCRIPTION;
	@PlugKey("handlers.mypages.title.add")
	private static Label LABEL_TITLE_ADD;
	@PlugKey("handlers.mypages.title.edit")
	private static Label LABEL_TITLE_EDIT;
	@PlugKey("handlers.mypages.button.importfromscrapbook")
	private static Label LABEL_BUTTON_IMPORTSCRAPBOOK;
	@PlugKey("handlers.mypages.warning.toomanypages")
	private static Label LABEL_WARN_TOO_MANY_PAGES;

	@Inject
	private ItemService itemService;
	@Inject
	private MyPagesService mypagesService;
	@Inject
	private SelectionService selectionService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private MyContentSelectable selectable;

	protected JSCallAndReference resultsCallback;

	private HtmlLinkState scrapbook;

	/*
	 * My Pages tree stuff
	 */
	@Inject
	private RegistrationController controller;
	@Inject
	@Named("myPagesTree")
	private SectionNode mypagesNode;

	private DefaultSectionTree mypagesTree;
	private RootMyPagesSection root;
	private MyPagesContributeSection contrib;
	private MyPagesHandlerPageActionsSection pages;
	private MyPagesExtrasSection extraOptions;

	@Override
	public String getHandlerId()
	{
		return "mypagesHandler";
	}

	@Override
	public SectionRenderable render(RenderContext context, DialogRenderOptions renderOptions)
	{
		final MyPagesHandlerModel model = getModel(context);

		if( model.isSelecting() )
		{
			return renderSelection(context, renderOptions);
		}

		boolean allowSave = true;
		boolean editing = dialogState.isEditing(context);
		boolean showOthers = !editing && !dialogState.isReplacing(context);

		if( !showOthers )
		{
			pages.setDisabled(context, true);
		}
		else
		{
			boolean add = true;
			final int pageAttachmentCount = getPageAttachments(context).size();
			if( isMultipleAllowed(context) )
			{
				allowSave = pageAttachmentCount > 0;
			}
			else
			{
				allowSave = pageAttachmentCount == 1;
				add = pageAttachmentCount == 0;
				if( pageAttachmentCount > 1 )
				{
					model.setWarnLabel(LABEL_WARN_TOO_MANY_PAGES);
				}
			}
			pages.setDisallowAdd(context, !add);
			if( add )
			{
				pages.setScrapbookCommand(context, scrapbook);
			}

		}
		renderOptions.setShowSave(allowSave);
		renderOptions.setShowAddReplace(allowSave && !editing);

		model.setMyPages(renderSection(context, root));
		return viewFactory.createResult("mypagesedit.ftl", this);

	}

	private SectionRenderable renderSelection(RenderContext context, DialogRenderOptions renderOptions)
	{
		Decorations.getDecorations(context).clearAllDecorations();
		renderOptions.setFullscreen(true);

		final SectionInfo forward = selectionService.getSelectionSessionForward(context, initSession(), selectable);

		final MyPagesHandlerModel model = getModel(context);
		model.setSelectionUrl(new InfoBookmark(forward).getHref());
		return viewFactory.createResult("mypagessession.ftl", this);
	}

	private SelectionSession initSession()
	{
		final SelectionSession session = new SelectionSession(new ParentFrameSelectionCallback(resultsCallback, false));

		final MyContentSelectionSettings settings = new MyContentSelectionSettings();
		settings.setRestrictToHandlerTypes(Arrays.asList(MyPagesConstants.MYPAGES_CONTENT_TYPE));
		session.setSelectScrapbook(true);
		session.setAttribute(MyContentSelectionSettings.class, settings);
		session.setSelectItem(true);
		session.setSelectAttachments(false);
		session.setSelectPackage(false);
		session.setSelectMultiple(isMultiple());
		session.setAddToRecentSelections(false);
		session.setOverrideVersionSelection(VersionSelection.FORCE_CURRENT);
		if( !isMultiple() )
		{
			session.setSkipCheckoutPage(true);
		}

		return session;
	}

	@Override
	public Label getTitleLabel(RenderContext context, boolean editing)
	{
		return editing ? LABEL_TITLE_EDIT : LABEL_TITLE_ADD;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		mypagesNode.setId(id + "m");
		mypagesTree = new DefaultSectionTree(controller, mypagesNode);
		mypagesTree.treeFinished();
		root = mypagesTree.lookupSection(RootMyPagesSection.class, null);
		contrib = mypagesTree.lookupSection(MyPagesContributeSection.class, null);
		pages = mypagesTree.lookupSection(MyPagesHandlerPageActionsSection.class, null);
		extraOptions = mypagesTree.lookupSection(MyPagesExtrasSection.class, null);

		scrapbook = new HtmlLinkState(LABEL_BUTTON_IMPORTSCRAPBOOK, events.getNamedHandler("importFromScrapbook"));
		resultsCallback = new PassThroughFunction("r" + id, events.getSubmitValuesFunction("selectionsMade"));
	}

	@SuppressWarnings("unchecked")
	@EventHandlerMethod
	public void selectionsMade(SectionInfo info, String selections)
	{
		final List<SelectedResourceDetails> selectedResources = new ArrayList<SelectedResourceDetails>(
			JSONArray.toCollection(JSONArray.fromObject(selections), SelectedResourceDetails.class));
		final WebRepository repo = dialogState.getRepository();
		final WizardState state = repo.getState();
		final MyPagesHandlerModel model = getModel(info);
		for( SelectedResourceDetails resource : selectedResources )
		{
			final ItemId key = new ItemId(resource.getUuid(), resource.getVersion());

			// clone the HtmlAttachments AND the file system only attachments
			// folder
			final Item item = itemService.get(key);
			final List<HtmlAttachment> pgs = new UnmodifiableAttachments(item).getList(AttachmentType.HTML);
			for( HtmlAttachment oldPage : pgs )
			{
				final HtmlAttachment newPage = mypagesService.clonePage(state, item, oldPage, true);
				if( Check.isEmpty(contrib.getPageUuid(info)) )
				{
					ChangePageEvent changePage = new ChangePageEvent(null, newPage.getUuid(), state.getWizid());
					info.processEvent(changePage, mypagesTree);
				}
				repo.getAttachments().addAttachment(newPage);
				// dialog.addAttachment(info, newPage);
			}
		}
		model.setSelecting(false);
	}

	@Override
	public AttachmentHandlerLabel getLabel()
	{
		return new AttachmentHandlerLabel(LABEL_NAME, LABEL_DESCRIPTION);
	}

	@Override
	public boolean supports(IAttachment attachment)
	{
		return attachment instanceof HtmlAttachment;
	}

	@EventHandlerMethod
	public void importFromScrapbook(SectionInfo info)
	{
		MyPagesHandlerModel model = getModel(info);
		model.setSelecting(true);
	}

	private void ensureCleanupOperation()
	{
		WizardState state = dialogState.getRepository().getState();
		UnusedContentCleanup cleanup = (UnusedContentCleanup) state.getWizardSaveOperation(UnusedContentCleanup.ID);
		if( cleanup == null )
		{
			state.setWizardSaveOperation(UnusedContentCleanup.ID, new UnusedContentCleanup());
		}
		state.setWizardSaveOperation("", new ChangePreviewUrls(state));
	}

	@Override
	public void cancelled(SectionInfo info)
	{
		contrib.setPageUuid(info, null);
		mypagesService.clearDraft(info, dialogState.getRepository().getWizid());
	}

	private List<HtmlAttachment> getPageAttachments(SectionInfo info)
	{
		return filterPages(
			mypagesService.getNonDeletedPageAttachments(info, dialogState.getRepository().getWizid(), null));
	}

	@Override
	public List<HtmlAttachment> filterPages(List<HtmlAttachment> pageAttachments)
	{
		final List<HtmlAttachment> filtered = new ArrayList<HtmlAttachment>();
		final Collection<Attachment> controlAttachments = dialogState.getAttachments();
		for( HtmlAttachment page : pageAttachments )
		{
			if( controlAttachments.contains(page) || page.isNew() || page.isDraft() )
			{
				filtered.add(page);
			}
		}
		return filtered;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new MyPagesHandlerModel();
	}

	public JSCallAndReference getResultsCallback()
	{
		return resultsCallback;
	}

	@Override
	public void handleInfoEvent(MutableSectionInfo info, boolean removed, boolean processParameters)
	{
		if( !removed )
		{
			info.getAttributeForClass(MutableSectionInfo.class).addTreeToBottom(mypagesTree, processParameters);
			contrib.setSessionId(info, dialogState.getRepository().getWizid());
			pages.setPageFilter(info, this);
			// FIXME: always false until fixed, see bug #7668
			extraOptions.setShowPreview(info, false);
			extraOptions.setShowRestrict(info, canRestrictAttachments());
		}
	}

	@Override
	public void createNew(SectionInfo info)
	{
		if( dialogState.isReplacing(info) )
		{
			pages.createPage(info);
		}
	}

	@Override
	public void loadForEdit(SectionInfo info, Attachment attachment)
	{
		contrib.setPageUuid(info, attachment.getUuid());
	}

	@Override
	public void saveEdited(SectionInfo info, Attachment attachment)
	{
		contrib.saveCurrentEdits(info);
		String sessionId = dialogState.getRepository().getWizid();
		mypagesService.commitDraft(info, sessionId);
		ensureCleanupOperation();
	}

	@Override
	public void remove(SectionInfo info, Attachment attachment, boolean willBeReplaced)
	{
		String sessionId = dialogState.getRepository().getWizid();
		dialogState.removeAttachment(info, attachment);
		mypagesService.deletePageFiles(info, sessionId, (HtmlAttachment) attachment);
		if( !willBeReplaced )
		{
			dialogState.removeMetadataUuid(info, attachment.getUuid());
		}
	}

	@Override
	public boolean validate(SectionInfo info)
	{
		return true;
	}

	@Override
	public void saveChanges(SectionInfo info, @Nullable String replacementUuid)
	{
		String sessionId = dialogState.getRepository().getWizid();
		contrib.saveCurrentEdits(info);

		List<HtmlAttachment> pageAttachments = getPageAttachments(info);
		for( HtmlAttachment htmlAttachment : pageAttachments )
		{
			if( htmlAttachment.isNew() )
			{
				if( replacementUuid != null )
				{
					final HtmlAttachment fakeDest = new HtmlAttachment();
					fakeDest.setUuid(replacementUuid);
					fakeDest.setParentFolder(htmlAttachment.getParentFolder());
					fakeDest.setDraft(htmlAttachment.isDraft());
					fileSystemService.move(new StagingFile(dialogState.getRepository().getStagingid()),
						htmlAttachment.getFolder(), fakeDest.getFolder());
					htmlAttachment.setUuid(replacementUuid);
					replacementUuid = null;
				}
				else
				{
					dialogState.addMetadataUuid(info, htmlAttachment.getUuid());
				}
			}
		}
		mypagesService.commitDraft(info, sessionId);
		ensureCleanupOperation();
	}

	public static class MyPagesHandlerModel
	{
		@Bookmarked(name = "s")
		private boolean selecting;
		private String selectionUrl;
		private SectionRenderable myPages;
		private Label warnLabel;

		public boolean isSelecting()
		{
			return selecting;
		}

		public void setSelecting(boolean selecting)
		{
			this.selecting = selecting;
		}

		public String getSelectionUrl()
		{
			return selectionUrl;
		}

		public void setSelectionUrl(String selectionUrl)
		{
			this.selectionUrl = selectionUrl;
		}

		public Label getWarnLabel()
		{
			return warnLabel;
		}

		public void setWarnLabel(Label warnLabel)
		{
			this.warnLabel = warnLabel;
		}

		public SectionRenderable getMyPages()
		{
			return myPages;
		}

		public void setMyPages(SectionRenderable myPages)
		{
			this.myPages = myPages;
		}
	}
}
