package com.tle.core.qti.service;

import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

import com.dytech.edge.exceptions.InvalidDataException;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.qti.entity.QtiAssessmentItem;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface QtiAssessmentItemService
{
	/**
	 * @param test
	 * @param question
	 * @return null if the question cannot be looked up
	 */
	@Nullable
	QtiAssessmentItem convertItemToEntity(ResolvedAssessmentTest test, ResolvedAssessmentItem question);

	void save(QtiAssessmentItem item) throws InvalidDataException;

}
