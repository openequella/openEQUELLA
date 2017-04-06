package com.tle.core.item.edit.impl;

import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractCustomAttachmentEditor;
import com.tle.core.item.edit.attachment.ScormResourceAttachmentEditor;

@Bind
public class ScormResourceAttachmentEditorImpl extends AbstractCustomAttachmentEditor
	implements
		ScormResourceAttachmentEditor
{

	@Override
	public void editFilename(String filename)
	{
		if( hasBeenEdited(attachment.getUrl(), filename) )
		{
			attachment.setUrl(filename);
		}
	}

	@Override
	public String getCustomType()
	{
		return "scormres"; //$NON-NLS-1$
	}

}
