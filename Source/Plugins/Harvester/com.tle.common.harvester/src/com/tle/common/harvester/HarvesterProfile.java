/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
