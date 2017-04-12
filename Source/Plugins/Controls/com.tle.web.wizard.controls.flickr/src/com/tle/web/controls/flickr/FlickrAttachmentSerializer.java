package com.tle.web.controls.flickr;

import java.util.Date;
import java.util.Map;

import javax.inject.Singleton;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.ItemEditor;
import com.tle.core.item.serializer.AbstractAttachmentSerializer;
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;

@Bind
@Singleton
public class FlickrAttachmentSerializer extends AbstractAttachmentSerializer
{
	@Override
	public EquellaAttachmentBean serialize(Attachment attachment)
	{
		CustomAttachment custom = (CustomAttachment) attachment;
		FlickrAttachmentBean flickrAttach = new FlickrAttachmentBean();
		flickrAttach.setAuthor((String) custom.getData(FlickrUtils.PROPERTY_AUTHOR));
		flickrAttach.setImageDimensions((String) custom.getData(FlickrUtils.PROPERTY_IMAGE_SIZE));
		flickrAttach.setDateTaken((Date) custom.getData(FlickrUtils.PROPERTY_DATE_TAKEN));
		flickrAttach.setDatePosted((Date) custom.getData(FlickrUtils.PROPERTY_DATE_POSTED));
		flickrAttach.setLicenseName((String) custom.getData(FlickrUtils.PROPERTY_LICENCE_NAME));
		flickrAttach.setThumbUrl((String) custom.getData(FlickrUtils.PROPERTY_THUMB_URL));
		flickrAttach.setViewUrl((String) custom.getData(FlickrUtils.PROPERTY_SHOW_URL));
		flickrAttach.setMediumUrl((String) custom.getData(FlickrUtils.PROPERTY_MEDIUM_URL));
		flickrAttach.setLicenseKey((String) custom.getData(FlickrUtils.PROPERTY_LICENCE_KEY));
		flickrAttach.setLicenseCode((String) custom.getData(FlickrUtils.PROPERTY_LICENCE_CODE));
		flickrAttach.setPhotoId((String) custom.getData(FlickrUtils.PROPERTY_ID));
		return flickrAttach;
	}

	@SuppressWarnings({"nls"})
	@Override
	public Map<String, Class<? extends EquellaAttachmentBean>> getAttachmentBeanTypes()
	{
		Builder<String, Class<? extends EquellaAttachmentBean>> builder = ImmutableMap.builder();
		builder.put("flickr", FlickrAttachmentBean.class);
		return builder.build();
	}

	@Override
	public String deserialize(EquellaAttachmentBean bean, ItemEditor itemEditor)
	{
		String uuid = bean.getUuid();
		FlickrAttachmentEditor attachEditor = itemEditor.getAttachmentEditor(uuid, FlickrAttachmentEditor.class);
		FlickrAttachmentBean fbean = (FlickrAttachmentBean) bean;
		attachEditor.editAuthor(fbean.getAuthor());
		attachEditor.editImageDimensions(fbean.getImageDimensions());
		attachEditor.editDateTaken(fbean.getDateTaken());
		attachEditor.editDatePosted(fbean.getDatePosted());
		attachEditor.editLicenseName(fbean.getLicenseName());
		attachEditor.editThumbUrl(fbean.getThumbUrl());
		attachEditor.editViewUrl(fbean.getViewUrl());
		attachEditor.editMediumUrl(fbean.getMediumUrl());
		attachEditor.editLicenseKey(fbean.getLicenseKey());
		attachEditor.editLicenseCode(fbean.getLicenseCode());
		attachEditor.editPhotoId(fbean.getPhotoId());
		editStandard(attachEditor, fbean);
		return uuid;
	}

	@Override
	public boolean exportable(EquellaAttachmentBean bean)
	{
		return true;
	}
}
