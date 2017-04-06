package com.tle.blackboard.webservice;

public class Folder extends Base
{
	// private Course course;
	// private final List<Content> contents = new ArrayList<Content>();

	/**
	 * May or may not be populated. Beware
	 * 
	 * @return A course. May only contain the course ID, or be fully populated
	 */
	// public Course getCourse()
	// {
	// return course;
	// }
	//
	// public void setCourse(Course course)
	// {
	// this.course = course;
	// }

	private String courseId;

	// private final List<String> contents = new ArrayList<String>();

	public String getCourseId()
	{
		return courseId;
	}

	public void setCourseId(String courseId)
	{
		this.courseId = courseId;
	}

	// public Content[] getContents()
	// {
	// return contents.toArray(new Content[contents.size()]);
	// }
	//
	// public void addItem(Content item)
	// {
	// contents.add(item);
	// }

}
