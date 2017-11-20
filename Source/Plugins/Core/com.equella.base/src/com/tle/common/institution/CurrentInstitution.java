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

package com.tle.common.institution;

import com.tle.beans.Institution;

/**
 * @author Nicholas Read
 */
public final class CurrentInstitution
{
	private static final ThreadLocal<Institution> local = new ThreadLocal<Institution>();

	public static Institution get()
	{
		return local.get();
	}

	public static void set(Institution institution)
	{
		local.set(institution);
	}

	public static void remove()
	{
		local.remove();
	}

	private CurrentInstitution()
	{
		throw new Error();
	}
}
