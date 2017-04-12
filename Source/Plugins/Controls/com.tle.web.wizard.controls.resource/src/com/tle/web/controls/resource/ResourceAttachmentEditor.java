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
