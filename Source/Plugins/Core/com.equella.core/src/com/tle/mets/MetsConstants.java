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

package com.tle.mets;

public final class MetsConstants
{
	/**
	 * The name of the METS record file when inside a ZIP package
	 */
	public static final String METS_FILENAME = "mets-manifest.xml"; //$NON-NLS-1$

	private MetsConstants()
	{
		throw new Error();
	}
}
