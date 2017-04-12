package com.tle.core.remoterepo.z3950;

import com.tle.core.fedsearch.RemoteRepoSearchResult;

/**
 * @author aholland
 */
public class Z3950SearchResult extends RemoteRepoSearchResult
{
	private static final long serialVersionUID = 1L;

	private String isbn;
	private String issn;
	private String edition;
	private String publisher;
	private String publishDate;
	private String lccn;

	public Z3950SearchResult(int index)
	{
		super(index);
	}

	public String getIsbn()
	{
		return isbn;
	}

	public void setIsbn(String isbn)
	{
		this.isbn = isbn;
	}

	public String getIssn()
	{
		return issn;
	}

	public void setIssn(String issn)
	{
		this.issn = issn;
	}

	public String getLccn()
	{
		return lccn;
	}

	public void setLccn(String lccn)
	{
		this.lccn = lccn;
	}

	public String getEdition()
	{
		return edition;
	}

	public void setEdition(String edition)
	{
		this.edition = edition;
	}

	public String getPublisher()
	{
		return publisher;
	}

	public void setPublisher(String publisher)
	{
		this.publisher = publisher;
	}

	public String getPublishDate()
	{
		return publishDate;
	}

	public void setPublishDate(String pubblishDate)
	{
		this.publishDate = pubblishDate;
	}

	public static long getSerialversionuid()
	{
		return serialVersionUID;
	}
}
