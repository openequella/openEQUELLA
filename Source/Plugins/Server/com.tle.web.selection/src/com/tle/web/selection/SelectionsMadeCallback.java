package com.tle.web.selection;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.ModalSessionCallback;

/**
 * @author aholland
 */
public interface SelectionsMadeCallback extends ModalSessionCallback
{
	/**
	 * 
	 * @param info
	 * @param session
	 * @return true if you want to maintain selected resources, otherwise false
	 */
	boolean executeSelectionsMade(SectionInfo info, SelectionSession session);
}
