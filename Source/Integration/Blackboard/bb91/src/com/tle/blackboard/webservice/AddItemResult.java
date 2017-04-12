package com.tle.blackboard.webservice;

public class AddItemResult
{
	private Folder folder;
	private Course course;

	public Folder getFolder()
	{
		return folder;
	}

	public void setFolder(Folder folder)
	{
		this.folder = folder;
	}

	public Course getCourse()
	{
		return course;
	}

	public void setCourse(Course course)
	{
		this.course = course;
	}
}
