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
