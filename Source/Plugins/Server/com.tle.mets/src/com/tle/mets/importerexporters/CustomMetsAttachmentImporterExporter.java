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

package com.tle.mets.importerexporters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.thoughtworks.xstream.XStream;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.core.guice.Bind;
import com.tle.mets.MetsIDElementInfo;
import com.tle.web.sections.SectionInfo;

import edu.harvard.hul.ois.mets.FLocat;
import edu.harvard.hul.ois.mets.File;
import edu.harvard.hul.ois.mets.Loctype;
import edu.harvard.hul.ois.mets.helper.MetsElement;
import edu.harvard.hul.ois.mets.helper.MetsIDElement;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class CustomMetsAttachmentImporterExporter extends AbstractMetsAttachmentImportExporter
{
	@Override
	public boolean canExport(Item item, Attachment attachment)
	{
		return attachment.getAttachmentType() == AttachmentType.CUSTOM;
	}

	@Override
	public boolean canImport(File parentElem, MetsElement elem, PropBagEx xmlData, ItemNavigationNode parentNode)
	{
		return idPrefixMatch(elem, "custom:");
	}

	@Override
	public List<MetsIDElementInfo<? extends MetsIDElement>> export(SectionInfo info, Item item, Attachment attachment)
	{
		final List<MetsIDElementInfo<? extends MetsIDElement>> res = new ArrayList<MetsIDElementInfo<? extends MetsIDElement>>();

		final CustomAttachment custom = (CustomAttachment) attachment;

		final PropBagEx xmlData = new PropBagEx();
		final XStream x = new XStream();
		xmlData.append("xstream", new PropBagEx(x.toXML(custom.getDataAttributesReadOnly())));
		xmlData.setNode("url", custom.getUrl());
		xmlData.setNode("uuid", custom.getUuid());
		xmlData.setNode("description", custom.getDescription());
		xmlData.setNode("type", custom.getType());

		final FLocat location = new FLocat();
		location.setID("custom:" + custom.getUuid());
		location.setXlinkHref(custom.getUuid());
		location.setXlinkTitle(custom.getDescription());
		location.setLOCTYPE(Loctype.OTHER);
		location.setOTHERLOCTYPE("equellacustom");

		res.add(new MetsIDElementInfo<FLocat>(location, "equella/" + custom.getType(), xmlData));
		return res;
	}

	@Override
	public void doImport(Item item, FileHandle staging, String packageFolder, File parentElem, MetsElement elem,
		PropBagEx xmlData, ItemNavigationNode parentNode, AttachmentAdder attachmentAdder)
	{
		final CustomAttachment custom = new CustomAttachment();

		final XStream x = new XStream();
		// need to use the first child
		PropBagEx mapXml = null;
		for( PropBagEx child : xmlData.getSubtree("xstream").iterator() )
		{
			mapXml = child;
			break;
		}
		if( mapXml != null )
		{
			custom.setDataAttributes((Map<String, Object>) x.fromXML(mapXml.toString()));
		}

		custom.setUrl(xmlData.getNode("url"));
		custom.setUuid(xmlData.getNode("uuid"));
		final String description = xmlData.getNode("description");
		custom.setDescription(description);
		custom.setType(xmlData.getNode("type"));

		attachmentAdder.addAttachment(parentNode, custom, description);
	}
}
