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
