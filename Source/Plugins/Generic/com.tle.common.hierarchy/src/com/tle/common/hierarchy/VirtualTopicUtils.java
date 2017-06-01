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

package com.tle.common.hierarchy;

import java.util.Map;

import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.common.Check;
import com.tle.common.URLUtils;

/**
 * @author larry
 * Culled form TopicUtils, moved to more widely accessible common namespace
 */
public class VirtualTopicUtils
{
	public static String buildTopicId(HierarchyTopic topic, String value, Map<String, String> parentValues)
	{
		// Short-cut!
		if( value == null && Check.isEmpty(parentValues) )
		{
			return topic.getUuid();
		}

		StringBuilder rv = new StringBuilder();
		rv.append(topic.getUuid());
		if( value != null )
		{
			rv.append(':');
			rv.append(URLUtils.basicUrlEncode(value));
		}

		if( Check.isEmpty(parentValues) )
		{
			return rv.toString();
		}

		topic = topic.getParent();
		while( topic != null )
		{
			String u = topic.getUuid();
			String v = parentValues.get(u);

			if( !Check.isEmpty(v) )
			{
				rv.append(',');
				rv.append(u);
				rv.append(':');
				rv.append(URLUtils.basicUrlEncode(v));
			}

			topic = topic.getParent();
		}

		return rv.toString();
	}

	private VirtualTopicUtils()
	{
		throw new Error();
	}
}
