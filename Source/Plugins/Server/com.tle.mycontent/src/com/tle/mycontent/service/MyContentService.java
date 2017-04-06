package com.tle.mycontent.service;

import java.io.InputStream;
import java.util.Set;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.core.workflow.operations.WorkflowOperation;
import com.tle.mycontent.ContentHandler;
import com.tle.mycontent.web.section.ContributeMyContentAction;
import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
public interface MyContentService
{
	boolean isMyContentContributionAllowed();

	ItemDefinition getMyContentItemDef();

	boolean isMyContentItem(Item item);

	/**
	 * @param info
	 * @return Are we able to return?
	 */
	boolean returnFromContribute(SectionInfo info);

	Set<String> getContentHandlerIds();

	String getContentHandlerNameKey(String handlerId);

	ContentHandler getHandlerForId(String handlerId);

	WorkflowOperation getEditOperation(MyContentFields fields, String filename, InputStream inputStream,
		String stagingUuid, boolean removeExistingAttachments, boolean useExistingAttachment);

	MyContentFields getFieldsForItem(ItemId itemId);

	void delete(ItemId itemId);

	void forwardToEditor(SectionInfo info, ItemId itemId);

	ContributeMyContentAction createActionForHandler(String handlerId);

	void forwardToContribute(SectionInfo info, String handlerId);

	void restore(ItemId itemId);

}
