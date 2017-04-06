/**
 * 
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
