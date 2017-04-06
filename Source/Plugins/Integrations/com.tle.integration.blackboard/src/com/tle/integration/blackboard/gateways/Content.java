/*
 * Created on Aug 12, 2004
 */
package com.tle.integration.blackboard.gateways;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.dytech.devlib.PropBagEx;

/**
 * @author cofarrel
 */
@SuppressWarnings("nls")
public class Content
{
	public static final String NODE = "content";

	protected String course;
	protected String content;
	protected String user;
	protected boolean notify;
	protected String name;
	protected String html;

	public Content()
	{
		course = "";
		content = "";
		user = "";
		notify = false;
		name = "";
		html = "";
	}

	public Content(PropBagEx xml)
	{
		super();
		course = xml.getNode("@course_id");
		content = xml.getNode("@content_id");
		user = xml.getNode("@user");
		notify = xml.isNodeTrue("@notify");
		name = xml.getNode("@name");
		html = xml.getNode("html");
	}

	public PropBagEx getXml()
	{
		PropBagEx xml = new PropBagEx().newSubtree(NODE);
		xml = getXml(xml);
		xml.setNode("html", html);
		return xml;
	}

	public PropBagEx getXml(PropBagEx xml)
	{
		if( xml == null )
		{
			xml = new PropBagEx().newSubtree(NODE);
		}

		xml.setNode("@course_id", course);
		xml.setNode("@content_id", content);
		xml.setNode("@user", user);
		xml.setNode("@name", name);
		xml.setNode("@notify", notify);

		return xml;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof Content) )
		{
			return false;
		}

		Content c = (Content) obj;
		return c.getContent().equals(getContent()) && c.getCourse().equals(getCourse());
	}

	@Override
	public int hashCode()
	{
		return (getContent() + getContent()).hashCode();
	}

	public static Map<String, Content> getContentMap(PropBagEx xml)
	{
		Map<String, Content> contents = new HashMap<String, Content>();

		if( xml == null )
		{
			return contents;
		}

		for( PropBagEx contentXml : xml.iterator(NODE) )
		{
			Content content = new Content(contentXml);
			contents.put(content.getContent(), content);
		}
		return contents;
	}

	public static Collection<Content> getContent(PropBagEx xml)
	{
		Collection<Content> contents = new ArrayList<Content>();

		if( xml == null )
		{
			return contents;
		}

		for( PropBagEx content : xml.iterator(NODE) )
		{
			contents.add(new Content(content));
		}
		return contents;
	}

	// ////////////// GETTERS AND SETTERS //////////////////

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public String getCourse()
	{
		return course;
	}

	public void setCourse(String course)
	{
		this.course = course;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public boolean getIsNotify()
	{
		return notify;
	}

	public void setIsNotify(boolean notify)
	{
		this.notify = notify;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getHtml()
	{
		return html;
	}

	public void setHtml(String html)
	{
		this.html = html;
	}
}
