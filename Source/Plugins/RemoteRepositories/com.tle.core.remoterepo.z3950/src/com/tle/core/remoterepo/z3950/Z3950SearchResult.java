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
