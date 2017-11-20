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

package com.tle.common.taxonomy;

@SuppressWarnings("nls")
public final class TaxonomyConstants
{
	public static final String TERM_SEPARATOR = "\\";
	public static final String TERM_SEPARATOR_REGEX = "\\\\";

	public static final String TERM_ALLOW_ADDITION = "TERM_ALLOW_ADDITION";

	public static final String INTERNAL_DATASOURCE = "internalTaxonomyDataSource";

	public static final String PRE41_TAXONOMY_EXPORT_FOLDER = "taxonomy";
	public static final String TAXONOMY_EXPORT_FOLDER = "taxonomy2";
	public static final String SINGLE_TAXONOMIES_FILE = "taxonomies.xml";

	public static final String TERMS_EXPORT_FOLDER = "terms";

	private TaxonomyConstants()
	{
		throw new Error();
	}
}
