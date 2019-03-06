package com.tle.web.api.item.interfaces.beans;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Aaron
 */
@XmlRootElement
public class DrmNetworkBean
{
	private String name;
	private String startAddress;
	private String endAddress;

	public DrmNetworkBean()
	{
	}

	public DrmNetworkBean(String name, String startAddress, String endAddress)
	{
		this.name = name;
		this.startAddress = startAddress;
		this.endAddress = endAddress;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getStartAddress()
	{
		return startAddress;
	}

	public void setStartAddress(String startAddress)
	{
		this.startAddress = startAddress;
	}

	public String getEndAddress()
	{
		return endAddress;
	}

	public void setEndAddress(String endAddress)
	{
		this.endAddress = endAddress;
	}
}
