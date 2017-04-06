package com.tle.web.viewitem.treeviewer;

import java.util.List;

import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.ItemNavigationNode;

public interface ModifyNavigationExtension
{
	void process(List<ItemNavigationNode> nodes, List<Attachment> nodedAttachments);
}
