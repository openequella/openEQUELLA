package com.tle.blackboard.webservice;

public class SearchResult
{
	private int available;
	private Content[] results;
	private Folder[] folders;
	private Course[] courses;

	public int getAvailable()
	{
		return available;
	}

	public void setAvailable(int available)
	{
		this.available = available;
	}

	public Content[] getResults()
	{
		return results;
	}

	public void setResults(Content[] results)
	{
		this.results = results;
	}

	public Folder[] getFolders()
	{
		return folders;
	}

	public void setFolders(Folder[] folders)
	{
		this.folders = folders;
	}

	public Course[] getCourses()
	{
		return courses;
	}

	public void setCourses(Course[] courses)
	{
		this.courses = courses;
	}
}
