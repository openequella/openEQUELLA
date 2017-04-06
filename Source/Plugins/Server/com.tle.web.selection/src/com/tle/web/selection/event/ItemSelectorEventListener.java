package com.tle.web.selection.event;

import java.util.EventListener;

import com.tle.web.sections.SectionInfo;

/**
 * @author Aaron
 */
public interface ItemSelectorEventListener extends EventListener
{
	/**
	 * The function you supply must conform to: public void
	 * selectItem(SectionInfo info, ItemId itemId)
	 * 
	 * @param info
	 * @param event
	 */
	void supplyFunction(SectionInfo info, ItemSelectorEvent event);
}
