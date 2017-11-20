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

package com.tle.core.fedsearch.impl;

import java.util.Collection;

import com.dytech.devlib.PropBagEx;
import com.tle.core.fedsearch.GenericRecord;

/**
 * @author aholland
 */
public class BasicRecord implements GenericRecord
{
	private PropBagEx xml;
	private String title;
	private String description;
	private String isbn;
	private String issn;
	private String lccn;
	private String uri;
	private String url;
	private Collection<String> authors;
	private String physicalDescription;

	@Override
	public PropBagEx getXml()
	{
		return xml;
	}

	public void setXml(PropBagEx xml)
	{
		this.xml = xml;
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@Override
	public String getIsbn()
	{
		return isbn;
	}

	public void setIsbn(String isbn)
	{
		this.isbn = isbn;
	}

	@Override
	public String getIssn()
	{
		return issn;
	}

	public void setIssn(String issn)
	{
		this.issn = issn;
	}

	@Override
	public String getLccn()
	{
		return lccn;
	}

	public void setLccn(String lccn)
	{
		this.lccn = lccn;
	}

	@Override
	public String getUri()
	{
		return uri;
	}

	public void setUri(String uri)
	{
		this.uri = uri;
	}

	@Override
	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	@Override
	public Collection<String> getAuthors()
	{
		return authors;
	}

	public void setAuthors(Collection<String> authors)
	{
		this.authors = authors;
	}

	@Override
	public String getPhysicalDescription()
	{
		return physicalDescription;
	}

	public void setPhysicalDescription(String physicalDescription)
	{
		this.physicalDescription = physicalDescription;
	}

	@Override
	public String getType()
	{
		return "basic"; //$NON-NLS-1$
	}
}
