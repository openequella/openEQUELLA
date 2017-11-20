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

package com.tle.core.util.ims.extension;

import java.util.List;

import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.core.util.ims.beans.IMSResource;

/**
 * @author Aaron
 */
public interface IMSAttachmentExporter
{
	/**
	 * @param info Would be nice if we didn't need this. Required for generating
	 *            URLs
	 * @param item
	 * @param attachment
	 * @param resources A list of current IMSResources. You need to add the
	 *            exported attachment to this list
	 * @return true if the attachment was handled
	 */
	boolean exportAttachment(Item item, IAttachment attachment, List<IMSResource> resources, FileHandle imsRoot);

	/**
	 * @return null if unhandled
	 */
	Attachment importAttachment(Item item, IMSResource resource, FileHandle root, String packageFolder);
}
