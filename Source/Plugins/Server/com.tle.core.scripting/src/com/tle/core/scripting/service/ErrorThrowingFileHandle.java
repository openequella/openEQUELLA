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

package com.tle.core.scripting.service;

import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.i18n.CurrentLocale;

/**
 * When the staging file is not available, this object is injected in instead so
 * that a descriptive error is thrown that tells the user to use if
 * (staging.isAvailable()) { blah } in their scripts
 * 
 * @author aholland
 */
@SuppressWarnings("nls")
public class ErrorThrowingFileHandle implements FileHandle
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getAbsolutePath()
	{
		throw new RuntimeException(CurrentLocale.get("com.tle.core.scripting.error.nostaging"));
	}

	@Override
	public String getMyPathComponent()
	{
		throw new RuntimeException(CurrentLocale.get("com.tle.core.scripting.error.nostaging"));
	}

	@Override
	public String getFilestoreId()
	{
		return null;
	}
}