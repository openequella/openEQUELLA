package com.tle.web.sections.result.util;

import java.io.Serializable;
import java.util.Collection;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.render.Label;

public class ListLabel implements Label, Serializable
{
	private static final long serialVersionUID = 1L;

	private final String text;

	public ListLabel(Collection<String> list, String separator)
	{
		text = Joiner.on(separator).join(Collections2.transform(list, new Function<String, String>()
		{
			@Override
			public String apply(String input)
			{
				return SectionUtils.ent(input);
			}

		}));
	}

	@Override
	public String getText()
	{
		return text;
	}

	@Override
	public boolean isHtml()
	{
		return true;
	}
}
