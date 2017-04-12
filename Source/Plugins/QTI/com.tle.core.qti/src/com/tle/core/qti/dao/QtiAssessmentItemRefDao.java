package com.tle.core.qti.dao;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.qti.entity.QtiAssessmentItemRef;
import com.tle.common.qti.entity.QtiAssessmentTest;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;

/**
 * @author aholland
 */
@NonNullByDefault
public interface QtiAssessmentItemRefDao extends GenericInstitutionalDao<QtiAssessmentItemRef, Long>
{
	QtiAssessmentItemRef getByUuid(String uuid);

	QtiAssessmentItemRef getByIdentifier(QtiAssessmentTest test, String identifier);
}
