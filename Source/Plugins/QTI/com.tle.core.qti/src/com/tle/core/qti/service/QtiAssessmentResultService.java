package com.tle.core.qti.service;

import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.qti.entity.QtiAssessmentResult;
import com.tle.common.qti.entity.QtiAssessmentTest;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface QtiAssessmentResultService
{
	@Nullable
	QtiAssessmentResult getAssessmentResult(QtiAssessmentTest test, String resourceLinkId, String userId,
		String toolConsumerInstanceGuid);

	QtiAssessmentResult ensureAssessmentResult(QtiAssessmentTest test, String resourceLinkId, String userId,
		String toolConsumerInstanceGuid);

	AssessmentResult persistTestSessionState(QtiAssessmentTest test, TestSessionController testSessionController,
		QtiAssessmentResult qtiAssessmentResult);

	TestSessionController loadTestSessionState(ResolvedAssessmentTest resolvedAssessmentTest,
		@Nullable QtiAssessmentResult qtiAssessmentResult);

	AssessmentResult computeAssessmentResult(TestSessionController testSessionController);

	int countAttemptsByResourceLink(QtiAssessmentTest test, String resourceLinkId, String userId,
		String toolConsumerInstanceGuid);

}
