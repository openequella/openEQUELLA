package com.tle.core.taxonomy;

public class TermResult
{
	private String term;
	private String fullTerm;
	private String uuid;
	private boolean leaf = false;

	public TermResult()
	{
		super();
	}

	public TermResult(String term, String fullTerm)
	{
		this(term, fullTerm, false);
	}

	public TermResult(String term, String fullterm, boolean leaf, String uuid)
	{
		this.term = term;
		this.fullTerm = fullterm;
		this.leaf = leaf;
		this.setUuid(uuid);

	}

	public TermResult(String term, String fullTerm, boolean leaf)
	{
		this(term, fullTerm, leaf, null);
	}

	public String getTerm()
	{
		return term;
	}

	public void setTerm(String term)
	{
		this.term = term;
	}

	public String getFullTerm()
	{
		return fullTerm;
	}

	public void setFullTerm(String fullTerm)
	{
		this.fullTerm = fullTerm;
	}

	public boolean isLeaf()
	{
		return leaf;
	}

	public void setLeaf(boolean leaf)
	{
		this.leaf = leaf;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}
}
