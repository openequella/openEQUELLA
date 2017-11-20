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

package com.tle.web.controls.resource.viewer;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.service.ItemCommentService;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.RegisterMimeTypeExtension;
import com.tle.web.controls.universal.AttachmentHandlerUtils;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.NumberLabel;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.selection.SelectedResource;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceExtension;
import com.tle.web.viewurl.attachments.AttachmentResourceService;
import com.tle.web.viewurl.resource.AbstractWrappedResource;
import com.tle.web.viewurl.resource.SimpleUrlResource;

@SuppressWarnings("nls")
@Bind
@Singleton
public class ResourceAttachmentResource
	implements
		AttachmentResourceExtension<CustomAttachment>,
		RegisterMimeTypeExtension<CustomAttachment>
{
	static
	{
		PluginResourceHandler.init(ResourceAttachmentResource.class);
	}

	@PlugKey("ressel.details.type")
	private static Label TYPE;
	@PlugKey("ressel.details.mimetype")
	private static Label MIMETYPE;
	@PlugKey("ressel.details.item.nodetail")
	private static Label LABEL_NA;
	@PlugKey("ressel.details.item.name")
	private static Label ITEM_NAME;
	@PlugKey("ressel.details.item.collection")
	private static Label ITEM_COLLECTION;
	@PlugKey("ressel.details.item.version")
	private static Label ITEM_VERSION;
	@PlugKey("ressel.details.item.rating")
	private static Label ITEM_RATING;
	@PlugKey("ressel.details.item.lastmod")
	private static Label ITEM_MODIFIED;

	private static final Log LOGGER = LogFactory.getLog(ResourceAttachmentResource.class);

	@Inject
	private ViewableItemFactory viewableItemFactory;
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private ItemService itemService;
	@Inject
	private IntegrationService integService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private ItemCommentService itemCommentService;
	@Inject
	private DateRendererFactory dateRendererFactory;

	@Override
	public ViewableResource process(SectionInfo info, ViewableResource resource, CustomAttachment attachment)
	{
		char type = ((String) attachment.getData("type")).charAt(0);
		if( SelectedResource.TYPE_REMOTE == type )
		{
			return new SimpleUrlResource(resource, attachment.getUrl(), attachment.getDescription(), false);
		}
		try
		{
			boolean latest = false;
			ItemId itemId = new ItemId((String) attachment.getData("uuid"), (Integer) attachment.getData("version"));
			if( itemId.getVersion() == 0 )
			{
				latest = true;
				int liveVersion = itemService.getLiveItemVersion(itemId.getUuid());
				itemId = new ItemId(itemId.getUuid(), liveVersion);
			}

			final ViewableItem<Item> viewableItem;
			if( integService.isInIntegrationSession(info) )
			{
				viewableItem = integService.getIntegrationServiceForData(integService.getSessionData(info))
					.createViewableItem(itemId, latest, null);
			}
			else
			{
				viewableItem = viewableItemFactory.createNewViewableItem(itemId);
			}

			if( type == SelectedResource.TYPE_PATH )
			{
				ViewableResource baseResource = attachmentResourceService.createPathResource(info, viewableItem,
					attachment.getUrl(), attachment);
				if( Check.isEmpty(attachment.getUrl()) )
				{
					baseResource = new LinkedItemResource(baseResource, viewableItem);
				}
				resource = baseResource;
			}
			else if( type == SelectedResource.TYPE_ATTACHMENT )
			{
				Item item = viewableItem.getItem();
				Map<String, Attachment> attachMap = UnmodifiableAttachments
					.convertToMapUuid(item.getAttachmentsUnmodifiable());

				//TODO: quicker? possibly slower if getItem is called elsewhere
				//Attachment linkedAttachment = itemService.getAttachmentForFilepath(itemId, attachment.getUrl());
				Attachment linkedAttachment = attachMap.get(attachment.getUrl());
				if( linkedAttachment != null )
				{
					resource = attachmentResourceService.getViewableResource(info, viewableItem, linkedAttachment);
					// attached CustomAttachments should always be visible...
					resource.setAttribute(ViewableResource.KEY_HIDDEN, false);
				}
				else
				{
					// TODO: This needs to be turned into a resource
					// which shows \"deleted\" or something
					LOGGER.warn("Attachment not found");
					FileAttachment dummy = new FileAttachment();
					dummy.setDescription(attachment.getDescription());
					dummy.setFilename(attachment.getDescription());
					resource = attachmentResourceService.getViewableResource(info, viewableItem, dummy);
				}
			}
		}
		catch( NotFoundException nfe )
		{
			// TODO: This needs to be turned into a resource which
			// shows \"deleted\" or something
			FileAttachment dummy = new FileAttachment();
			dummy.setDescription(attachment.getDescription());
			dummy.setFilename(attachment.getDescription());
			resource = attachmentResourceService.getViewableResource(info, resource.getViewableItem(), dummy);
		}
		return resource;
	}

	public class LinkedItemResource extends AbstractWrappedResource
	{
		private final ViewableItem<Item> vitem;

		public LinkedItemResource(ViewableResource inner, ViewableItem<Item> vitem)
		{
			super(inner);
			this.vitem = vitem;
		}

		@Override
		public String getMimeType()
		{
			return MimeTypeConstants.MIME_ITEM;
		}

		@Override
		public String getDescription()
		{
			final Map<ItemId, LanguageBundle> itemNames = itemService
				.getItemNames(Collections.singleton(vitem.getItemId()));
			return CurrentLocale.get(itemNames.get(vitem.getItemId()), vitem.getItemId().getUuid());
		}

		@Override
		public ImageRenderer createStandardThumbnailRenderer(Label label)
		{
			ItemKey itemId = getViewableItem().getItemId();
			String source = institutionService
				.institutionalise(MessageFormat.format("thumbs/{0}/{1}/", itemId.getUuid(), itemId.getVersion()));
			return new ImageRenderer(source, label);
		}

		@Override
		public List<AttachmentDetail> getCommonAttachmentDetails()
		{
			List<AttachmentDetail> commonDetails = new ArrayList<AttachmentDetail>();

			// Type
			commonDetails.add(makeDetail(TYPE, MIMETYPE));

			// Get Item details
			ItemId itemId = ItemId.fromKey(vitem.getItemId());
			Map<String, Object> allInfo = itemService.getItemInfo(itemId);
			int rating = AttachmentHandlerUtils.getRating(itemCommentService.getAverageRatingForItem(itemId));

			if( !Check.isEmpty(allInfo) )
			{
				BundleLabel name = new BundleLabel(allInfo.get("name_id"), LABEL_NA, bundleCache);
				BundleLabel collection = new BundleLabel(allInfo.get("collection_id"), bundleCache);

				commonDetails.add(makeDetail(ITEM_NAME, name)); // Name
				commonDetails.add(makeDetail(ITEM_COLLECTION, collection)); // Collection
				commonDetails.add(makeDetail(ITEM_VERSION, new NumberLabel(itemId.getVersion()))); // Version
				Date date = (Date) allInfo.get("date_mod");
				if( date != null )
				{
					commonDetails.add(makeDetail(ITEM_MODIFIED, dateRendererFactory.createDateRenderer(date)));
				}
				commonDetails.add(makeDetail(ITEM_RATING,
					new DivRenderer("rating-stars " + AttachmentHandlerUtils.RATING_CLASSES.get(rating), "")));
			}

			return commonDetails;
		}
	}

	@Override
	public String getMimeType(CustomAttachment attachment)
	{
		return MimeTypeConstants.MIME_ITEM;
	}
}
