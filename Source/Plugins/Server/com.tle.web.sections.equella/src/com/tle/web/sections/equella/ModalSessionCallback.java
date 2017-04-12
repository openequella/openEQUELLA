package com.tle.web.sections.equella;

import java.io.Serializable;

import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
public interface ModalSessionCallback extends Serializable
{
	void executeModalFinished(SectionInfo info, ModalSession session);
}
