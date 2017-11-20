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

package com.tle.core.quota.service;

import java.util.Collection;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.Institution;
import com.tle.beans.item.Item;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.quota.exception.QuotaExceededException;

/**
 * @author Charles O'Farrell
 */
@NonNullByDefault
public interface QuotaService
{
	long checkQuotaAndReturnNewItemSize(Item item, StagingFile stagingFile1) throws QuotaExceededException;

	long getFileSize(FileHandle file);

	long getInstitutionalConsumption(Institution inst);

	Collection<Institution> getInstitutionsWithFilestoreLimits();

	boolean isInstitutionOverLimit(Institution inst);

	void refreshCache(Institution inst);
}
