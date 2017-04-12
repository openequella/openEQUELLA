package com.tle.core.qti.service;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.filesystem.FileHandle;
import com.tle.core.qti.beans.QtiTestDetails;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface QtiService
{
	ResolvedAssessmentTest loadV2Test(FileHandle handle, String basePath, String relativeFilePath);

	QtiTestDetails getTestDetails(ResolvedAssessmentTest test);

	boolean isResponded(AssessmentItem assessmentItem, ItemSessionState itemState);

	TestSessionController getNewTestSessionController(ResolvedAssessmentTest test);

	TestSessionController getTestSessionController(ResolvedAssessmentTest test, TestSessionState testSessionState);
}
