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

package com.tle.core.cloud;

import com.tle.annotation.NonNullByDefault;

@SuppressWarnings("nls")
@NonNullByDefault
public final class CloudConstants
{
	public static final String LANGUAGE_PATH = "/oer/dc/language";
	public static final String LICENCE_PATH = "/oer/eq/license_type";
	public static final String PUBLISHER_PATH = "/oer/dc/publisher";
	public static final String EDUCATION_LEVEL_PATH = "/oer/dc/terms/educationLevel";
	public static final String FORMAT_PATH = "/oer/dc/format";

	public static final String ITEM_EXTENSION = "cloud";

	public CloudConstants()
	{
		throw new Error();
	}
}
