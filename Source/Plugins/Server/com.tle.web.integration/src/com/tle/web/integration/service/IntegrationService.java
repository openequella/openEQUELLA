package com.tle.web.integration.service;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.integration.Integration;
import com.tle.web.integration.IntegrationActionInfo;
import com.tle.web.integration.IntegrationInterface;
import com.tle.web.integration.IntegrationSessionData;
import com.tle.web.integration.SingleSignonForm;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectionSession;

@SuppressWarnings("nls")
@NonNullByDefault
public interface IntegrationService
{
	String KEY_FORINTEGRATION = "$FOR$INTEG$";
	String KEY_INTEGRATION_CALLBACK = "$INTEG$RETURNER";

	@Nullable
	IntegrationInterface getIntegrationInterface(SectionInfo info);

	Integration<? extends IntegrationSessionData> getIntegrationServiceForId(String id);

	Integration<? extends IntegrationSessionData> getIntegrationServiceForData(IntegrationSessionData data);

	String setupSessionData(SectionInfo forwardInfo, IntegrationSessionData data);

	@Nullable
	IntegrationSessionData getSessionData(SectionInfo info);

	boolean isInIntegrationSession(SectionInfo info);

	void logSelections(SectionInfo info, SelectionSession session);

	IntegrationActionInfo getActionInfo(String name, @Nullable String userOptions);

	@Nullable
	IntegrationActionInfo getActionInfoForUrl(String url);

	/**
	 * Returns the code of the first matching CourseInfo against courseCodes then courseId.
	 * Returns the first non-empty courseCode or courseId if none of those matched.
	 * 
	 * @param courseId
	 * @param courseCodes
	 * @return
	 */
	@Nullable
	String getCourseInfoCode(@Nullable String courseId, String... courseCodes);

	// form param is sub-ottstimal
	void standardForward(SectionInfo info, String forward, IntegrationSessionData data, IntegrationActionInfo action,
		SingleSignonForm form);

	void checkIntegrationAllowed() throws AccessDeniedException;
}
