/*
 * Created on Jun 10, 2004 For "The Learning Edge"
 */
package com.tle.web.wizard;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;

/**
 * @author jmaginnis
 */
public interface WizardExceptionHandler
{
	SectionResult handleException(SectionInfo info, Throwable cause);
}
