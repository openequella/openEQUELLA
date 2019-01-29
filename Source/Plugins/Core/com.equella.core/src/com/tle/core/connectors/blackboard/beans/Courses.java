package com.tle.core.connectors.blackboard.beans;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class Courses
{
	private List<Course> results;

	public List<Course> getResults()
	{
		return results;
	}

	public void setResults(List<Course> results)
	{
		this.results = results;
	}
}
