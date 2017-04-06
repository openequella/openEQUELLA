package com.tle.web.institution.database;

import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.renderers.SpanRenderer;

public class MigrationRow
{
	private final int percent;
	private final String id;
	private final SpanRenderer label;
	private final HtmlComponentState errorLink;

	public MigrationRow(int percent, String id, SpanRenderer label, HtmlComponentState errorLink)
	{
		this.percent = percent;
		this.id = id;
		this.label = label;
		this.errorLink = errorLink;
	}

	public int getPercent()
	{
		return percent;
	}

	public String getId()
	{
		return id;
	}

	public SpanRenderer getLabel()
	{
		return label;
	}

	public HtmlComponentState getErrorLink()
	{
		return errorLink;
	}
}