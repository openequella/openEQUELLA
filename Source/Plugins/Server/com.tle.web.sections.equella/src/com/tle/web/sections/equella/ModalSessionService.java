package com.tle.web.sections.equella;

import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
public interface ModalSessionService
{
	SectionInfo createForward(SectionInfo original, String path, ModalSessionCallback finished);

	/**
	 * Use createForward in preference to setupModalSession.
	 * 
	 * @param info
	 * @param session
	 */
	void setupModalSession(SectionInfo info, ModalSession session);

	void returnFromSession(SectionInfo info);
}