package com.tle.beans.item.attachments;

import java.util.Map;

public interface IAttachment
{
	String getDescription();

	void setDescription(String description);

	String getUrl();

	void setUrl(String url);

	void setData(String name, Object value);

	Object getData(String name);

	Map<String, Object> getDataAttributesReadOnly();

	Map<String, Object> getDataAttributes();

	void setDataAttributes(Map<String, Object> data);

	AttachmentType getAttachmentType();

	String getUuid();

	void setUuid(String uuid);

	String getThumbnail();

	void setThumbnail(String thumbnail);

	String getMd5sum();

	void setMd5sum(String md5sum);

	String getViewer();

	void setViewer(String viewer);

	boolean isPreview();

	void setPreview(boolean preview);

	void setRestricted(boolean restricted);

	boolean isRestricted();
}
