package com.tle.core.connectors.brightspace.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Aaron
 *
 */
@XmlRootElement
public class RichText
{
	/*
	 * Read properties
	 */
	@JsonProperty("Text")
	private String text;
	//"Text|Html"
	@JsonProperty("Html")
	private String html;

	/*
	 * Write properties
	 */

	@JsonProperty("Content")
	private String content;
	//"Text|Html"
	@JsonProperty("Type")
	private String type;

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public String getHtml()
	{
		return html;
	}

	public void setHtml(String html)
	{
		this.html = html;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}
}
