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

package com.tle.web.integration.extension;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import com.tle.web.integration.IntegrationSessionExtension;
import com.tle.web.integration.SingleSignonForm;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectedResourceKey;
import com.tle.web.selection.SelectionSession;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.ViewableItemResolver;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@SuppressWarnings("nls")
@Bind
@Singleton
public class BasicInfoResultsExtension implements IntegrationSessionExtension
{
	@Inject
	private UserService userService;
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private ViewableItemResolver viewableItemResolver;

	@Override
	public void setupSession(SectionInfo info, SelectionSession session, SingleSignonForm form)
	{
		// Results only
	}

	@Override
	public void processResultForSingle(SectionInfo info, SelectionSession session, Map<String, String> params,
		String prefix, IItem<?> item, SelectedResource resource)
	{
		Bookmark bm = viewableItemResolver.createThumbnailAttachmentLink(item, resource.isLatest(),
			resource.getAttachmentUuid(), resource.getKey().getExtensionType());
		params.put(prefix + "thumbnail", bm.getHref());
	}

	// FIXME: Note: due to Jackson ObjectNode not being a Map subclass, need to
	// copy
	// and paste this here. Long term solution is pass in an ObjectNode into
	// processResultForSingle
	public void processSingle(SectionInfo info, ObjectNode link, String prefix, IItem<?> item,
		SelectedResource resource)
	{
		Bookmark bm = viewableItemResolver.createThumbnailAttachmentLink(item, resource.isLatest(),
			resource.getAttachmentUuid(), resource.getKey().getExtensionType());
		if( bm != null )
		{
			link.put(prefix + "thumbnail", bm.getHref());
		}
	}

	@Override
	public void processResultForMultiple(SectionInfo info, SelectionSession session, ObjectNode link, IItem<?> item,
		SelectedResource resource)
	{
		processSingle(info, link, "", item, resource);

		link.put("uuid", resource.getUuid());
		link.put("version", resource.isLatest() ? 0 : resource.getVersion());
		link.put("datecreated", item.getDateCreated().getTime());
		link.put("datecreated", item.getDateCreated().getTime());
		link.put("datemodified", item.getDateModified().getTime());
		link.put("itemName", CurrentLocale.get(item.getName(), item.getUuid()));
		link.put("itemDescription", CurrentLocale.get(item.getDescription(), ""));

		// TODO dirty:
		if( item instanceof Item )
		{
			UserBean ownerBean = userService.getInformationForUser(((Item) item).getOwner());
			if( ownerBean != null )
			{
				link.put("owner", Format.format(ownerBean));
			}
		}

		if( !Check.isEmpty(resource.getAttachmentUuid()) )
		{
			final SelectedResourceKey key = resource.getKey();
			final ViewableItem<?> viewableItem = viewableItemResolver.createViewableItem(item, key.getExtensionType());
			final IAttachment attachment = viewableItem.getAttachmentByUuid(resource.getAttachmentUuid());
			final ViewableResource viewableResource = attachmentResourceService.getViewableResource(info, viewableItem,
				attachment);
			link.put("attachmentUuid", attachment.getUuid());
			link.put("attachmentName", attachment.getDescription());
			link.put("mimeType", viewableResource.getMimeType());

			if( attachment.getAttachmentType() == AttachmentType.FILE )
			{
				FileAttachment fileAttachment = (FileAttachment) attachment;
				link.put("filesize", fileAttachment.getSize());
				link.put("filename", fileAttachment.getFilename());
			}
		}
	}
}
