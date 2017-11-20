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

package com.tle.beans.item.cal.request;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.entity.BaseEntity;
import com.tle.beans.item.VersionSelection;
import com.tle.common.NameValue;

@Entity
@AccessType("field")
// No can do as it's across 2 tables... we need some enforcement though...
// @Table(uniqueConstraints = {@UniqueConstraint(columnNames =
// {"institution_id", "code"})})
public class CourseInfo extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	// Keep Sonar happy with a final immutable List
	//@formatter:off
	@SuppressWarnings("nls")
	public static final List<NameValue> COURSE_TYPES = Collections.unmodifiableList(Arrays.asList(
		new NameValue("Internal", "i"),
		new NameValue("External", "e"),
		new NameValue("Staff", "s")));
	//@formatter:on

	private int students;
	private Date from;
	@Index(name = "courseInfoUntilIndex")
	private Date until;
	private String citation;
	private String lmsid;
	@Column(length = 512)
	private String departmentName;
	private Character courseType;
	@Index(name = "courseCodeIndex")
	@Column(length = 128)
	private String code;
	@Column(length = 30)
	@Enumerated(EnumType.STRING)
	private VersionSelection versionSelection;

	public CourseInfo()
	{
		super();
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public Date getFrom()
	{
		return from;
	}

	public void setFrom(Date from)
	{
		this.from = from;
	}

	public int getStudents()
	{
		return students;
	}

	public void setStudents(int students)
	{
		this.students = students;
	}

	public Date getUntil()
	{
		return until;
	}

	public void setUntil(Date until)
	{
		this.until = until;
	}

	public String getCitation()
	{
		return citation;
	}

	public void setCitation(String citation)
	{
		this.citation = citation;
	}

	public String getLmsid()
	{
		return lmsid;
	}

	public void setLmsid(String lmsid)
	{
		this.lmsid = lmsid;
	}

	public String getDepartmentName()
	{
		return departmentName;
	}

	public void setDepartmentName(String departmentName)
	{
		if( departmentName != null && departmentName.length() > 512 )
		{
			departmentName = departmentName.substring(0, 512);
		}
		this.departmentName = departmentName;
	}

	public char getCourseType()
	{
		if( courseType == null )
		{
			return 'i';
		}
		return courseType;
	}

	public void setCourseType(char courseType)
	{
		this.courseType = courseType;
	}

	public VersionSelection getVersionSelection()
	{
		return versionSelection;
	}

	public void setVersionSelection(VersionSelection versionSelection)
	{
		this.versionSelection = versionSelection;
	}

	/**
	 * From static List, get the name string corresponding to primitive char.
	 * @param ch
	 * @return name String if matched, otherwise null
	 */
	public static String getTypeStringFromChar(char ch)
	{
		String val = null;
		Iterator<NameValue> iter = CourseInfo.COURSE_TYPES.iterator();
		while( iter.hasNext() )
		{
			NameValue nameValue = iter.next();
			if( nameValue.getValue().charAt(0) == ch )
			{
				return nameValue.getName();
			}
		}
		return val;
	}
}
