/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.qti.service;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.common.qti.entity.QtiAssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

@NonNullByDefault
public interface QtiAssessmentTestService {
  QtiAssessmentTest convertTestToEntity(
      ResolvedAssessmentTest test, Item item, String xmlPath, String testUuid);

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
