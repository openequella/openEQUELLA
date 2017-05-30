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

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.LinkAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.url.URLCheckerService;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.i18n.BundleCache;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
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
	@PlugKey("attachments.title")
	private static Label LABEL_ATTACHMENTS_TITLE;

	private LanguageBundle title;
	private List<String> metadataTargets;
	private String attachmentControlId;

	@Inject
	private BundleCache bundleCache;
	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	private URLCheckerService urlCheckerService;

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

	public static class Model extends AbstractAttachmentsSection.AttachmentsModel
	{
		// Nothing specific
	}

	@Override
	protected String getAttchmentControlId()
	{
		return attachmentControlId;
	}
}