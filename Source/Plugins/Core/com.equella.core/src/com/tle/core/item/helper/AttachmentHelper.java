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

package com.tle.core.item.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.thoughtworks.xstream.XStream;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.IMSResourceAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.LinkAttachment;
import com.tle.beans.item.attachments.ZipAttachment;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemResolver;
import com.tle.core.url.URLCheckerService;

@SuppressWarnings("nls")
@Bind
@Singleton
public class AttachmentHelper extends AbstractHelper
{
	private final XStream customAttachXstream = new XStream();

	@Inject
	private URLCheckerService urlCheckerService;
	@Inject
	private ItemResolver itemResolver;

	public AttachmentHelper()
	{
		customAttachXstream.alias("attributes", Map.class);
	}

	@Override
	public void load(PropBagEx itemxml, Item bean)
	{
		PropBagEx attXml = itemxml.aquireSubtree("attachments");
		attXml.deleteAll(Constants.XML_WILD);

		for( Attachment attachment : bean.getAttachmentsUnmodifiable() )
		{
			//itemResolver will only be null for unit tests
			if( itemResolver == null || !itemResolver.checkRestrictedAttachment(bean, attachment, null) )
			{
				if( attachment.getAttachmentType() != AttachmentType.IMS )
				{
					PropBagEx aXml = new PropBagEx().newSubtree("attachment");
					String type;
					switch( attachment.getAttachmentType() )
					{
						case FILE:
							FileAttachment att = (FileAttachment) attachment;
							setNode(aXml, "conversion", att.isConversion());
							setNode(aXml, "size", att.getSize());
							type = "local";
							break;
						case LINK:
							LinkAttachment la = (LinkAttachment) attachment;
							if( urlCheckerService != null )
							{
								setNode(aXml, "@disabled", urlCheckerService.isUrlDisabled(la.getUrl()));
							}
							else
							{
								setNode(aXml, "@disabled", false);
							}
							type = "remote";
							break;
						case ZIP:
							ZipAttachment za = (ZipAttachment) attachment;
							setNode(aXml, "@mapped", za.isMapped());
							type = "zip";
							break;
						case IMSRES:
							type = "imsres";
							break;
						case CUSTOM:
							CustomAttachment ca = (CustomAttachment) attachment;
							setNode(aXml, "type", ca.getType());
							type = "custom";
							break;
						default:
							type = null;
					}
					if( type != null )
					{
						setNode(aXml, "uuid", attachment.getUuid());
						setNode(aXml, "@type", type);
						setNode(aXml, "file", attachment.getUrl());
						setNode(aXml, "description", attachment.getDescription());
						setNode(aXml, "restricted", attachment.isRestricted());
						setNode(aXml, "thumbnail", attachment.getThumbnail());

						Map<String, Object> dataAttributes = new HashMap<String, Object>(
							attachment.getDataAttributesReadOnly());
						PropBagEx dataXml = new PropBagEx(customAttachXstream.toXML(dataAttributes));
						aXml.appendChildren("attributes", dataXml);

						attXml.append(Constants.BLANK, aXml);
					}
				}
				else
				{
					ImsAttachment ims = (ImsAttachment) attachment;
					PropBagEx packagefile = itemxml.aquireSubtree("itembody/packagefile");
					setNode(packagefile, "@name", ims.getDescription());
					setNode(packagefile, "@size", ims.getSize());
					if( ims.isScorm() )
					{
						setNode(packagefile, "@scorm", ims.getScormVersion());
					}
					setNode(packagefile, Constants.XML_ROOT, ims.getUrl());
					setNode(packagefile, "@uuid", ims.getUuid());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void save(PropBagEx xml, Item item, Set<String> handled)
	{
		List<Attachment> attachments = new ArrayList<Attachment>();
		PropBagEx attxml = xml.getSubtree("attachments");
		if( attxml != null )
		{
			for( PropBagEx aXml : attxml.iterator("attachment") )
			{
				Attachment attachment = null;

				String type = aXml.getNode("@type");
				if( type.equals("local") )
				{
					FileAttachment att = new FileAttachment();
					att.setConversion(aXml.isNodeTrue("conversion"));
					att.setSize(aXml.getIntNode("size", 0));
					attachment = att;
				}
				else if( type.equals("remote") )
				{
					attachment = new LinkAttachment();
				}
				else if( type.equals("zip") )
				{
					ZipAttachment att = new ZipAttachment();
					att.setMapped(aXml.isNodeTrue("@mapped"));

					attachment = att;
				}
				else if( type.equals("imsres") )
				{
					IMSResourceAttachment imsres = new IMSResourceAttachment();
					attachment = imsres;
				}
				else if( type.equals("custom") )
				{
					CustomAttachment ca = new CustomAttachment();
					ca.setType(aXml.getNode("type"));
					attachment = ca;
				}

				if( attachment != null )
				{
					attachment.setUrl(aXml.getNode("file"));
					attachment.setDescription(aXml.getNode("description"));
					attachment.setUuid(aXml.getNode("uuid", attachment.getUuid()));
					String thumb = aXml.getNode("thumbnail");
					attachment.setThumbnail(thumb.length() > 0 ? thumb : null);
					attachment.setRestricted(aXml.isNodeTrue("restricted"));

					PropBagEx attributes = aXml.getSubtree("attributes");
					if( attributes != null )
					{
						Map<String, Object> data = (Map<String, Object>) customAttachXstream
							.fromXML(attributes.toString(), new HashMap<String, Object>());
						attachment.setDataAttributes(data);
					}

					attachments.add(attachment);
				}
			}
		}

		PropBagEx packagefile = xml.getSubtree("itembody/packagefile");
		if( packagefile != null )
		{
			ImsAttachment ims = new ImsAttachment();
			ims.setDescription(packagefile.getNode("@name"));
			ims.setUrl(packagefile.getNode(Constants.XML_ROOT));
			ims.setScormVersion(packagefile.getNode("@scorm"));
			ims.setSize(packagefile.getIntNode("@size", 0));
			String uuid = packagefile.getNode("@uuid");
			if( !Check.isEmpty(uuid) )
			{
				ims.setUuid(uuid);
			}
			attachments.add(ims);
		}

		item.setAttachments(attachments);
		handled.addAll(Arrays.asList(new String[]{"attachments", "itembody/packagefile"}));
	}
}