package com.tle.core.qti.service;

import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

import com.dytech.edge.exceptions.InvalidDataException;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.common.qti.entity.QtiAssessmentTest;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface QtiAssessmentTestService
{
	QtiAssessmentTest convertTestToEntity(ResolvedAssessmentTest test, Item item, String xmlPath, String testUuid);

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

	void validate(QtiAssessmentTest test) throws InvalidDataException;

	void save(QtiAssessmentTest test) throws InvalidDataException;

	void deleteForItemId(long itemId);

	void delete(QtiAssessmentTest test);
}
