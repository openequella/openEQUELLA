package com.tle.common.connectors;

public class ConnectorCourse extends ConnectorFolder
{
	private static final long serialVersionUID = 1L;

	private String courseCode;

	public ConnectorCourse(String id)
	{
		super(id, null);
	}

	@Override
	public ConnectorCourse getCourse()
	{
		return this;
	}

	public String getCourseCode()
	{
		return courseCode;
	}

	public void setCourseCode(String courseCode)
	{
		this.courseCode = courseCode;
	}
}