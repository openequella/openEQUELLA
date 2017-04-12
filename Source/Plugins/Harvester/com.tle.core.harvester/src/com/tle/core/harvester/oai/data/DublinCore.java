/*
 * Created on Apr 12, 2005
 */

package com.tle.core.harvester.oai.data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 */
public class DublinCore
{
	private Collection<String> creator;
	private Collection<String> title;
	private Collection<String> subject;
	private Collection<String> date;
	private Collection<String> description;
	private Collection<String> identifier;

	private Collection<String> getCollection(Collection<String> blah)
	{
		if( blah == null )
		{
			blah = new ArrayList<String>(1);
		}
		return blah;
	}

	public Collection<String> getCreator()
	{
		creator = getCollection(creator);
		return creator;
	}

	public void addCreator(String creator2)
	{
		getCreator().add(creator2);
	}

	public Collection<String> getDate()
	{
		date = getCollection(date);
		return date;
	}

	public void addDate(String date2)
	{
		getDate().add(date2);
	}

	public Collection<String> getDescription()
	{
		description = getCollection(description);
		return description;
	}

	public void addDescription(String description2)
	{
		getDescription().add(description2);
	}

	public Collection<String> getSubject()
	{
		subject = getCollection(subject);
		return subject;
	}

	public void addSubject(String subject2)
	{
		getSubject().add(subject2);
	}

	public Collection<String> getTitle()
	{
		title = getCollection(title);
		return title;
	}

	public void addTitle(String title2)
	{
		getTitle().add(title2);
	}

	public Collection<String> getIdentifier()
	{
		identifier = getCollection(identifier);
		return identifier;
	}

	public void addIdentifier(String ident)
	{
		getIdentifier().add(ident);
	}
}
