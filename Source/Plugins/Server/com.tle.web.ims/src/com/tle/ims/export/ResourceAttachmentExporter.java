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

package com.tle.ims.export;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.LinkAttachment;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.util.ims.beans.IMSCustomData;
import com.tle.core.util.ims.beans.IMSManifest;
import com.tle.core.util.ims.beans.IMSMetadata;
import com.tle.core.util.ims.beans.IMSResource;
import com.tle.core.util.ims.extension.IMSAttachmentExporter;
import com.tle.core.util.ims.extension.IMSManifestExporter;

/**
 * Need to find a better place to put this Resource attachments always create a
 * LinkAttachment upon import.
 * 
 * @author Aaron
 */
@Bind
@Singleton
public class ResourceAttachmentExporter implements IMSAttachmentExporter, IMSManifestExporter
{
	private static final String DATA_TYPE = "type"; //$NON-NLS-1$
	private static final String DATA_UUID = "uuid"; //$NON-NLS-1$
	private static final String DATA_VERSION = "version"; //$NON-NLS-1$

	// @Inject
	// private AttachmentResourceService resourceService;
	@Inject
	private InstitutionService institutionService;

	// @Inject
	// private ViewableItemFactory viewableItemFactory;

	@Override
	public boolean exportAttachment(Item item, IAttachment attachment, List<IMSResource> resources, FileHandle imsRoot)
	{
		if( attachment.getAttachmentType() == AttachmentType.CUSTOM )
		{
			final CustomAttachment custom = (CustomAttachment) attachment;
			if( custom.getType().equals("resource") )
			{
				// final ViewableItem vitem =
				// viewableItemFactory.createNewViewableItem(item
				// .getItemId());
				// final ViewableResource viewableResource =
				// resourceService.getViewableResource(info,
				// vitem, attachment);
				// final ViewItemUrl vurl =
				// viewableResource.createDefaultViewerUrl();
				// vurl.setBackTo(null);

				final IMSMetadata data = new IMSMetadata();
				final IMSCustomData customData = new IMSCustomData();

				PropBagEx xml = new PropBagEx("<equella/>");
				xml.setNode("type", "resource");
				PropBagEx resourceData = xml.aquireSubtree("resourcedata");
				resourceData.setNode("description", custom.getDescription());
				customData.setXml(xml);

				data.setData(customData);

				final IMSResource res = new IMSResource();
				res.setMetadata(data);
				res.setIdentifier(attachment.getUuid());

				// FIXME: the old way was better (see commented out crap) but
				// relied on a section info
				final Map<String, Object> attr = attachment.getDataAttributesReadOnly();
				final String itemUuid = (String) attr.get(DATA_UUID);
				final Integer itemVersion = (Integer) attr.get(DATA_VERSION);
				final String type = (String) attr.get(DATA_TYPE);
				String attachmentUuid = null;
				if( type.equals("a") )
				{
					attachmentUuid = attachment.getUrl();
				}
				res.setHref(institutionService.institutionalise("items/" + itemUuid + "/" + itemVersion
					+ (attachmentUuid == null ? "" : "/?attachment.uuid=" + URLUtils.urlEncode(attachmentUuid))));

				resources.add(res);
				return true;
			}
		}
		return false;
	}

	@Override
	public Attachment importAttachment(Item item, IMSResource resource, FileHandle root, String packageFolder)
	{
		IMSMetadata metadata = resource.getMetadata();
		if( metadata != null )
		{
			IMSCustomData data = metadata.getData();
			if( data != null )
			{
				PropBagEx xml = data.getXml();
				String type = xml.getNode("type");
				if( !Check.isEmpty(type) && type.equals("resource") )
				{
					LinkAttachment linkAttachment = new LinkAttachment();
					linkAttachment.setUuid(resource.getIdentifier());
					linkAttachment.setUrl(resource.getHref());
					linkAttachment.setDescription(xml.getNode("resourcedata/description"));
					return linkAttachment;
				}
			}
		}
		return null;
	}

	@Override
	public void exportManifest(Item item, FileHandle staging, IMSManifest manifest)
	{
	}

	@Override
	public void importManifest(Item item, FileHandle staging, IMSManifest manifest)
	{
	}
}
