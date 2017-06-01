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
