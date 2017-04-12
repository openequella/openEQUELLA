package com.tle.web.sections.result.util;

import java.io.Serializable;

import com.tle.web.sections.render.Label;

public class SubstitutionLabel implements Label, Serializable
{
	private static final long serialVersionUID = 1L;

	private final Label base;
	private final String pattern;
	private final String replacement;
	private final boolean useRegex;

	public SubstitutionLabel(Label base, String pattern, String replacement)
	{
		this(base, pattern, replacement, true);
	}

	public SubstitutionLabel(Label base, String pattern, String replacement, boolean useRegex)
	{
		this.base = base;
		this.pattern = pattern;
		this.replacement = replacement;
		this.useRegex = useRegex;
	}

	@Override
	public String getText()
	{
		return useRegex ? base.getText().replaceAll(pattern, replacement) : base.getText()
			.replace(pattern, replacement);
	}

	@Override
	public boolean isHtml()
	{
		return base.isHtml();
	}
}
