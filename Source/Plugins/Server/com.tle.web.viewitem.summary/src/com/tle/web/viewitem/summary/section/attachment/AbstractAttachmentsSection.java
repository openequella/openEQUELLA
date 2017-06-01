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

package com.tle.web.viewitem.summary.section.attachment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import com.dytech.edge.exceptions.AttachmentNotFoundException;
import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.beans.item.attachments.ZipAttachment;
import com.tle.common.Check;
import com.tle.common.security.SecurityConstants;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.item.ItemService;
import com.tle.core.workflow.operations.WorkflowFactory;
import com.tle.core.workflow.operations.WorkflowOperation;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.jquery.libraries.JQuerySortable;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.IconLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.selection.SelectAttachmentHandler;
import com.tle.web.selection.SelectedResourceKey;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.event.AllAttachmentsSelectorEvent;
import com.tle.web.selection.event.AllAttachmentsSelectorEventListener;
import com.tle.web.selection.event.AttachmentSelectorEvent;
import com.tle.web.selection.event.AttachmentSelectorEventListener;
import com.tle.web.selection.event.PackageSelectorEvent;
import com.tle.web.selection.event.PackageSelectorEventListener;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.AttachmentViewFilter;
import com.tle.web.viewitem.summary.attachment.service.ViewAttachmentWebService;
import com.tle.web.viewitem.summary.attachment.service.ViewAttachmentWebService.AttachmentRowDisplay;

@NonNullByDefault
@SuppressWarnings("nls")
public abstract class AbstractAttachmentsSection<I extends IItem<?>, M extends AbstractAttachmentsSection.AttachmentsModel>
	extends
		AbstractPrototypeSection<AbstractAttachmentsSection.AttachmentsModel>
	implements
		HtmlRenderer,
		AttachmentSelectorEventListener,
		AllAttachmentsSelectorEventListener,
		PackageSelectorEventListener
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(AbstractAttachmentsSection.class);
	private static final IncludeFile INCLUDE = new IncludeFile(resources.url("scripts/attachments/attachments.js"),
		JQuerySortable.PRERENDER);
	private static final JSCallAndReference ATTACHMENTS_CLASS = new ExternallyDefinedFunction("Attachments", INCLUDE);
	private static final JSCallable SELECT_PACKAGE_FUNCTION = new ExternallyDefinedFunction(ATTACHMENTS_CLASS,
		"selectPackage", 3);
	private static final JSCallable SELECT_ALL_ATTACHMENTS_FUN = new ExternallyDefinedFunction(ATTACHMENTS_CLASS,
		"setupSelectAllButton", 5);
	private static final JSCallable SETUP_REORDER = new ExternallyDefinedFunction(ATTACHMENTS_CLASS, "setupReorder", 6);

	protected boolean showFull = true;
	protected boolean showFullNewWindow = false;
	protected boolean showStructuredView = true;

	@Inject
	private ViewAttachmentWebService viewAttachmentWebService;
	@Inject
	private SelectionService selectionService;
	@Inject
	private ItemService itemService;
	@Inject
	private TLEAclManager tleACLManager;
	@Inject
	private WorkflowFactory workflowFactory;

	@PlugKey("summary.content.attachments.modal.warning")
	public static Label MODAL_CLOSE_WARNING_LABEL;
	@PlugKey("summary.content.attachments.modal.button.save")
	public static Label MODAL_SAVE_LABEL;
	@PlugKey("summary.content.attachments.modal.button.cancel")
	public static Label MODAL_CANCEL_LABEL;

	@Component
	private Div div;
	@PlugKey("summary.content.attachments.attachment.button.selectattachment.all")
	@Component
	private Button selectAllAttachmentButton;
	@PlugKey("summary.content.attachments.attachment.button.selectpackage")
	@Component
	private Button selectPackageButton;
	@PlugKey("summary.content.attachments.link.fullscreen")
	@Component
	private Link fullScreenLink;
	@PlugKey("summary.content.attachments.link.fullscreen.newwindow")
	@Component
	private Link fullScreenLinkNewWindow;
	@PlugKey("summary.content.attachments.link.reorder")
	@Component
	private Link reorderAttachments;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	protected FreemarkerFactory viewFactory;
	@AjaxFactory
	private AjaxGenerator ajax;

	protected abstract boolean showFullscreen(SectionInfo info, I item, List<AttachmentRowDisplay> rows);

	@Nullable
	protected abstract Bookmark getFullscreenBookmark(SectionInfo info, ViewableItem<I> vitem);

	protected abstract ViewableItem<I> getViewableItem(SectionInfo info);

	protected abstract Label getTitle(SectionInfo info, ViewableItem<I> vitem);

	@Nullable
	protected abstract AttachmentViewFilter getCustomFilter(SectionInfo info, ViewableItem<I> vitem);

	@Nullable
	protected abstract String getItemExtensionType();

	protected abstract String getAttchmentControlId();

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		tree.addListener(null, AttachmentSelectorEventListener.class, this);
		tree.addListener(null, AllAttachmentsSelectorEventListener.class, this);
		tree.addListener(null, PackageSelectorEventListener.class, this);
		selectPackageButton.setStyleClass("package-select button-expandable");
		selectAllAttachmentButton.setStyleClass("package-select button-expandable");
	}

	@Nullable
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final AttachmentsModel model = getModel(context);
		final ViewableItem<I> viewableItem = getViewableItem(context);
		final I item = viewableItem.getItem();
		final ItemKey itemId = item.getItemId();

		boolean renderSelect = selectionService.getCurrentSession(context) != null
			&& !selectionService.getCurrentSession(context).getStructure().isNoTargets();

		final List<AttachmentRowDisplay> attachmentDisplays = viewAttachmentWebService.createViewsForItem(context,
			viewableItem, div, renderSelect, true, !showStructuredView);
		viewAttachmentWebService.filterAttachmentDisplays(context, attachmentDisplays,
			getCustomFilter(context, viewableItem));
		removeEmptyFolder(attachmentDisplays);

		if( attachmentDisplays.isEmpty() )
		{
			return null;
		}

		div.addReadyStatements(context,
			viewAttachmentWebService.createShowDetailsFunction(itemId, ".attachments-browse"));

		final String itemExtensionType = getItemExtensionType();
		final JSCallable selectFunction = selectionService.getSelectAttachmentFunction(context, viewableItem);
		if( selectFunction != null )
		{
			div.getState(context).addClass("selectable");
			div.addReadyStatements(
				context,
				viewAttachmentWebService.setupSelectButtonsFunction(selectFunction,
					new ItemId(itemId.getUuid(), itemId.getVersion()), itemExtensionType, ".attachments-browse"));
		}

		final JSCallable selectPackageFunction = getSelectPackageFunction(context, viewableItem);
		if( selectPackageFunction != null && renderSelect )
		{
			selectPackageButton.setClickHandler(context,
				new OverrideHandler(SELECT_PACKAGE_FUNCTION, Jq.$(selectPackageButton), selectPackageFunction, itemId,
					getAttchmentControlId()));
		}
		else
		{
			selectPackageButton.setDisplayed(context, false);
		}

		final JSCallable selectAllAttachmentsFunction = getSelectAllAttachmentsFunction(context, viewableItem);
		if( showSelectAllButton(context, attachmentDisplays) && selectAllAttachmentsFunction != null )
		{
			model.setShowSelectAllButton(true);
			List<String> allAttachmentUuids = getAllAttachmentUuids(attachmentDisplays);
			selectAllAttachmentButton
				.setClickHandler(context, new OverrideHandler(SELECT_ALL_ATTACHMENTS_FUN, selectAllAttachmentsFunction,
					allAttachmentUuids, itemId, itemExtensionType, div.getElementId(context)));
		}
		else
		{
			selectAllAttachmentButton.setDisplayed(context, false);
		}

		model.setAttachmentRows(attachmentDisplays);

		// only render the fullscreen link if enabled attachments > 0
		final boolean showFullscreen = showFullscreen(context, item, attachmentDisplays);
		fullScreenLink.setDisplayed(context, showFull && showFullscreen);
		fullScreenLinkNewWindow.setDisplayed(context, showFullNewWindow && showFullscreen);
		if( showFullscreen && (showFull || showFullNewWindow) )
		{
			final Bookmark fullscreenBookmark = getFullscreenBookmark(context, viewableItem);
			fullScreenLink.setBookmark(context, fullscreenBookmark);
			fullScreenLink.setLabel(context, new IconLabel(Icon.FULLSCREEN, fullScreenLink.getLabel(context), false));
			fullScreenLinkNewWindow.setLabel(context,
				new IconLabel(Icon.FULLSCREEN, fullScreenLinkNewWindow.getLabel(context), false));
			fullScreenLinkNewWindow.setBookmark(context, fullscreenBookmark);
			fullScreenLinkNewWindow.getState(context).setTarget("_blank");
		}
		if( !tleACLManager.filterNonGrantedPrivileges(item, Collections.singleton(SecurityConstants.EDIT_ITEM))
			.isEmpty()
			&& selectionService.getCurrentSession(context) == null
			&& attachmentStructureReorderable(attachmentDisplays, item) )
		{
			reorderAttachments.setClickHandler(
				context,
				new OverrideHandler(SETUP_REORDER, div.getElementId(context), ajax
					.getAjaxFunction("attachmentsReordered"), MODAL_CLOSE_WARNING_LABEL.getText(), MODAL_SAVE_LABEL
					.getText(), MODAL_CANCEL_LABEL.getText(), isShowStructuredView()));
			reorderAttachments.setLabel(context, new IconLabel(Icon.MOVE, reorderAttachments.getLabel(context), false));
		}
		else
		{
			reorderAttachments.setDisplayed(context, false);
		}

		model.setSectionTitle(getTitle(context, viewableItem));
		return viewFactory.createResult("viewitem/attachments/attachments.ftl", context);
	}

	private void removeEmptyFolder(List<AttachmentRowDisplay> attachmentDisplays)
	{
		if( attachmentDisplays.size() > 0 )
		{
			AttachmentRowDisplay firstRow = attachmentDisplays.get(0);
			String rowType = firstRow.getRow().getData("rowType");

			if( rowType != null && rowType.equals("folder") )
			{
				if( attachmentDisplays.size() == 1 )
				{
					attachmentDisplays.remove(0);
				}
				else
				{
					AttachmentRowDisplay secondRow = attachmentDisplays.get(1);
					if( firstRow.getLevel() == secondRow.getLevel() )
					{
						attachmentDisplays.remove(0);
					}
				}
			}
		}
	}

	private boolean attachmentStructureReorderable(List<AttachmentRowDisplay> attachmentDisplays, I iitem)
	{
		return attachmentDisplays.size() > 1 && isFlatHierachy(attachmentDisplays)
			&& (iitem.getTreeNodes() == null || iitem.getTreeNodes().isEmpty()) && !containsHiddenZip(iitem);
	}

	private boolean containsHiddenZip(I item)
	{
		for( IAttachment att : item.getAttachments() )
		{
			if( att.getAttachmentType() == AttachmentType.ZIP )
			{
				ZipAttachment zip = (ZipAttachment) att;
				if( !zip.isAttachZip() )
				{
					return true;
				}
			}
		}
		return false;
	}

	private boolean isFlatHierachy(List<AttachmentRowDisplay> attachmentDisplays)
	{
		for( AttachmentRowDisplay row : attachmentDisplays )
		{
			if( row.getLevel() > 0 )
			{
				return false;
			}
		}
		return true;
	}

	private List<String> getAllAttachmentUuids(List<AttachmentRowDisplay> rows)
	{
		List<String> uuids = new ArrayList<>();

		for( AttachmentRowDisplay attachmentRow : rows )
		{
			if( attachmentRow.getAttachmentView() != null )
			{
				uuids.add(attachmentRow.getAttachmentView().getAttachment().getUuid());
			}
		}
		return uuids;
	}

	@EventHandlerMethod
	public void selectAttachment(SectionInfo info, String uuid, ItemId itemId, String extensionType)
	{
		final ViewableItem<I> vitem = getViewableItem(info);
		final SelectedResourceKey key = new SelectedResourceKey(vitem.getItemId(), uuid, extensionType);

		if( selectionService.getCurrentSession(info).containsResource(key, false) )
		{
			selectionService.removeSelectedResource(info, key);
		}
		else
		{
			final IAttachment attachment = vitem.getAttachmentByUuid(uuid);
			final SelectAttachmentHandler selectAttachmentHandler = selectionService.getSelectAttachmentHandler(info,
				vitem, null);
			if( selectAttachmentHandler != null )
			{
				selectAttachmentHandler.handleAttachmentSelection(info, itemId, attachment, extensionType);
			}
		}
	}

	@AjaxMethod
	public void attachmentsReordered(SectionInfo info, List<String> newOrder)
	{
		Item item = (Item) getViewableItem(info).getItem();
		List<Attachment> attachments = item.getAttachments();
		Map<String, Attachment> attachmentMap = UnmodifiableAttachments.convertToMapUuid(item);
		Map<Integer, Attachment> movedItems = Maps.newHashMap();

		for( int x = 0; x < newOrder.size(); x++ )
		{
			Attachment currAttach = attachments.get(x);
			String reorderdedUuid = newOrder.get(x);
			// sanity check
			if( attachmentMap.get(reorderdedUuid) == null )
			{
				throw new AttachmentNotFoundException(item.getItemId(), " with uuid: " + reorderdedUuid);
			}

			if( !newOrder.contains(currAttach.getUuid()) )
			{
				newOrder.add(x, "nothing");// move elements up
				continue;
			}
			if( !currAttach.getUuid().equals(reorderdedUuid) )
			{
				movedItems.put(x, attachmentMap.get(reorderdedUuid));
			}
		}

		if( !movedItems.isEmpty() )
		{
			for( Entry<Integer, Attachment> entries : movedItems.entrySet() )
			{
				attachments.set(entries.getKey(), entries.getValue());
			}
			ItemIdKey itemKey = new ItemIdKey(item);
			itemService.operation(
				itemKey,
				new WorkflowOperation[]{workflowFactory.editMetadata(itemService.getItemPack(itemKey)),
						workflowFactory.saveNoSaveScript(true)});
		}

	}

	@EventHandlerMethod
	public void selectAllAttachments(SectionInfo info, List<String> uuids, ItemId itemId, String extensionType)
	{
		for( String uuid : uuids )
		{
			final ViewableItem<I> vitem = getViewableItem(info);
			final SelectedResourceKey key = new SelectedResourceKey(vitem.getItemId(), uuid, extensionType);
			if( !selectionService.getCurrentSession(info).containsResource(key, false) )
			{
				final IAttachment attachment = vitem.getAttachmentByUuid(uuid);
				final SelectAttachmentHandler selectAttachmentHandler = selectionService.getSelectAttachmentHandler(
					info, vitem, null);
				if( selectAttachmentHandler != null )
				{
					selectAttachmentHandler.handleAttachmentSelection(info, itemId, attachment, extensionType);
				}
			}
		}
	}

	// TODO: Doesn't really apply for cloud items...
	@EventHandlerMethod
	public void selectPackage(SectionInfo info, ItemId itemId, String extensionType, String attachmentControlId)
	{
		final ViewableItem<I> vitem = getViewableItem(info);
		final I item = vitem.getItem();

		final ImsAttachment attachment = new UnmodifiableAttachments(item).getIms();
		if( attachment == null )
		{
			if( !Check.isEmpty(item.getTreeNodes()) )
			{
				selectionService.addSelectedPath(info, item, "treenav.jsp", null, extensionType);
			}
			else
			{
				String resourcePath = "viewcontent/" + attachmentControlId;
				selectionService.addSelectedPath(info, item, resourcePath, null, extensionType);
			}
		}
		else
		{
			selectionService.addSelectedResource(info,
				selectionService.createAttachmentSelection(info, item.getItemId(), attachment, null, extensionType),
				true);
		}
	}

	@Override
	public void handleAttachmentSelection(SectionInfo info, ItemId itemId, IAttachment attachment, String extensionType)
	{
		selectionService.addSelectedResource(info,
			selectionService.createAttachmentSelection(info, itemId, attachment, null, extensionType), true);
	}

	@Override
	public void supplyFunction(SectionInfo info, PackageSelectorEvent event)
	{
		event.setFunction(events.getSubmitValuesFunction("selectPackage"));
	}

	@Override
	public void supplyFunction(SectionInfo info, AttachmentSelectorEvent event)
	{
		event.setFunction(events.getSubmitValuesFunction("selectAttachment"));
	}

	@Override
	public void supplyFunction(SectionInfo info, AllAttachmentsSelectorEvent event)
	{
		event.setFunction(events.getSubmitValuesFunction("selectAllAttachments"));
	}

	private boolean showSelectAllButton(SectionInfo info, List<AttachmentRowDisplay> rows)
	{
		SelectionSession currentSession = selectionService.getCurrentSession(info);
		if( currentSession == null || !currentSession.isSelectMultiple() || currentSession.getStructure().isNoTargets() )
		{
			return false;
		}

		int i = 0;
		for( AttachmentRowDisplay attachmentRowDisplay : rows )
		{
			if( attachmentRowDisplay.getAttachmentView() != null )
			{
				i++;
				if( i > 1 )
				{
					return true;
				}
			}
		}
		return false;
	}

	@Nullable
	protected JSCallable getSelectAllAttachmentsFunction(SectionInfo info, ViewableItem<I> vitem)
	{
		return selectionService.getSelectAllAttachmentsFunction(info, vitem);
	}

	@Nullable
	protected JSCallable getSelectPackageFunction(SectionInfo info, ViewableItem<I> vitem)
	{
		return selectionService.getSelectPackageFunction(info, vitem);
	}

	public Button getSelectPackageButton()
	{
		return selectPackageButton;
	}

	public Button getSelectAllAttachmentButton()
	{
		return selectAllAttachmentButton;
	}

	public Link getFullScreenLink()
	{
		return fullScreenLink;
	}

	public Link getFullScreenLinkNewWindow()
	{
		return fullScreenLinkNewWindow;
	}

	public Div getDiv()
	{
		return div;
	}

	public Link getReorderAttachments()
	{
		return reorderAttachments;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "attachments";
	}

	@Override
	public Class<AttachmentsModel> getModelClass()
	{
		return AttachmentsModel.class;
	}

	public boolean isShowStructuredView()
	{
		return showStructuredView;
	}

	public static class AttachmentsModel
	{
		private List<AttachmentRowDisplay> attachmentRows;
		private boolean showSelectAllButton;
		private Label sectionTitle;

		public List<AttachmentRowDisplay> getAttachmentRows()
		{
			return attachmentRows;
		}

		public void setAttachmentRows(List<AttachmentRowDisplay> attachmentRows)
		{
			this.attachmentRows = attachmentRows;
		}

		public boolean isShowSelectAllButton()
		{
			return showSelectAllButton;
		}

		public void setShowSelectAllButton(boolean showSelectAllButton)
		{
			this.showSelectAllButton = showSelectAllButton;
		}

		public Label getSectionTitle()
		{
			return sectionTitle;
		}

		public void setSectionTitle(Label sectionTitle)
		{
			this.sectionTitle = sectionTitle;
		}
	}
}