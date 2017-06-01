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

package com.tle.integration.blackboard.gateways;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.devlib.Base64;
import com.dytech.devlib.PropBagEx;
import com.tle.common.NameValue;
import com.tle.integration.blackboard.BlackBoardSessionData;

@SuppressWarnings("nls")
public class BlackboardExport extends Blackboard
{
	private static final Log LOGGER = LogFactory.getLog(BlackboardExport.class);

	public static final String CONTENT_NODE = "_blackboard";

	protected List<Content> newContents = new ArrayList<Content>();

	protected BlackBoardSessionData data;
	protected String username = "";

	public void setData(BlackBoardSessionData data)
	{
		this.data = data;
	}

	/**
	 * Exports a plan to Blackboard. This creates a link back to a plan/item in
	 * the repository.
	 */
	public PropBagEx exportItems(PropBagEx itemxml, boolean isPlan, boolean create) throws IOException
	{
		itemxml = getItemRoot(itemxml);
		PropBagEx resultxml = null;
		if( newContents.size() > 0 )
		{
			resultxml = export(itemxml, newContents, isPlan, create);

			// see Jira Defect TLE-996 :
			// http://apps.dytech.com.au/jira/browse/TLE-996
			// newContents.clear();
		}
		else
		{
			Collection<Content> contents = Content.getContent(itemxml.getSubtree(CONTENT_NODE));
			resultxml = export(itemxml, contents, isPlan, false);
		}
		if( resultxml == null )
		{
			return null;
		}
		processResults(itemxml, resultxml);

		return resultxml;
	}

	protected PropBagEx getItemRoot(PropBagEx xml)
	{
		// We need to be have a root element of item (for custom display stuff)
		if( xml.nodeExists("item") )
		{
			xml = xml.getSubtree("item");
		}
		return xml;
	}

	protected void processResults(PropBagEx itemxml, PropBagEx resultxml)
	{
		if( resultxml == null )
		{
			return;
		}

		Iterator<PropBagEx> i = resultxml.iterator("added/content");
		while( i.hasNext() )
		{
			PropBagEx xml = i.next();
			Content pair = new BlackboardContent(xml);
			pair.setUser(username);
			pair.setName(itemxml.getNode("name"));
			setContentPair(itemxml, pair);
		}

		i = resultxml.iterator("removed/content");
		while( i.hasNext() )
		{
			PropBagEx xml = i.next();
			Content pair = new BlackboardContent(xml);

			Iterator<PropBagEx> j = itemxml.iterator(CONTENT_NODE + "/" + Content.NODE);
			while( j.hasNext() )
			{
				xml = j.next();
				Content pair2 = new BlackboardContent(xml);
				if( pair.equals(pair2) )
				{
					j.remove();
				}
			}
		}
	}

	protected void setContentPair(PropBagEx itemxml, Content content)
	{
		PropBagEx xml = itemxml.newSubtree(CONTENT_NODE + "/" + Content.NODE);
		xml.setNode("@url", url.toString());
		content.getXml(xml);
	}

	protected PropBagEx export(PropBagEx xml, Collection<Content> contents, boolean isPlan, boolean isNew)
		throws IOException
	{
		if( contents.isEmpty() )
		{
			return null;
		}

		String uuid = xml.getNode("@id");

		// String html = getHtml(uuid, itemdefid, version, xml);

		List<NameValue> parameters = new ArrayList<NameValue>();

		// Redmine #6600 - BB91 SP8 introduced a bug which not only munges
		// <form> xml tags, but also <format> tags.
		// Hence by Base64 encoding the xml parameter bore we send it, and
		// decoding it here, we avoid the munge.
		// (search for #6600 to find all decoding code, and encoding code on the
		// outward bound, in case we ever
		// consider it appropriate to rollback this workaround.)
		String xmlBase64Encoded = new Base64().encode(xml.toString().getBytes("UTF-8"));

		parameters.add(new NameValue(xmlBase64Encoded, "xml"));

		parameters.add(new NameValue(Boolean.toString(true), "available"));
		parameters.add(new NameValue(Boolean.toString(isPlan), "isPlan"));
		parameters.add(new NameValue(Boolean.toString(isNew), "isNew"));

		addParameters(contents, parameters);

		LOGGER.info("Exporting item " + uuid + " to Blackboard");

		// see Jira Defect TLE-898 :
		// http://apps.dytech.com.au/jira/browse/TLE-898
		// Used to be 5 second timeout.
		return invoke(data, "export", parameters);
	}

	private List<NameValue> addParameters(Collection<Content> contents, List<NameValue> parameters)
	{
		for( Content content : contents )
		{
			parameters.add(new NameValue(content.getCourse(), "course_id"));
			parameters.add(new NameValue(content.getContent(), "content_id"));
		}
		return parameters;
	}

	public void delete(PropBagEx itemxml) throws Exception
	{
		itemxml = getItemRoot(itemxml);
		Collection<Content> contents = Content.getContent(itemxml);

		List<NameValue> parameters = new ArrayList<NameValue>(contents.size() * 2);

		for( Content content : contents )
		{
			parameters.add(new NameValue(content.getCourse(), "course_id"));
			parameters.add(new NameValue(content.getContent(), "content_id"));
		}

		LOGGER.info("Delete content " + "" + " from Blackboard");
		invoke(data, "delete", parameters);
	}

	public String getRedirectURL(String id, String itemdefid, int version1, String path)
	{
		String url1 = "item/" + itemdefid + "/" + id + "/" + version1 + "/" + path + "?hasPoppedUp=true&";
		url1 = encode(url1);

		url1 = this.url.getPath() + "Redirect?forward=" + url1;
		return url1;
	}

	public void addContent(String courseId, String contentId)
	{
		if( courseId != null && courseId.length() > 0 && contentId != null && contentId.length() > 0 )
		{
			Content content = new BlackboardContent();
			content.setCourse(courseId);
			content.setContent(contentId);
			newContents.add(content);
		}
	}

	public URL previewContent(String courseId, String contentId, boolean modify) throws IOException
	{
		NameValue[] parameters = new NameValue[]{new NameValue(courseId, "course_id"),
				new NameValue(contentId, "content_id"), new NameValue(Boolean.toString(modify), "modify"),};

		String referrer = invoke(data, "url", convertParameters(parameters)).getNode("url");
		return new URL(url.getProtocol(), url.getHost(), url.getPort(), referrer);
	}

	public URL redirContent(String courseId, String contentId, boolean modify) throws IOException
	{
		NameValue[] parameters = new NameValue[]{new NameValue(courseId, "course_id"),
				new NameValue(contentId, "content_id"), new NameValue(Boolean.toString(modify), "modify"),};

		String referrer = invoke(data, "burl", convertParameters(parameters)).getNode("url");
		return new URL(url.getProtocol(), url.getHost(), url.getPort(), referrer);
	}

	public String getHome()
	{
		try
		{
			return new URL(url.getProtocol(), url.getHost(), url.getPort(), "/webapps/portal/tab/_1_1/index.jsp")
				.toString();
		}
		catch( MalformedURLException e )
		{
			// Never happen
		}
		return null;
	}

	public String getTopHome()
	{
		try
		{
			return new URL(url.getProtocol(), url.getHost(), url.getPort(), "/webapps/portal/frameset.jsp").toString();
		}
		catch( MalformedURLException e )
		{
			// Never happen
		}
		return null;
	}

	public PropBagEx invoke(String name, NameValue[] parameters) throws IOException
	{
		return super.invoke(data, name, convertParameters(parameters));
	}

	public List<Content> getContents()
	{
		return newContents;
	}
}
