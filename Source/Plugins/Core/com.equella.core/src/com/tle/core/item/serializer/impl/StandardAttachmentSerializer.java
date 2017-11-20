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

package com.tle.core.item.serializer.impl;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.IMSResourceAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.LinkAttachment;
import com.tle.beans.item.attachments.ZipAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.ItemEditor;
import com.tle.core.item.edit.attachment.AttachmentEditor;
import com.tle.core.item.edit.attachment.FileAttachmentEditor;
import com.tle.core.item.edit.attachment.PackageAttachmentEditor;
import com.tle.core.item.edit.attachment.PackageResourceAttachmentEditor;
import com.tle.core.item.edit.attachment.ScormAttachmentEditor;
import com.tle.core.item.edit.attachment.ScormResourceAttachmentEditor;
import com.tle.core.item.edit.attachment.UrlAttachmentEditor;
import com.tle.core.item.edit.attachment.ZipAttachmentEditor;
import com.tle.core.item.serializer.AbstractAttachmentSerializer;
import com.tle.core.url.URLCheckerService;
import com.tle.web.api.item.equella.interfaces.beans.AbstractFileAttachmentBean;
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;
import com.tle.web.api.item.equella.interfaces.beans.FileAttachmentBean;
import com.tle.web.api.item.equella.interfaces.beans.PackageAttachmentBean;
import com.tle.web.api.item.equella.interfaces.beans.PackageResourceAttachmentBean;
import com.tle.web.api.item.equella.interfaces.beans.ScormAttachmentBean;
import com.tle.web.api.item.equella.interfaces.beans.ScormResourceAttachmentBean;
import com.tle.web.api.item.equella.interfaces.beans.UrlAttachmentBean;
import com.tle.web.api.item.equella.interfaces.beans.ZipAttachmentBean;

@Bind
@Singleton
@SuppressWarnings("nls")
public class StandardAttachmentSerializer extends AbstractAttachmentSerializer
{
	@Inject
	private URLCheckerService urlCheckerService;

	@Override
	public EquellaAttachmentBean serialize(Attachment attachment)
	{
		EquellaAttachmentBean bean = null;
		switch( attachment.getAttachmentType() )
		{
			case FILE:
				FileAttachment fattach = (FileAttachment) attachment;
				FileAttachmentBean fbean = new FileAttachmentBean();
				bean = fbean;
				fbean.setParentZip((String) fattach.getData(ZipAttachment.KEY_ZIP_ATTACHMENT_UUID));
				fbean.setConversion(fattach.isConversion());
				copyFileData(fattach, fbean);
				break;
			case ZIP:
				ZipAttachment zattach = (ZipAttachment) attachment;
				ZipAttachmentBean zbean = new ZipAttachmentBean();
				zbean.setFolder(zattach.getUrl());
				zbean.setMapped(zattach.isMapped());
				bean = zbean;
				break;
			case LINK:
				LinkAttachment lattach = (LinkAttachment) attachment;
				UrlAttachmentBean ubean = new UrlAttachmentBean();
				ubean.setUrl(lattach.getUrl());
				ubean.setDisabled(urlCheckerService.isUrlDisabled(lattach.getUrl()));
				bean = ubean;
				break;
			case IMS:
				ImsAttachment iattach = (ImsAttachment) attachment;
				PackageAttachmentBean pbean = new PackageAttachmentBean();
				pbean.setPackageFile(iattach.getUrl());
				pbean.setSize(iattach.getSize());
				pbean.setExpand(iattach.isExpand());
				bean = pbean;
				break;
			case IMSRES:
				IMSResourceAttachment irattach = (IMSResourceAttachment) attachment;
				PackageResourceAttachmentBean prbean = new PackageResourceAttachmentBean();
				prbean.setFilename(irattach.getUrl());
				prbean.setMd5(irattach.getMd5sum());
				bean = prbean;
				break;
			case CUSTOM:
				CustomAttachment cattach = (CustomAttachment) attachment;
				if( cattach.getType().equals("scorm") )
				{
					ScormAttachmentBean sbean = new ScormAttachmentBean();
					sbean.setPackageFile(cattach.getUrl());
					Long fileSize = (Long) cattach.getData("fileSize");
					sbean.setSize(fileSize);
					sbean.setScormVersion((String) cattach.getData("SCORM_VERSION"));
					bean = sbean;
				}
				else
				{
					ScormResourceAttachmentBean sbean = new ScormResourceAttachmentBean();
					sbean.setFilename(cattach.getUrl());
					Long fileSize = (Long) cattach.getData("fileSize");
					if( fileSize != null )
					{
						sbean.setSize(fileSize);
					}
					sbean.setMd5(cattach.getMd5sum());
					bean = sbean;
				}
				break;

			default:
				break;
		}
		return bean;
	}

	private void copyFileData(FileAttachment fattach, AbstractFileAttachmentBean fbean)
	{
		fbean.setMd5(fattach.getMd5sum());
		fbean.setFilename(fattach.getFilename());
		fbean.setSize(fattach.getSize());
	}

	@Override
	public Map<String, Class<? extends EquellaAttachmentBean>> getAttachmentBeanTypes()
	{
		Builder<String, Class<? extends EquellaAttachmentBean>> builder = ImmutableMap.builder();
		builder.put("file", FileAttachmentBean.class);
		builder.put("url", UrlAttachmentBean.class);
		builder.put("zip", ZipAttachmentBean.class);
		builder.put("package", PackageAttachmentBean.class);
		builder.put("package-res", PackageResourceAttachmentBean.class);
		builder.put("scorm", ScormAttachmentBean.class);
		builder.put("scorm-res", ScormResourceAttachmentBean.class);
		return builder.build();
	}

	@Override
	public String deserialize(EquellaAttachmentBean attachBean, ItemEditor itemEditor)
	{
		AttachmentType type = AttachmentType.CUSTOM;
		String rawAttachmentType = attachBean.getRawAttachmentType();
		if( !rawAttachmentType.startsWith("custom/") )
		{
			type = AttachmentType.valueOf(rawAttachmentType.toUpperCase());
		}
		AttachmentEditor attachEditor = null;
		String attachUuid = attachBean.getUuid();
		switch( type )
		{
			case FILE:
				FileAttachmentBean fattach = (FileAttachmentBean) attachBean;
				FileAttachmentEditor editor = itemEditor.getAttachmentEditor(attachUuid, FileAttachmentEditor.class);
				attachEditor = editor;
				editor.editZipParent(fattach.getParentZip());
				editor.editFilename(fattach.getFilename());
				editor.editConversion(fattach.isConversion());
				break;
			case LINK:
				UrlAttachmentBean uattach = (UrlAttachmentBean) attachBean;
				UrlAttachmentEditor ueditor = itemEditor.getAttachmentEditor(attachUuid, UrlAttachmentEditor.class);
				attachEditor = ueditor;
				ueditor.editUrl(uattach.getUrl());
				break;
			case IMSRES:
				PackageResourceAttachmentBean prattach = (PackageResourceAttachmentBean) attachBean;
				PackageResourceAttachmentEditor preditor = itemEditor.getAttachmentEditor(attachUuid,
					PackageResourceAttachmentEditor.class);
				attachEditor = preditor;
				preditor.editFilename(prattach.getFilename());
				break;
			case IMS:
				PackageAttachmentBean pattach = (PackageAttachmentBean) attachBean;
				PackageAttachmentEditor peditor = itemEditor.getAttachmentEditor(attachUuid,
					PackageAttachmentEditor.class);
				peditor.setExpand(pattach.isExpand());
				attachEditor = peditor;
				peditor.editPackageFile(pattach.getPackageFile());
				break;
			case ZIP:
				ZipAttachmentBean zattach = (ZipAttachmentBean) attachBean;
				ZipAttachmentEditor zeditor = itemEditor.getAttachmentEditor(attachUuid, ZipAttachmentEditor.class);
				attachEditor = zeditor;
				zeditor.editFolder(zattach.getFolder());
				zeditor.editMapped(zattach.isMapped());
				break;
			case CUSTOM:
				if( rawAttachmentType.equals("custom/scorm") )
				{
					ScormAttachmentBean sattach = (ScormAttachmentBean) attachBean;
					ScormAttachmentEditor seditor = itemEditor.getAttachmentEditor(attachUuid,
						ScormAttachmentEditor.class);
					attachEditor = seditor;
					seditor.editPackageFile(sattach.getPackageFile());
					seditor.editScormVersion(sattach.getScormVersion());
				}
				else
				{
					ScormResourceAttachmentBean srattach = (ScormResourceAttachmentBean) attachBean;
					ScormResourceAttachmentEditor sreditor = itemEditor.getAttachmentEditor(attachUuid,
						ScormResourceAttachmentEditor.class);
					attachEditor = sreditor;
					sreditor.editFilename(srattach.getFilename());
				}
				break;
			default:
				throw new Error("Unknown attachment type");
		}
		editStandard(attachEditor, attachBean);
		return attachEditor.getAttachmentUuid();
	}

	@Override
	public boolean exportable(EquellaAttachmentBean bean)
	{
		return true;
	}
}
