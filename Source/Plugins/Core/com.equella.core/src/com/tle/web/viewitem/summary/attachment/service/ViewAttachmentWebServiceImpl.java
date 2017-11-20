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

package com.tle.web.viewitem.summary.attachment.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.exceptions.ItemNotFoundException;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.beans.item.attachments.ItemNavigationTree;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.Pair;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.sections.standard.renderers.LinkTagRenderer;
import com.tle.web.sections.standard.renderers.SpanRenderer;
import com.tle.web.selection.SelectedResourceKey;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.filter.SelectionFilter;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.AttachmentViewFilter;
import com.tle.web.viewitem.attachments.AttachmentView;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewItemService;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentNode;
import com.tle.web.viewurl.attachments.AttachmentResourceService;
import com.tle.web.viewurl.attachments.AttachmentTreeService;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind(ViewAttachmentWebService.class)
@Singleton
public class ViewAttachmentWebServiceImpl implements ViewAttachmentWebService
{
	private static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(ViewAttachmentWebService.class);

	private static final CssInclude ATTACHMENTS_CSS = CssInclude
		.include(RESOURCES.url("css/attachments/attachments.css")).hasRtl().make();
	private static final IncludeFile INCLUDE = new IncludeFile(RESOURCES.url("scripts/attachments/attachments.js"));
	private static final JSCallAndReference ATTACHMENTS_CLASS = new ExternallyDefinedFunction("Attachments", INCLUDE);
	private static final JSCallable SETUP_ROWS = new ExternallyDefinedFunction(ATTACHMENTS_CLASS, "setupAttachmentRows",
		2);
	private static final JSCallable SETUP_SELECTS = new ExternallyDefinedFunction(ATTACHMENTS_CLASS,
		"setupSelectButtons", 3);

	private static final Label SELECT_LABEL = new KeyLabel(
		RESOURCES.key("summary.content.attachments.attachment.button.selectattachment"));
	private static final Label UNSELECT_LABEL = new KeyLabel(
		RESOURCES.key("summary.content.attachments.attachment.button.unselectattachment"));

	private static final Label SHOW_DETAILS_LABEL = new KeyLabel(
		RESOURCES.key("summary.content.attachments.alt.droparrow"));

	@Inject
	private AttachmentTreeService attachmentTreeService;
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private SelectionService selectionService;
	@Inject
	private ViewItemService viewItemService;
	@Inject
	private ViewAttachmentsFreemarkerFactory viewFactory;
	@Inject
	private PluginTracker<AttachmentViewFilter> attachmentFilters;

	@Override
	public List<AttachmentRowDisplay> createViewsForItem(SectionInfo info, ViewableItem viewableItem,
		ElementId baseElement, boolean renderSelection, boolean details, boolean thumbnail, boolean filtered)
	{
		final List<AttachmentRowDisplay> attachmentDisplays = new ArrayList<AttachmentRowDisplay>();
		final IItem item = viewableItem.getItem();
		final List<ItemNavigationNode> treeNodes = item.getTreeNodes();

		final BuildTreeData btdata = new BuildTreeData();
		if( !Check.isEmpty(treeNodes) && !thumbnail && !filtered)
		{
			final ItemNavigationTree tree = new ItemNavigationTree(treeNodes);

			buildTree(info, viewableItem, baseElement, tree, tree.getRootNodes(), attachmentDisplays, 0, btdata,
				renderSelection, details, thumbnail);
		}

		// attachments with no tree nodes (only if not manually modified
		// navigation)
		if( filtered || !item.getNavigationSettings().isManualNavigation() )
		{
			List<IAttachment> unreferenced = Lists.newArrayList();
			List<IAttachment> attachments = item.getAttachments();
			for( IAttachment attachment : attachments )
			{
				if( !btdata.nodedAttachments.contains(attachment) )
				{
					unreferenced.add(attachment);
				}
			}
			List<AttachmentNode> rootNodes = attachmentTreeService.getTreeStructure(unreferenced, true);
			addRowsForNodes(info, baseElement, renderSelection, attachmentDisplays, rootNodes, viewableItem,
				btdata.attachmentIndex - attachmentDisplays.size(), 0, details, thumbnail);
		}

		return attachmentDisplays;
	}

	private void addRowsForNodes(SectionInfo info, ElementId baseElement, boolean renderSelection,
		List<AttachmentRowDisplay> attachmentDisplays, List<AttachmentNode> rootNodes, ViewableItem viewableItem,
		int offset, int level, boolean details, boolean thumbnail)
	{
		for( AttachmentNode attachmentNode : rootNodes )
		{
			IAttachment attachment = attachmentNode.getAttachment();

			final ViewableResource viewableResource = attachmentResourceService.getViewableResource(info, viewableItem,
				attachment);
			if( !viewableResource.getBooleanAttribute(ViewableResource.KEY_HIDDEN) )
			{
				final AttachmentView attachmentView = new AttachmentView(attachment, viewableResource);
				final AttachmentRowDisplay display = new AttachmentRowDisplay();
				display.setAttachmentView(attachmentView);
				// attachment.getDescription is in fact the title
				display.setRow(makeAttachmentRowRenderer(info, viewableItem, baseElement, attachmentView, level,
					attachment.getDescription(), false, attachmentDisplays.size() + offset, renderSelection, details,
					thumbnail));
				display.setLevel(level);

				attachmentDisplays.add(display);
			}
			addRowsForNodes(info, baseElement, renderSelection, attachmentDisplays, attachmentNode.getChildren(),
				viewableItem, offset, level + 1, details, thumbnail);
		}
	}

	@Override
	public void filterAttachmentDisplays(SectionInfo info, List<AttachmentRowDisplay> attachmentDisplays)
	{
		filterAttachmentDisplays(info, attachmentDisplays, null);
	}

	// TODO: roll into getViewsForItem??
	@Override
	public void filterAttachmentDisplays(SectionInfo info, List<AttachmentRowDisplay> attachmentDisplays,
		@Nullable AttachmentViewFilter customFilter)
	{
		List<AttachmentViewFilter> filters = getAttachmentFilters(info);
		if( customFilter != null )
		{
			filters = new ArrayList<>(getAttachmentFilters(info));
			filters.add(customFilter);
		}

		final Iterator<AttachmentRowDisplay> iter = attachmentDisplays.iterator();
		while( iter.hasNext() && !Check.isEmpty(filters) )
		{
			final AttachmentRowDisplay attachmentDisplay = iter.next();
			final AttachmentView view = attachmentDisplay.getAttachmentView();
			if( view != null )
			{
				boolean remove = false;
				for( AttachmentViewFilter filter : filters )
				{
					try
					{
						if( !filter.shouldBeDisplayed(info, view) )
						{
							remove = true;
							break;
						}
					}
					catch( ItemNotFoundException infe )
					{
						//ignore
					}
				}
				if( remove )
				{
					iter.remove();
				}
			}
		}
	}

	@Override
	public SectionRenderable makeAttachmentDetailsRenderer(SectionInfo info, ElementId baseElement, String uuid,
		int index, @Nullable SectionRenderable thumbnail, @Nullable List<AttachmentDetail> details,
		@Nullable List<LinkTagRenderer> alternateViewerLinks)
	{
		final AttachmentDetailsModel model = new AttachmentDetailsModel();
		model.setId(new AppendedElementId(baseElement, "detail_" + uuid).getElementId(info));

		if( details != null )
		{
			model.getDetails().addAll(details);
		}
		model.setThumbnail(thumbnail);
		if( alternateViewerLinks != null )
		{
			model.setViewerLinks(alternateViewerLinks);
		}
		else
		{
			model.setViewerLinks(Lists.<LinkTagRenderer>newArrayList());
		}

		return viewFactory.createResultWithModel("viewitem/attachments/details.ftl", model);
	}

	private String getViewer(AttachmentView view, final ViewableResource resource)
	{
		String viewer = view.getOverrideViewer();
		if( Check.isEmpty(viewer) )
		{
			viewer = view.getAttachment().getViewer();
			if( Check.isEmpty(viewer) )
			{
				viewer = resource.getDefaultViewer();
				if( Check.isEmpty(viewer) )
				{
					viewer = viewItemService.getDefaultViewerId(resource);
				}
			}
		}
		return viewer;
	}

	@Override
	public JSStatements createShowDetailsFunction(ItemKey itemId, String attachmentsJquerySelector)
	{
		return Js.call_s(SETUP_ROWS, itemId, attachmentsJquerySelector);
	}

	@Override
	public JSStatements setupSelectButtonsFunction(JSCallable selectAttachmentFunction, ItemKey itemId,
		String itemExtensionType, String attachmentsJquerySelector)
	{
		final FunctionCallStatement readyStatement = Js.call_s(SETUP_SELECTS, selectAttachmentFunction, itemId,
			itemExtensionType, attachmentsJquerySelector);
		return readyStatement;
	}

	@Nullable
	private SectionRenderable createThumbRenderer(SectionInfo info, ElementId baseElement, ViewableResource resource,
		String defaultViewerId, int index, IAttachment attachment)
	{
		final Label description = new TextLabel(attachment.getDescription());
		boolean customThumb = resource.isCustomThumb();
		final ImageRenderer thumbImage = resource.createStandardThumbnailRenderer(description);

		if( thumbImage == null )
		{
			return null;
		}

		if( customThumb )
		{
			thumbImage.addClasses("addBorder");
		}
		else
		{
			thumbImage.addClasses("noBorder");
		}

		final LinkTagRenderer thumbRenderer = viewItemService.getViewableLink(info, resource, defaultViewerId);
		thumbRenderer.getLinkState().setElementId(new AppendedElementId(baseElement, "attThmb" + index));
		thumbRenderer.setTitle(description);
		thumbRenderer.setLabel(description);
		if( !resource.isDisabled() )
		{
			thumbRenderer.ensureClickable();
		}

		thumbRenderer.setNestedRenderable(thumbImage);
		thumbImage.setAlt(description);
		return new DivRenderer(thumbRenderer);
	}

	/**
	 * Callers expected to cope with tri-state boolean logic, hence the name.
	 * 
	 * @param info
	 * @param itemInfo
	 * @param view
	 * @param renderSelection
	 * @return
	 */
	@Nullable
	private Boolean nullOrIsSelected(SectionInfo info, ViewableItem viewableItem, AttachmentView view,
		boolean renderSelection)
	{
		final ItemKey itemId = viewableItem.getItemId();
		final String attachmentUuid = view.getAttachment().getUuid();
		if( !renderSelection || !selectionService.canSelectAttachment(info, viewableItem, attachmentUuid) )
		{
			return null; // NOSONAR
		}

		final SelectionSession session = selectionService.getCurrentSession(info);
		final SelectionFilter selectionFilter = (SelectionFilter) session.getAttribute(SelectionFilter.class);
		if( selectionFilter != null )
		{
			final Collection<String> allowedMimeTypes = selectionFilter.getAllowedMimeTypes();
			if( allowedMimeTypes != null && !allowedMimeTypes.contains(view.getViewableResource().getMimeType()) )
			{
				return null; // NOSONAR
			}
		}

		return session.containsResource(
			new SelectedResourceKey(itemId, attachmentUuid, viewableItem.getItemExtensionType()), false);
	}

	/**
	 * @param info
	 * @param itemInfo
	 * @param viewableItem
	 * @param tree
	 * @param nodes
	 * @param attachmentDisplays
	 * @param level
	 * @param btdata An in/out parameter
	 */
	private void buildTree(SectionInfo info, ViewableItem viewableItem, ElementId baseElement, ItemNavigationTree tree,
		List<ItemNavigationNode> nodes, List<AttachmentRowDisplay> attachmentDisplays, int level, BuildTreeData btdata,
		boolean renderSelection, boolean details, boolean thumbnail)
	{
		if( Check.isEmpty(nodes) )
		{
			return;
		}

		for( ItemNavigationNode node : nodes )
		{
			final List<ItemNavigationTab> tabs = getTabsWithAttachments(node);
			final boolean displayFolder = tabs.size() != 1;

			final List<ItemNavigationNode> childNodes = tree.getChildMap().get(node);
			final boolean hasChildren = !Check.isEmpty(childNodes);
			final int tabLevel = displayFolder ? level + 1 : level;

			if( !anyAttachments(node, tree, info, viewableItem) )
			{
				continue;
			}

			if( displayFolder )
			{
				AttachmentRowDisplay folder = new AttachmentRowDisplay();
				folder.setRow(makeFolderRowRenderer(node.getName(), level));
				folder.setLevel(level);
				attachmentDisplays.add(folder);
			}

			for( ItemNavigationTab tab : tabs )
			{
				final Attachment attachment = tab.getAttachment();
				ViewableResource viewableResource = attachmentResourceService.getViewableResource(info, viewableItem,
					attachment);
				if( !viewableResource.getBooleanAttribute(ViewableResource.KEY_HIDDEN) )
				{
					final AttachmentView attachmentView = new AttachmentView(attachment, viewableResource);
					attachmentView.setOverrideViewer(tab.getViewer());

					final AttachmentRowDisplay display = new AttachmentRowDisplay();
					display.setAttachmentView(attachmentView);
					display.setRow(makeAttachmentRowRenderer(info, viewableItem, baseElement, attachmentView, tabLevel,
						displayFolder ? tab.getName() : node.getName(), hasChildren, btdata.attachmentIndex++,
						renderSelection, details, thumbnail));
					display.setLevel(tabLevel);
					attachmentDisplays.add(display);
					btdata.nodedAttachments.add(attachment);
				}
			}

			buildTree(info, viewableItem, baseElement, tree, childNodes, attachmentDisplays, level + 1, btdata,
				renderSelection, details, thumbnail);
		}
	}

	private boolean anyAttachments(ItemNavigationNode node, ItemNavigationTree tree, SectionInfo info,
		ViewableItem viewableItem)
	{
		boolean hasAttachment = false;

		final List<ItemNavigationNode> childNodes = tree.getChildMap().get(node);
		final boolean hasChildren = !Check.isEmpty(childNodes);
		if( hasChildren )
		{
			for( ItemNavigationNode itemNavigationNode : childNodes )
			{
				hasAttachment |= anyAttachments(itemNavigationNode, tree, info, viewableItem);
			}
		}

		final List<ItemNavigationTab> tabs = node.getTabs();
		for( ItemNavigationTab tab : tabs )
		{
			final Attachment attachment = tab.getAttachment();
			if( attachment != null )
			{
				ViewableResource viewableResource = attachmentResourceService.getViewableResource(info, viewableItem,
					tab.getAttachment());
				hasAttachment |= !viewableResource.getBooleanAttribute(ViewableResource.KEY_HIDDEN);
			}
		}
		return hasAttachment;
	}

	private TagRenderer makeFolderRowRenderer(String name, int level)
	{
		final TagState state = new TagState();
		final TagRenderer row = makeRowRenderer(state, level, true);
		row.setNestedRenderable(CombinedRenderer.combineResults(ATTACHMENTS_CSS, new SpanRenderer(name)));
		return row;
	}

	private TagRenderer makeAttachmentRowRenderer(SectionInfo info, ViewableItem viewableItem, ElementId baseElement,
		AttachmentView view, int level, String displayName, boolean hasChildren, int attachmentIndex,
		boolean renderSelection, boolean details, boolean thumbnail)
	{
		final ViewableResource viewable = view.getViewableResource();
		final String viewer = getViewer(view, viewable);
		final LinkTagRenderer link = viewItemService.getViewableLink(info, viewable, viewer);
		final Label title = new TextLabel(displayName);
		final HtmlLinkState linkState = link.getLinkState();
		final ElementId linkId = new AppendedElementId(baseElement, "_attRow" + attachmentIndex);
		linkId.registerUse();
		linkState.setElementId(linkId);
		link.addClass("defaultviewer");
		link.setLabel(title);
		link.setTitle(title);

		final ItemKey itemId = viewableItem.getItemId();
		// use the local attachment uuid, viewable.getAttachment() returns the
		// remote one
		final String attachmentUuid = view.getAttachment().getUuid();
		linkState.setData("itemuuid", itemId.getUuid());
		linkState.setData("itemversion", Integer.toString(itemId.getVersion()));
		linkState.setData("attachmentuuid", attachmentUuid);
		linkState.setData("extensiontype", viewableItem.getItemExtensionType());

		Boolean nullOrIsSelected = nullOrIsSelected(info, viewableItem, view, renderSelection);
		List<SectionRenderable> renderables = Lists.newArrayList();

		// Add link
		renderables.add(new DivRenderer(link).addClass("link-div"));

		// Preview
		final boolean preview = view.getAttachment().isPreview();
		if( preview )
		{
			final TagRenderer previewSpan = new SpanRenderer(RESOURCES.getString("attachments.previewtext"))
				.addClass("preview-tag");
			renderables.add(previewSpan);
		}

		// Details link
		if( details )
		{
			renderables.add(makeShowDetailsLinkRenderer());
		}

		ItemStatus status = null;
		boolean notFound = false;
		try
		{
			final WorkflowStatus workflowStatus = viewable.getViewableItem().getWorkflowStatus();
			status = (workflowStatus == null ? null : workflowStatus.getStatusName());
		}
		catch( ItemNotFoundException infe )
		{
			notFound = true;
		}

		// Select button
		if( nullOrIsSelected != null && (status == null || status == ItemStatus.LIVE) && !notFound )
		{
			final HtmlComponentState hcs = new HtmlComponentState();

			ButtonRenderer button;

			if( nullOrIsSelected )
			{
				if( thumbnail )
				{
					button = new ButtonRenderer(hcs).showAs(ButtonType.MINUS).addClass("over-image");
				}
				else
				{
					button = new ButtonRenderer(hcs).showAs(ButtonType.MINUS).addClass("btn-plus");
				}
				// Making accessible icon button
				button.setAriaLabelValue(UNSELECT_LABEL.getText());
			}
			else
			{
				if( thumbnail )
				{
					button = new ButtonRenderer(hcs).showAs(ButtonType.PLUS).addClass("over-image");
				}
				else
				{
					button = new ButtonRenderer(hcs).showAs(ButtonType.PLUS).addClass("btn-plus");
				}
				// Making accessible icon button
				button.setAriaLabelValue(SELECT_LABEL.getText());
			}

			renderables.add(new DivRenderer(button).addClass("button-div"));
		}
		else
		{
			renderables.add(new DivRenderer("non-button-div", null));
		}

		// Add thumbnail
		SectionRenderable nested;
		if( thumbnail )
		{
			renderables.add(0, createThumbRenderer(info, baseElement, viewable, getViewer(view, viewable), level,
				view.getAttachment()));
			nested = new DivRenderer("thumbnail", CombinedRenderer.combineMultipleResults(renderables));
		}
		else
		{
			nested = CombinedRenderer.combineMultipleResults(renderables);
		}

		// embedded details
		if( details )
		{
			final List<LinkTagRenderer> alternateViewerLinks = new ArrayList<LinkTagRenderer>();
			for( NameValue viewerNv : viewItemService.getEnabledViewers(info, viewable) )
			{
				final String otherViewer = viewerNv.getValue();
				// Ignore the configured viewer
				if( !otherViewer.equals(viewer) && viewItemService.getViewer(otherViewer).supports(info, viewable) )
				{
					final LinkTagRenderer vlink = viewItemService.getViewableLink(info, viewable, otherViewer);
					vlink.addClass("viewer");
					vlink.addClass(otherViewer);
					vlink.setLabel(new TextLabel(viewerNv.getLabel()));
					// FIXME: use tab name if there is one
					vlink.setTitle(new TextLabel(viewable.getDescription()));
					vlink.ensureClickable();
					alternateViewerLinks.add(vlink);
				}
			}
			// Note: only want thumbnail if NOT using thumbnail view
			final SectionRenderable thumb = thumbnail ? null : createThumbRenderer(info, baseElement,
				view.getViewableResource(), viewer, attachmentIndex, view.getAttachment());
			List<AttachmentDetail> attDetails = viewable.getCommonAttachmentDetails();
			List<AttachmentDetail> extraDetails = viewable.getExtraAttachmentDetails();
			if( !Check.isEmpty(extraDetails) )
			{
				attDetails.addAll(extraDetails);
			}
			nested = CombinedRenderer.combineMultipleResults(nested, makeAttachmentDetailsRenderer(info, baseElement,
				attachmentUuid, attachmentIndex, thumb, attDetails, alternateViewerLinks));
		}

		final ElementId rowId = new AppendedElementId(baseElement, "_li" + attachmentIndex);
		rowId.registerUse();
		final TagRenderer row = makeRowRenderer(new TagState(rowId), level, hasChildren);
		row.addClass("attachmentrow");
		if( thumbnail )
		{
			row.addClass("span4");
		}
		row.setNestedRenderable(CombinedRenderer.combineMultipleResults(row.getNestedRenderable(), nested));

		if( nullOrIsSelected != null )
		{
			row.addClass(nullOrIsSelected.booleanValue() ? "with-unselect" : "with-select");
		}
		return row;
	}

	@Override
	public TagRenderer makeShowDetailsLinkRenderer()
	{
		HtmlLinkState detailsLink = new HtmlLinkState();
		detailsLink.setLabel(SHOW_DETAILS_LABEL);
		detailsLink.addClass("droparrow");
		return new LinkRenderer(detailsLink);
	}

	@Override
	public TagRenderer makeRowRenderer(TagState state, int level, boolean folder)
	{
		final TagRenderer li = new TagRenderer("li", state);
		li.addClass("level" + level);
		if( folder )
		{
			li.addClass("folder");
			li.setData("rowType", "folder");
		}
		li.addClass("inactive");
		// Needed to convert pre-renderable into a renderable...
		li.setNestedRenderable(CombinedRenderer.combineMultipleResults(ATTACHMENTS_CSS));
		return li;
	}

	private List<AttachmentViewFilter> getAttachmentFilters(SectionInfo info)
	{
		// E.g CAL
		return attachmentFilters.getBeanList();
	}

	private List<ItemNavigationTab> getTabsWithAttachments(ItemNavigationNode node)
	{
		if( Check.isEmpty(node.getTabs()) )
		{
			return Collections.emptyList();
		}

		List<ItemNavigationTab> rv = new ArrayList<ItemNavigationTab>();
		for( ItemNavigationTab tab : node.getTabs() )
		{
			if( tab.getAttachment() != null )
			{
				rv.add(tab);
			}
		}
		return rv;
	}

	private static final class BuildTreeData
	{
		final Set<Attachment> nodedAttachments = new HashSet<Attachment>();
		int attachmentIndex;
	}

	@NonNullByDefault(false)
	public static class AttachmentDetailsModel
	{
		private String id;
		private AttachmentView attachment;
		private List<LinkTagRenderer> viewerLinks;
		private SectionRenderable thumbnail;

		private HtmlLinkState viewLink;
		private LinkTagRenderer detailsLink;
		private LinkTagRenderer attachmentDescription;
		private ButtonRenderer selectButton;

		private boolean localFile;

		private final List<AttachmentDetail> details = Lists.newArrayList();
		private final Map<String, Pair<Label, Object>> specificDetail = new HashMap<String, Pair<Label, Object>>();

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public AttachmentView getAttachment()
		{
			return attachment;
		}

		public void setAttachment(AttachmentView attachment)
		{
			this.attachment = attachment;
		}

		public SectionRenderable getThumbnail()
		{
			return thumbnail;
		}

		public void setThumbnail(SectionRenderable thumbnail)
		{
			this.thumbnail = thumbnail;
		}

		public HtmlLinkState getViewLink()
		{
			return viewLink;
		}

		public void setViewLink(HtmlLinkState viewLink)
		{
			this.viewLink = viewLink;
		}

		public boolean isLocalFile()
		{
			return localFile;
		}

		public void setLocalFile(boolean localFile)
		{
			this.localFile = localFile;
		}

		public List<LinkTagRenderer> getViewerLinks()
		{
			return viewerLinks;
		}

		public void setViewerLinks(List<LinkTagRenderer> viewerLinks)
		{
			this.viewerLinks = viewerLinks;
		}

		public List<AttachmentDetail> getDetails()
		{
			return details;
		}

		public Map<String, Pair<Label, Object>> getSpecificDetail()
		{
			return specificDetail;
		}

		public void addSpecificDetail(String key, Pair<Label, Object> detail)
		{
			specificDetail.put(key, detail);
		}

		public ButtonRenderer getSelectButton()
		{
			return selectButton;
		}

		public void setSelectButton(ButtonRenderer selectButton)
		{
			this.selectButton = selectButton;
		}

		public LinkTagRenderer getAttachmentDescription()
		{
			return attachmentDescription;
		}

		public void setAttachmentDescription(LinkTagRenderer attachmentDescription)
		{
			this.attachmentDescription = attachmentDescription;
		}

		public LinkTagRenderer getDetailsLink()
		{
			return detailsLink;
		}

		public void setDetailsLink(LinkTagRenderer detailsLink)
		{
			this.detailsLink = detailsLink;
		}
	}
}
