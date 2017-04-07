package com.tle.web.api.item.interfaces.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.tle.web.api.interfaces.beans.AbstractExtendableBean;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = GenericFileBean.class)
public class GenericFileBean extends AbstractExtendableBean
{
	public static final String TYPE = "generic";

	private String filename;
	private GenericFileBean parent;

	public String getFilename()
	{
		return filename;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public GenericFileBean getParent()
	{
		return parent;
	}

	public void setParent(GenericFileBean parent)
	{
		this.parent = parent;
	}
}
