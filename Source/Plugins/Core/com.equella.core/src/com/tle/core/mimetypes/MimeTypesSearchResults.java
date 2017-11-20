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

package com.tle.core.mimetypes;

import java.util.List;

import com.tle.beans.mime.MimeEntry;
import com.tle.common.searching.SimpleSearchResults;

/**
 * @author aholland
 */
public class MimeTypesSearchResults extends SimpleSearchResults<MimeEntry>
{
	private static final long serialVersionUID = 1L;

	public MimeTypesSearchResults(List<MimeEntry> results, int offset, int available)
	{
		super(results, results.size(), offset, available);
	}
}
