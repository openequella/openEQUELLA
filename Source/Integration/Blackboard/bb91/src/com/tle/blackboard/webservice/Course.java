package com.tle.blackboard.webservice;

public class Course extends Base
{
	private boolean available;
	private String code;
	private String[] instructors;
	private int enrollments;
	private String url;

	public boolean isAvailable()
	{
		return available;
	}

	public void setAvailable(boolean available)
	{
		this.available = available;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String[] getInstructors()
	{
		return instructors;
	}

	public void setInstructors(String[] instructors)
	{
		this.instructors = instructors;
	}

	public int getEnrollments()
	{
		return enrollments;
	}

	public void setEnrollments(int enrollments)
	{
		this.enrollments = enrollments;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}
}
