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

package com.tle.freetext;

import org.apache.lucene.util.Version;

public final class LuceneConstants
{
	/**
	 * As most Lucene methods now require a version to be passed in and
	 * Version.LUCENE_CURRENT is now deprecated this allows us to change the
	 * version in one simple location
	 */
	public static final Version LATEST_VERSION = Version.LUCENE_35;

	// Noli me tangere constructor, because Sonar likes it that way for
	// non-instantiated utility classes
	protected LuceneConstants()
	{
		// not to be instantiated, hence nothing to construct except a token
		// hidden constructor to silence Sonar
	}
}
