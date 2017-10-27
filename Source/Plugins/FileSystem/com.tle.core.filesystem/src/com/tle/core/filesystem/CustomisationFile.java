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

package com.tle.core.filesystem;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.Institution;
import com.tle.common.PathUtils;
import com.tle.common.institution.CurrentInstitution;

@NonNullByDefault
public class CustomisationFile extends InstitutionFile
{
	private static final long serialVersionUID = 1L;

	private static final String CUSTOM2_FOLDER = "Custom2";

	/**
	 * References the current customisation for the given institution.
	 */
	public CustomisationFile()
	{
		this(CurrentInstitution.get());
	}

	public CustomisationFile(Institution inst)
	{
		super(inst);
	}

	@Override
	protected String createAbsolutePath()
	{
		return PathUtils.filePath(super.createAbsolutePath(), CUSTOM2_FOLDER);
	}
}
