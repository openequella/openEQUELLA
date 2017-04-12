package com.tle.qti;

import java.util.List;

import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.IMSResourceAttachment;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.core.guice.Bind;
import com.tle.core.util.ims.beans.IMSResource;
import com.tle.core.util.ims.extension.IMSAttachmentExporter;

@Bind
public class QTIAttachmentExporter implements IMSAttachmentExporter
{
	@Override
	public boolean exportAttachment(Item item, IAttachment attachment, List<IMSResource> resources, FileHandle imsRoot)
	{
		return false;
	}

	@Override
	public Attachment importAttachment(Item item, IMSResource resource, FileHandle root, String packageFolder)
	{
		String type = resource.getType();

		if( !Check.isEmpty(type) && type.startsWith("imsqti_xml") ) //$NON-NLS-1$
		{
			Attachment fattach = null;
			String url = resource.getFullHref();
			fattach = new IMSResourceAttachment();
			fattach.setUrl(URLUtils.isAbsoluteUrl(url) ? url : packageFolder + url);
			fattach.setDescription(url);
			fattach.setViewer("qti"); //$NON-NLS-1$
			return fattach;
		}
		return null;
	}

}
