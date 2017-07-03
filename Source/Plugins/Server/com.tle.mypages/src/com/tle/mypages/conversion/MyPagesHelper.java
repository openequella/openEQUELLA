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

package com.tle.mypages.conversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.helper.AbstractHelper;
import com.tle.core.item.service.ItemResolver;
import com.tle.mypages.MyPagesConstants;

/**
 * Not really a 'helper' class, but it's consistent naming with the
 * AttachmentHelper etc. which convert to and from XML
 * 
 * @author aholland
 */
@Bind
@Singleton
public class MyPagesHelper extends AbstractHelper
{
	@Inject
	private ItemResolver itemResolver;

	@Override
	public void load(PropBagEx xml, Item item)
	{
		final PropBagEx attachmentsXml = xml.aquireSubtree("attachments"); //$NON-NLS-1$
		for( Attachment attachment : item.getAttachmentsUnmodifiable() )
		{
			if( attachment.getAttachmentType() == AttachmentType.HTML && !checkRestricted(item, attachment) )
			{
				final PropBagEx aXml = new PropBagEx().newSubtree("attachment"); //$NON-NLS-1$
				final HtmlAttachment html = (HtmlAttachment) attachment;
				setNode(aXml, "conversion", html.isConversion()); //$NON-NLS-1$
				setNode(aXml, "size", html.getSize()); //$NON-NLS-1$
				setNode(aXml, "thumbnail", html.getThumbnail()); //$NON-NLS-1$
				setNode(aXml, "uuid", attachment.getUuid()); //$NON-NLS-1$
				setNode(aXml, "@type", MyPagesConstants.MYPAGES_CONTENT_TYPE); //$NON-NLS-1$
				setNode(aXml, "file", html.getFilename()); //$NON-NLS-1$
				setNode(aXml, "description", attachment.getDescription()); //$NON-NLS-1$
				attachmentsXml.append(Constants.BLANK, aXml);
			}
		}
	}

	private boolean checkRestricted(Item item, Attachment attachment)
	{
		return itemResolver.checkRestrictedAttachment(item, attachment, null);
	}

	@Override
	public void save(PropBagEx xml, Item item, Set<String> handled)
	{
		final PropBagEx attachmentsXml = xml.getSubtree("attachments"); //$NON-NLS-1$
		if( attachmentsXml != null )
		{
			for( PropBagEx aXml : attachmentsXml.iterator("attachment") ) //$NON-NLS-1$
			{
				final String type = aXml.getNode("@type"); //$NON-NLS-1$
				if( type.equals(MyPagesConstants.MYPAGES_CONTENT_TYPE) )
				{
					final HtmlAttachment html = new HtmlAttachment();
					html.setConversion(aXml.isNodeTrue("conversion")); //$NON-NLS-1$
					html.setSize(aXml.getIntNode("size", 0)); //$NON-NLS-1$
					final String thumb = aXml.getNode("thumbnail"); //$NON-NLS-1$
					html.setThumbnail(thumb.length() > 0 ? thumb : null);
					html.setDescription(aXml.getNode("description")); //$NON-NLS-1$
					html.setUuid(aXml.getNode("uuid", html.getUuid())); //$NON-NLS-1$
					addItemAttachment(item, html);
				}
			}
		}
		handled.add("attachments"); //$NON-NLS-1$
	}

	private void addItemAttachment(Item item, Attachment attachment)
	{
		List<Attachment> attachments = item.getAttachments();
		if( attachments == null )
		{
			attachments = new ArrayList<Attachment>();
			item.setAttachments(attachments);
		}
		attachments.add(attachment);
	}
}
