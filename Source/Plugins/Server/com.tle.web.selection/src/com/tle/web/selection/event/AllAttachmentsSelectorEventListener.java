package com.tle.web.selection.event;

import java.util.EventListener;

import com.tle.web.sections.SectionInfo;

public interface AllAttachmentsSelectorEventListener extends EventListener
{
	/**
	 * The function you supply must conform to: public void
	 * selectAllAttachments(SectionInfo info, List<String> uuids, ItemId itemId)
	 * 
	 * @param info
	 * @param event
	 */
	void supplyFunction(SectionInfo info, AllAttachmentsSelectorEvent event);
}
