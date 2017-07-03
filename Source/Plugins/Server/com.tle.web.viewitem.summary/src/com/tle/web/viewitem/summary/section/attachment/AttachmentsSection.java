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

import static com.tle.common.collection.AttachmentConfigConstants.DISPLAY_MODE_KEY;
import static com.tle.common.collection.AttachmentConfigConstants.DISPLAY_MODE_THUMBNAIL;
import static com.tle.common.collection.AttachmentConfigConstants.METADATA_TARGET;
import static com.tle.common.collection.AttachmentConfigConstants.SHOW_FULLSCREEN_LINK_KEY;
import static com.tle.common.collection.AttachmentConfigConstants.SHOW_FULLSCREEN_LINK_NEW_WINDOW_KEY;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.exceptions.AttachmentNotFoundException;
import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.LinkAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.beans.item.attachments.ZipAttachment;
import com.tle.common.Check;
import com.tle.common.security.SecurityConstants;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.security.TLEAclManager;
import com.tle.core.url.URLCheckerService;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.IconLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.AttachmentViewFilter;
import com.tle.web.viewitem.attachments.AttachmentView;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewitem.summary.attachment.service.ViewAttachmentWebService.AttachmentRowDisplay;
import com.tle.web.viewitem.summary.section.DisplaySectionConfiguration;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;

@Bind
@NonNullByDefault
@SuppressWarnings("nls")
public class AttachmentsSection extends AbstractAttachmentsSection<Item, AttachmentsSection.Model>
	implements
		ViewableChildInterface,
		DisplaySectionConfiguration
{
	private static final JSCallable SETUP_REORDER = new ExternallyDefinedFunction(
		AbstractAttachmentsSection.ATTACHMENTS_CLASS, "setupReorder", 6);

	@PlugKey("attachments.title")
	private static Label LABEL_ATTACHMENTS_TITLE;
	@PlugKey("summary.content.attachments.link.reorder")
	@Component
	private Link reorderAttachments;

	private LanguageBundle title;
	private List<String> metadataTargets;
	private String attachmentControlId;

	@Inject
	private BundleCache bundleCache;
	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	private URLCheckerService urlCheckerService;
	@Inject
	private ItemOperationFactory itemOperationFactory;
	@Inject
	private ItemService itemService;
	@Inject
	private TLEAclManager tleACLManager;

	@AjaxFactory
	private AjaxGenerator ajax;

	@Nullable
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !canView(context) )
		{
			return null;
		}
		return super.renderHtml(context);
	}

	@Override
	protected void customRender(RenderEventContext context, AttachmentsModel model, ViewableItem<Item> viewableItem,
		List<AttachmentRowDisplay> attachmentDisplays)
	{
		super.customRender(context, model, viewableItem, attachmentDisplays);

		final Item item = viewableItem.getItem();

		if( !tleACLManager.filterNonGrantedPrivileges(item, Collections.singleton(SecurityConstants.EDIT_ITEM))
			.isEmpty() && selectionService.getCurrentSession(context) == null
			&& attachmentStructureReorderable(attachmentDisplays, item) )
		{
			reorderAttachments.setClickHandler(context,
				new OverrideHandler(SETUP_REORDER, div.getElementId(context),
					ajax.getAjaxFunction("attachmentsReordered"), MODAL_CLOSE_WARNING_LABEL.getText(),
					MODAL_SAVE_LABEL.getText(), MODAL_CANCEL_LABEL.getText(), isShowStructuredView()));
			reorderAttachments.setLabel(context, new IconLabel(Icon.MOVE, reorderAttachments.getLabel(context), false));
		}
		else
		{
			reorderAttachments.setDisplayed(context, false);
		}
	}

	@AjaxMethod
	public void attachmentsReordered(SectionInfo info, List<String> newOrder)
	{
		Item item = getViewableItem(info).getItem();
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
			itemService.operation(itemKey,
				new WorkflowOperation[]{itemOperationFactory.editMetadata(itemService.getItemPack(itemKey)),
						itemOperationFactory.saveNoSaveScript(true)});
		}
	}

	private boolean attachmentStructureReorderable(List<AttachmentRowDisplay> attachmentDisplays, Item item)
	{
		return attachmentDisplays.size() > 1 && isFlatHierachy(attachmentDisplays)
			&& (item.getTreeNodes() == null || item.getTreeNodes().isEmpty()) && !containsHiddenZip(item);
	}

	private boolean containsHiddenZip(Item item)
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

	@Override
	protected Bookmark getFullscreenBookmark(SectionInfo info, ViewableItem<Item> vitem)
	{
		return urlFactory.createItemUrl(info, vitem,
			UrlEncodedString.createFromFilePath("viewcontent/" + attachmentControlId),
			ViewItemUrl.FLAG_IGNORE_SESSION_TEMPLATE);
	}

	@Override
	protected ViewableItem<Item> getViewableItem(SectionInfo info)
	{
		final ItemSectionInfo itemInfo = getItemInfo(info);
		return itemInfo.getViewableItem();
	}

	@Nullable
	@Override
	protected String getItemExtensionType()
	{
		return null;
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return getItemInfo(info).getPrivileges().contains("VIEW_ITEM");
	}

	/**
	 * Only render the fullscreen link if there are more than 0 enabled
	 * attachments.
	 */
	@Override
	protected boolean showFullscreen(SectionInfo info, Item item, List<AttachmentRowDisplay> rows)
	{
		final UnmodifiableAttachments attachments = new UnmodifiableAttachments(item);
		final List<LinkAttachment> links = attachments.getList(AttachmentType.LINK);

		// Means there must be at least one other displayed attachment
		if( links.size() < rows.size() )
		{
			return true;
		}

		for( LinkAttachment link : links )
		{
			if( !urlCheckerService.isUrlDisabled(link.getUrl()) )
			{
				return true;
			}
		}
		// All attachments are links and they are all disabled
		return false;
	}

	@Override
	protected Label getTitle(SectionInfo info, ViewableItem<Item> vitem)
	{
		if( title != null )
		{
			return new BundleLabel(title, LABEL_ATTACHMENTS_TITLE.getText(), bundleCache);
		}
		return LABEL_ATTACHMENTS_TITLE;
	}

	@Override
	protected AttachmentViewFilter getCustomFilter(SectionInfo info, ViewableItem<Item> vitem, boolean filtered)
	{
		return new AttachmentViewFilter()
		{
			@Override
			public boolean shouldBeDisplayed(SectionInfo info, AttachmentView attachmentView)
			{
				if( metadataTargets.isEmpty() )
				{
					return true;
				}

				if( !filtered && attachmentView.getAttachment().getAttachmentType() == AttachmentType.IMSRES )
				{
					return true;
				}
				// Check attachment UUID vs presence in item XML at any associated Xpath
				String attachmentUuid = attachmentView.getAttachment().getUuid();
				final PropBagEx xml = attachmentView.getViewableResource().getViewableItem().getItemxml();
				for( String target : metadataTargets )
				{
					for( String val : xml.getNodeList(target) )
					{
						if( val.equals(attachmentUuid) )
						{
							return true;
						}
					}
				}
				return false;
			}
		};
	}

	@Override
	public void associateConfiguration(SummarySectionsConfig config)
	{
		String configuration = config.getConfiguration();
		title = config.getBundleTitle();
		attachmentControlId = config.getUuid();
		if( !Check.isEmpty(configuration) )
		{
			PropBagEx xml = new PropBagEx(configuration);
			showFull = xml.isNodeTrue(SHOW_FULLSCREEN_LINK_KEY);
			showFullNewWindow = xml.isNodeTrue(SHOW_FULLSCREEN_LINK_NEW_WINDOW_KEY);
			showStructuredView = !xml.getNode(DISPLAY_MODE_KEY).equals(DISPLAY_MODE_THUMBNAIL);
			metadataTargets = xml.getNodeList(METADATA_TARGET);
		}
		else
		{
			metadataTargets = Collections.emptyList();
		}
	}

	@Override
	protected boolean isFiltered(ViewableItem<Item> viewableItem)
	{
		if( !metadataTargets.isEmpty() )
		{
			UnmodifiableAttachments attachments = new UnmodifiableAttachments(viewableItem.getItem());
			ImsAttachment ims = attachments.getIms();
			if( ims != null )
			{
				PropBagEx xml = viewableItem.getItemxml();
				String attachmentUuid = ims.getUuid();
				for( String target : metadataTargets )
				{
					for( String val : xml.getNodeList(target) )
					{
						if( val.equals(attachmentUuid) )
						{
							return false;
						}
					}
				}
			}
			return true;
		}
		return false;
	}

	private ItemSectionInfo getItemInfo(SectionInfo info)
	{
		return ParentViewItemSectionUtils.getItemInfo(info);
	}

	@Override
	public Model instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	@Override
	protected String getAttchmentControlId()
	{
		return attachmentControlId;
	}

	public Link getReorderAttachments()
	{
		return reorderAttachments;
	}

	public static class Model extends AbstractAttachmentsSection.AttachmentsModel
	{
		// Nothing specific
	}
}