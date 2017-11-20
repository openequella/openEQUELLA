/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
