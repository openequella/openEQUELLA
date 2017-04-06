package com.tle.core.util.ims.extension;

import java.util.List;

import com.tle.beans.filesystem.FileHandle;
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
