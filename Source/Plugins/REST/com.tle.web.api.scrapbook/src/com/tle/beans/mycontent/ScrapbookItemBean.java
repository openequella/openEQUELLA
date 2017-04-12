package com.tle.beans.mycontent;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;

@XmlRootElement
public class ScrapbookItemBean extends AbstractExtendableBean
{
	private String uuid;
	private String title;
	private String type;
	private List<Map<String, String>> pages;
	private Map<String, Object> file;
	private Map<String, String> links;
	private String keywords;

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getKeywords()
	{
		return keywords;
	}

	public void setKeywords(String keywords)
	{
		this.keywords = keywords;
	}

	public Map<String, String> getLinks()
	{
		return links;
	}

	public void setLinks(Map<String, String> links)
	{
		this.links = links;
	}

	public List<Map<String, String>> getPages()
	{
		return pages;
	}

	public void setPages(List<Map<String, String>> pages)
	{
		this.pages = pages;
	}

	public Map<String, Object> getFile()
	{
		return file;
	}

	public void setFile(Map<String, Object> file)
	{
		this.file = file;
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
