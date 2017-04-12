package com.tle.web.api.activation;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.BaseEntityBean;

/**
 * @author larry
 */
@XmlRootElement
public class CourseBean extends BaseEntityBean
{
	/**
	 * Course code, for POST /course, a mandatory field, must be unique
	 */
	private String code;
	private String type;
	private String citation;
	private String departmentName;
	private Date from;
	private Date until;
	/**
	 * no of students in the course, must be integer
	 */
	private int students;
	private String versionSelection;
	private boolean archived;

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getCitation()
	{
		return citation;
	}

	public void setCitation(String citation)
	{
		this.citation = citation;
	}

	public String getDepartmentName()
	{
		return departmentName;
	}

	public void setDepartmentName(String departmentName)
	{
		this.departmentName = departmentName;
	}

	public Date getFrom()
	{
		return from;
	}

	public void setFrom(Date from)
	{
		this.from = from;
	}

	public Date getUntil()
	{
		return until;
	}

	public void setUntil(Date until)
	{
		this.until = until;
	}

	public int getStudents()
	{
		return students;
	}

	public void setStudents(int students)
	{
		this.students = students;
	}

	public String getVersionSelection()
	{
		return versionSelection;
	}

	public void setVersionSelection(String versionSelection)
	{
		this.versionSelection = versionSelection;
	}

	public boolean isArchived()
	{
		return archived;
	}

	public void setArchived(boolean archived)
	{
		this.archived = archived;
	}
}
