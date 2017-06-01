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

package com.tle.web.hierarchy;

import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.SubstitutionLabel;

/**
 * for the buildTopicId method, @see com.tle.common.hierarchy.VirtualTopicUtils#buildTopicId()
 */
public final class TopicUtils
{
	@SuppressWarnings("nls")
	public static Label labelForValue(Label label, String value)
	{
		return value == null ? label : new SubstitutionLabel(label, "%s", value, false);
	}


	private TopicUtils()
	{
		throw new Error();
	}
}
