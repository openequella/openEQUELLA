/*
 * Created on Jul 8, 2005
 */
package com.tle.beans.entity.report;

import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

import com.tle.beans.entity.BaseEntity;

/**
 * @author Nicholas Read
 */
@Entity
@AccessType("field")
public class Report extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	private String filename;
	private Boolean hideReport;

	public Report()
	{
		super();
	}

	public String getFilename()
	{
		return filename;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public Boolean isHideReport()
	{
		return hideReport != null && hideReport;
	}

	public void setHideReport(Boolean hideReport)
	{
		this.hideReport = hideReport;
	}
}
