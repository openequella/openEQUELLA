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

package com.tle.core.connectors.brightspace.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Aaron
 *
 */
@XmlRootElement
public class MyOrgUnitInfo
{
	@JsonProperty("OrgUnit")
	private OrgUnitInfo orgUnit;
	@JsonProperty("Access")
	private Access access;

	public OrgUnitInfo getOrgUnit()
	{
		return orgUnit;
	}

	public void setOrgUnit(OrgUnitInfo orgUnit)
	{
		this.orgUnit = orgUnit;
	}

	public Access getAccess()
	{
		return access;
	}

	public void setAccess(Access access)
	{
		this.access = access;
	}

	@XmlRootElement
	public static class Access
	{
		@JsonProperty("IsActive")
		private boolean isActive;
		@JsonProperty("StartDate")
		private String startDate;
		@JsonProperty("EndDate")
		private String endDate;
		@JsonProperty("CanAccess")
		private boolean canAccess;
		@JsonProperty("ClasslistRoleName")
		private String classlistRoleName;
		@JsonProperty("LISRoles")
		private List<String> lisRoles;

		public boolean isActive()
		{
			return isActive;
		}

		public void setActive(boolean isActive)
		{
			this.isActive = isActive;
		}

		public String getStartDate()
		{
			return startDate;
		}

		public void setStartDate(String startDate)
		{
			this.startDate = startDate;
		}

		public String getEndDate()
		{
			return endDate;
		}

		public void setEndDate(String endDate)
		{
			this.endDate = endDate;
		}

		public boolean isCanAccess()
		{
			return canAccess;
		}

		public void setCanAccess(boolean canAccess)
		{
			this.canAccess = canAccess;
		}

		public String getClasslistRoleName()
		{
			return classlistRoleName;
		}

		public void setClasslistRoleName(String classlistRoleName)
		{
			this.classlistRoleName = classlistRoleName;
		}

		public List<String> getLisRoles()
		{
			return lisRoles;
		}

		public void setLisRoles(List<String> lisRoles)
		{
			this.lisRoles = lisRoles;
		}
	}
}
