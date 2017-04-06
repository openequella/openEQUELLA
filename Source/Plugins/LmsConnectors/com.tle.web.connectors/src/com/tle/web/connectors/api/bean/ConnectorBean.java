package com.tle.web.connectors.api.bean;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.BaseEntityBean;

/**
 * @author larry
 */
@XmlRootElement
public class ConnectorBean extends BaseEntityBean
{
	private String type;

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

}
