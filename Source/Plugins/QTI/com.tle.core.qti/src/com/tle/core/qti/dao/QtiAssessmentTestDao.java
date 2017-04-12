package com.tle.core.qti.dao;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.common.qti.entity.QtiAssessmentTest;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;

/**
 * @author aholland
 */
@NonNullByDefault
public interface QtiAssessmentTestDao extends GenericInstitutionalDao<QtiAssessmentTest, Long>
{
	/**
	 * Lookup a test from it's uuid. Will blow up if not found.
	 * 
	 * @param uuid
	 * @return
	 */
	QtiAssessmentTest getByUuid(String uuid);

	/**
	 * Same as getByUuid but returns null if not found
	 * 
	 * @param uuid
	 * @return null if not found
	 */
	@Nullable
	QtiAssessmentTest findByUuid(String uuid);

	@Nullable
	QtiAssessmentTest findByItem(Item item);

	@Nullable
	QtiAssessmentTest findByItemId(long itemId);

	void deleteAll();
}
