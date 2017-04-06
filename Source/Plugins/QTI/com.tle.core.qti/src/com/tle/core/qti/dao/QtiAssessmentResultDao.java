package com.tle.core.qti.dao;

import java.util.Iterator;
import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.qti.entity.QtiAssessmentResult;
import com.tle.common.qti.entity.QtiAssessmentTest;
import com.tle.core.hibernate.dao.GenericDao;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface QtiAssessmentResultDao extends GenericDao<QtiAssessmentResult, Long>
{
	List<QtiAssessmentResult> findByAssessmentTest(QtiAssessmentTest test);

	/**
	 * Only counts _complete_ attempts
	 * 
	 * @param test
	 * @param resourceLinkId
	 * @param userId
	 * @param toolConsumerInstanceGuid
	 * @return
	 */
	int countAttemptsByResourceLink(QtiAssessmentTest test, String resourceLinkId, String userId,
		String toolConsumerInstanceGuid);

	/**
	 * In the case of multiple attempts you will receive a list of results. Any
	 * non-submitted sessionStatus result should be continued.
	 * 
	 * @param test
	 * @param resourceLinkId
	 * @param userId
	 * @param toolConsumerInstanceGuid
	 * @return
	 */
	List<QtiAssessmentResult> findByResourceLink(QtiAssessmentTest test, String resourceLinkId, String userId,
		String toolConsumerInstanceGuid);

	QtiAssessmentResult getCurrentByResourceLink(QtiAssessmentTest test, String resourceLinkId, String userId,
		String toolConsumerInstanceGuid);

	Iterator<QtiAssessmentResult> getIterator();

	void deleteAll();
}
