package com.tle.core.taxonomy.wizard.model;

import com.tle.web.sections.standard.model.HtmlLinkState;

public class TermSelectorTermModel
{
	private String term;
	private HtmlLinkState deleteButton;

	public TermSelectorTermModel(String term, HtmlLinkState deleteButton)
	{
		this.term = term;
		this.deleteButton = deleteButton;
	}

	public String getTerm()
	{
		return term;
	}

	public void setTerm(String term)
	{
		this.term = term;
	}

	public HtmlLinkState getDeleteButton()
	{
		return deleteButton;
	}

	public void setDeleteButton(HtmlLinkState deleteButton)
	{
		this.deleteButton = deleteButton;
	}
}
