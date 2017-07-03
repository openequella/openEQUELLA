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

package com.tle.beans.item;

public enum VersionSelection
{
	FORCE_LATEST, FORCE_CURRENT, DEFAULT_TO_LATEST, DEFAULT_TO_CURRENT,

	// The following value is deprecated as it's not really valid. For courses
	// that currently store this in the database, they should really be using
	// "null" instead to indicate that the course doesn't have a preference.
	@Deprecated
	INSTITUTION_DEFAULT;
}
