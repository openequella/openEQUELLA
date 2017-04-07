package com.tle.common.interfaces;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class BaseEntityReference
{
	private final Map<String, Object> extras = new HashMap<String, Object>();

	private String uuid;
	private I18NString name;

	public BaseEntityReference()
	{
		// nothing
	}

	public BaseEntityReference(String uuid)
	{
		this.uuid = uuid;
	}

	public BaseEntityReference(String uuid, I18NString name)
	{
		this.uuid = uuid;
		this.name = name;
	}

	@JsonAnyGetter
	public Map<String, Object> any()
	{
		return extras;
	}

	@JsonAnySetter
	public void set(String key, Object value)
	{
		extras.put(key, value);
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public I18NString getName()
	{
		return name;
	}

	public void setName(I18NString name)
	{
		this.name = name;
	}

}
