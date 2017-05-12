package com.tle.common.interfaces;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * We might want to consider some sort of referencing standard, e.g.
 * http://dojotoolkit.org/reference-guide/dojox/json/ref.html
 * 
 * @author Aaron
 */
@XmlRootElement
public class UuidReference
{
	private String uuid;

	public UuidReference()
	{
	}

	public UuidReference(String uuid)
	{
		this.uuid = uuid;
	}

	@JsonProperty("$ref")
	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}
}
