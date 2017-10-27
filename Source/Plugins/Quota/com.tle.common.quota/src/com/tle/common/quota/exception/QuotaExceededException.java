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

package com.tle.common.quota.exception;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.tle.common.FileSizeUtils;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class QuotaExceededException extends RuntimeApplicationException
{
	private static final long serialVersionUID = 1L;

	public QuotaExceededException(long current, long max)
	{
		super(CurrentLocale.get("exception.quotaexceeded", //$NON-NLS-1$
			FileSizeUtils.humanReadableFileSize(max), FileSizeUtils.humanReadableFileSize(current)));
	}
}
