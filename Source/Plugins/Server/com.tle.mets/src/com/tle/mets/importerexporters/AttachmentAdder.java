package com.tle.mets.importerexporters;

import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.ItemNavigationNode;

/**
 * @author Aaron
 */
public interface AttachmentAdder
{
	void addAttachment(ItemNavigationNode parentNode, Attachment attach, String nodeName);
}
