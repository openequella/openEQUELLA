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

package com.tle.web.searching.section;

import com.tle.web.sections.SectionInfo;

/**
 * This subclass's sole raison d'etre is to provide a alternative for
 * createForward(..) with a skinny URL rather than a fat URL. Given its only
 * features are static, extending RootSearchSection is for reference only.
 * 
 * @author larry
 */
@SuppressWarnings("nls")
public class RootSkinnySearchSection extends RootSearchSection
{
	public static final String SKINNYSEARCHURL = "/access/skinny/searching.do";

	public static SectionInfo createForward(SectionInfo from)
	{
		return from.createForward(SKINNYSEARCHURL);
	}
}
