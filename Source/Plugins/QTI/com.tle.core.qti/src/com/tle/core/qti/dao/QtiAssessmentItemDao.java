package com.tle.core.qti.dao;

import java.util.Iterator;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.qti.entity.QtiAssessmentItem;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;

/**
 * @author aholland
 */
@NonNullByDefault
public interface QtiAssessmentItemDao extends GenericInstitutionalDao<QtiAssessmentItem, Long>
{
	QtiAssessmentItem getByUuid(String uuid);

	/**
	 * @return All questions in the current institution
	 */
	Iterator<QtiAssessmentItem> getIterator();

	void deleteAll();
}
