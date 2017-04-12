package com.tle.common.harvester;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

import com.tle.beans.entity.BaseEntity;

@Entity
@AccessType("field")
public class HarvesterProfile extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	@Column(length = 64)
	private String type;
	private Date lastRun;
	private Boolean enabled;
	private Boolean newVersionOnHarvest;

	public HarvesterProfile()
	{
		super();
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public void setLastRun(Date lastRun)
	{
		this.lastRun = lastRun;
	}

	public Date getLastRun()
	{
		return lastRun;
	}

	public void setEnabled(Boolean enabled)
	{
		this.enabled = enabled;
	}

	public Boolean getEnabled()
	{
		return enabled;
	}

	public void setNewVersionOnHarvest(Boolean newVersionOnHarvest)
	{
		this.newVersionOnHarvest = newVersionOnHarvest;
	}

	public Boolean getNewVersionOnHarvest()
	{
		return newVersionOnHarvest;
	}
}
