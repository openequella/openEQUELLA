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

package com.tle.web.controls.resource;

import com.tle.beans.item.ItemKey;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractCustomAttachmentEditor;

@Bind
public class ResourceAttachmentEditor extends AbstractCustomAttachmentEditor
{
	@Override
	public String getCustomType()
	{
		return ResourceHandler.TYPE_RESOURCE;
	}

	public void editItemId(ItemKey itemKey)
	{
		editCustomData(ResourceHandler.DATA_UUID, itemKey.getUuid());
		editCustomData(ResourceHandler.DATA_VERSION, itemKey.getVersion());
	}

	public void editType(char type)
	{
		editCustomData(ResourceHandler.DATA_TYPE, String.valueOf(type));
	}

	public void editAttachmentUuid(String uuid)
	{
		if( hasBeenEdited(customAttachment.getUrl(), uuid) )
		{
			customAttachment.setUrl(uuid);
		}
	}

	public void editPath(String path)
	{
		if( hasBeenEdited(customAttachment.getUrl(), path) )
		{
			customAttachment.setUrl(path);
		}
	}

}
