package com.tle.web.selection.event;

import java.util.EventListener;

import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectAttachmentHandler;

/**
 * @author Aaron
 */
public interface AttachmentSelectorEventListener extends EventListener, SelectAttachmentHandler
{
	/**
	 * The function you supply must conform to: public void
	 * selectAttachment(SectionInfo info, String uuid, ItemId itemId, String
	 * extensionType)
	 * 
	 * @param info
	 * @param event
	 */
	void supplyFunction(SectionInfo info, AttachmentSelectorEvent event);
}
