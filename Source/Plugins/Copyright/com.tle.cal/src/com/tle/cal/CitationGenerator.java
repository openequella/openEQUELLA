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

package com.tle.cal;

import java.util.List;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.cal.CALSection;
import com.tle.common.Check;
import com.tle.common.Utils;
import com.tle.core.guice.Bind;

@Bind
@Singleton
public class CitationGenerator
{
	public String citeJournal(CALHolding holding, PropBagEx holdingXml)
	{
		return citeJournalPortion(holding, null, holdingXml, null);
	}

	@SuppressWarnings("nls")
	public String citeJournalPortion(CALHolding holding, CALPortion portion, PropBagEx holdingXml, PropBagEx portionXml)
	{
		String holdingTitle = createTitleString(holding.getTitle());
		String pubYear = getHoldingYear(holding);
		String volume = holding.getVolume();
		if( !Check.isEmpty(volume) )
		{
			volume = "vol. " + volume;
		}
		String issue = holdingXml.getNode("issue/value");
		String issueType = holdingXml.getNode("issue/type");

		if( issueType.equals("number") )
		{
			issue = "no. " + issue;
		}
		StringBuilder sbuf = new StringBuilder();
		boolean doneYear = false;
		if( portion != null )
		{
			String portionAuthors = createAuthorList(portion.getAuthors());
			boolean noTitle = Check.isEmpty(portion.getTitle());
			String portionTitle = "";
			if( !noTitle )
			{
				portionTitle = Utils.ent(portion.getTitle());
			}
			if( !Check.isEmpty(portionAuthors) )
			{
				sbuf.append(portionAuthors);
				sbuf.append(' ');
				sbuf.append(pubYear);
				addField(sbuf, portionTitle);
				doneYear = true;
			}
			else
			{
				sbuf.append(portionTitle);
				sbuf.append(' ');
				sbuf.append(pubYear);
				doneYear = true;
			}
		}
		addField(sbuf, holdingTitle);
		if( !doneYear )
		{
			sbuf.append(' ');
			sbuf.append(pubYear);
		}
		addField(sbuf, volume);
		addField(sbuf, issue);
		addRange(sbuf, portion);
		if( sbuf.charAt(sbuf.length() - 1) != '.' )
		{
			sbuf.append('.');
		}
		return sbuf.toString();
	}

	public String citeBook(CALHolding holding, PropBagEx holdingXml)
	{
		return citeBookPortion(holding, null, holdingXml, null);
	}

	@SuppressWarnings("nls")
	public String citeBookPortion(CALHolding holding, CALPortion portion, PropBagEx holdingXml, PropBagEx portionXml)
	{
		String conferenceName = holdingXml.getNode("conference/name");
		String conferenceLocation = holdingXml.getNode("conference/location");
		String conferenceYear = holdingXml.getNode("conference/year");
		String authorList = createAuthorList(holding.getAuthors());
		if( authorList.isEmpty() )
		{
			List<String> editors = holdingXml.getNodeList("editors/editor");
			authorList = createAuthorList(editors);
			if( editors.size() == 1 )
			{
				authorList += " (ed.)";
			}
			else if( editors.size() > 1 )
			{
				authorList += " (eds.)";
			}
		}
		String holdingTitle = createTitleString(holding.getTitle());
		String publisher = holding.getPublisher();
		String edition = holdingXml.getNode("edition");
		String pubPlace = holdingXml.getNode("publication/place");
		String pubYear = getHoldingYear(holding);
		StringBuilder sbuf = new StringBuilder();
		boolean hasAuthors = !authorList.isEmpty();
		boolean doneTitle = false;
		boolean doneDate = false;
		boolean isConference = !Check.isEmpty(conferenceName) && Check.isEmpty(publisher);
		if( portion != null )
		{
			String portionAuthors = createAuthorList(portion.getAuthors());
			boolean noTitle = Check.isEmpty(portion.getTitle());
			String portionTitle = "";
			if( !noTitle )
			{
				portionTitle = "'" + Utils.ent(portion.getTitle()) + "'";
			}
			if( !Check.isEmpty(portionAuthors) )
			{
				sbuf.append(portionAuthors);
				sbuf.append(' ');
				sbuf.append(pubYear);
				addField(sbuf, portionTitle);
				doneDate = true;
			}
			else if( !noTitle )
			{
				sbuf.append(portionTitle);
				sbuf.append(' ');
				sbuf.append(pubYear);
				doneDate = true;
			}
			if( doneDate )
			{
				sbuf.append(" in ");
			}

		}
		if( hasAuthors )
		{
			sbuf.append(authorList);
			isConference = false;
		}
		else if( isConference )
		{
			sbuf.append(Utils.ent(conferenceName));
			doneTitle = true;
		}
		else
		{
			sbuf.append(holdingTitle);
			doneTitle = true;
		}
		if( !doneDate )
		{
			sbuf.append(' ');
			sbuf.append(pubYear);
		}
		if( !doneTitle )
		{
			addField(sbuf, holdingTitle);
		}
		addField(sbuf, edition, true);
		addField(sbuf, publisher, true);
		if( !isConference && Check.isEmpty(pubPlace) )
		{
			pubPlace = "n.p.";
		}
		addField(sbuf, pubPlace, true);
		addRange(sbuf, portion);
		if( isConference )
		{
			addField(sbuf, conferenceLocation, true);
			addField(sbuf, conferenceYear, true);
		}
		if( sbuf.charAt(sbuf.length() - 1) != '.' )
		{
			sbuf.append('.');
		}
		return sbuf.toString();
	}

	@SuppressWarnings("nls")
	private void addRange(StringBuilder sbuf, CALPortion portion)
	{
		if( portion != null && !Check.isEmpty(portion.getSections()) )
		{
			CALSection section = portion.getCALSections().get(0);
			String range = section.getRange();
			addField(sbuf, "pp. " + range, true);
		}
	}

	@SuppressWarnings("nls")
	private String getHoldingYear(CALHolding holding)
	{
		String pubDate = holding.getPubDate();
		if( Check.isEmpty(pubDate) )
		{
			return "n.d.";
		}
		return pubDate;
	}

	@SuppressWarnings("nls")
	private void addField(StringBuilder sbuf, String field, boolean encode)
	{
		if( !Check.isEmpty(field) )
		{
			if( sbuf.length() > 0 )
			{
				sbuf.append(", ");
			}
			if( encode )
			{
				field = Utils.ent(field);
			}
			sbuf.append(field);
		}
	}

	private void addField(StringBuilder sbuf, String field)
	{
		addField(sbuf, field, false);
	}

	@SuppressWarnings("nls")
	private String createTitleString(String title)
	{
		return "<b><i>" + Utils.ent(title) + "</i></b>";
	}

	@SuppressWarnings("nls")
	private String createAuthorList(List<String> authors)
	{
		if( Check.isEmpty(authors) )
		{
			return "";
		}
		StringBuilder sbuf = new StringBuilder();
		int i = 0;
		for( String author : authors )
		{
			boolean last = i == authors.size() - 1;
			if( i > 0 && !last )
			{
				sbuf.append(", ");
			}
			else if( last && i > 0 )
			{
				sbuf.append(" &amp; ");
			}
			sbuf.append(Utils.ent(author));
			i++;
		}
		return sbuf.toString();
	}

}
