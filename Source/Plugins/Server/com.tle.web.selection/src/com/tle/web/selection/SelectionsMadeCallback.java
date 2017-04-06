package com.tle.web.selection;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.ModalSessionCallback;

/**
 * @author aholland
 */
public interface SelectionsMadeCallback extends ModalSessionCallback
{
	void executeSelectionsMade(SectionInfo info, SelectionSession session);
}
