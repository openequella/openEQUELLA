package com.tle.core.payment.beans.store;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.google.common.collect.Maps;

/**
 * @author Aaron
 */
@XmlRootElement
public class StoreCatalogueAttachmentBean
{
	private String uuid;
	private String description;
	private boolean preview;
	private String filename;
	private String attachmentType;
	private String parentZip;
	private boolean external;

	private final Map<String, Object> extras = Maps.newHashMap();

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public boolean isPreview()
	{
		return preview;
	}

	public void setPreview(boolean preview)
	{
		this.preview = preview;
	}

	public Object get(String name)
	{
		return extras.get(name);
	}

	public String getFilename()
	{
		return filename;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public String getAttachmentType()
	{
		return attachmentType;
	}

	public void setAttachmentType(String attachmentType)
	{
		this.attachmentType = attachmentType;
	}

	public boolean isExternal()
	{
		return external;
	}

	public void setExternal(boolean external)
	{
		this.external = external;
	}

	public String getParentZip()
	{
		return parentZip;
	}

	public void setParentZip(String parentZip)
	{
		this.parentZip = parentZip;
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
}
