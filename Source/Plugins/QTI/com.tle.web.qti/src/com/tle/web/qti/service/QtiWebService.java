package com.tle.web.qti.service;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.qti.beans.QtiTestDetails;
import com.tle.web.lti.LtiData;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewurl.ViewItemResource;

@NonNullByDefault
public interface QtiWebService
{
	void startTest(SectionInfo info, ViewItemResource resource);

	void cancelTest(SectionInfo info, ViewItemResource resource);

	void submitTest(SectionInfo info, ViewItemResource resource);

	void readFormValues(SectionInfo info, ViewItemResource resource, boolean persist);

	/**
	 * @param info
	 * @param resource
	 * @param key Specifiy null to use direction
	 * @param direction -1 == left, 0 == summary page, 1 == next
	 */
	void selectQuestion(SectionInfo info, ViewItemResource resource, @Nullable String key, int direction);

	ResolvedAssessmentTest getResolvedTest(SectionInfo info, ViewItemResource resource);

	QtiTestDetails getTestDetails(ResolvedAssessmentTest test);

	TestSessionState getTestSessionState(SectionInfo info, ViewItemResource resource);

	TestSessionController getTestSessionController(SectionInfo info, ViewItemResource resource);

	@Nullable
	LtiData getLtiData();

	boolean isResponded(AssessmentItem assessmentItem, ItemSessionState itemSessionState);
}
