package com.tle.core.connectors.brightspace.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Aaron
 *
 */
@XmlRootElement
public class BrightspaceUsageResults
{
	@JsonProperty("TotalEquellaLinkUsageCount")
	private int totalEquellaLinkUsageCount;
	@JsonProperty("ResultSetCount")
	private int resultSetCount;
	@JsonProperty("Next")
	private String next;
	@JsonProperty("Objects")
	private BrightspaceEquellaLink[] objects;

	public int getTotalEquellaLinkUsageCount()
	{
		return totalEquellaLinkUsageCount;
	}

	public void setTotalEquellaLinkUsageCount(int totalEquellaLinkUsageCount)
	{
		this.totalEquellaLinkUsageCount = totalEquellaLinkUsageCount;
	}

	public int getResultSetCount()
	{
		return resultSetCount;
	}

	public void setResultSetCount(int resultSetCount)
	{
		this.resultSetCount = resultSetCount;
	}

	public String getNext()
	{
		return next;
	}

	public void setNext(String next)
	{
		this.next = next;
	}

	public BrightspaceEquellaLink[] getObjects()
	{
		return objects;
	}

	public void setObjects(BrightspaceEquellaLink[] objects)
	{
		this.objects = objects;
	}
}
