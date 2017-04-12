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
