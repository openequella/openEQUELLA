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

package com.tle.web.controls.universal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class AttachmentHandlerUtils
{
	//@formatter:off
	@SuppressWarnings("nls")
	public static final List<String> RATING_CLASSES = Collections.unmodifiableList(Arrays.asList(
		"zero", "one", "two", "three", "four", "five"));
	//@formatter:on

	public static int getRating(float avgRating)
	{
		int rating = (int) avgRating;
		if( rating < 0 )
		{
			return 0;
		}
		if( rating > 5 )
		{
			return 5;
		}
		return rating;
	}

	private AttachmentHandlerUtils()
	{
		throw new Error();
	}
}
