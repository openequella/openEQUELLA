package com.tle.common.connectors;

public class ConnectorTerminology
{
	// These are all keys
	private String showArchived;
	private String showArchivedLocations;
	private String courseHeading;
	private String locationHeading;

	public ConnectorTerminology()
	{

	}

	public ConnectorTerminology(String showArchived, String courseHeading, String locationHeading,
		String showArchivedLocations)
	{
		this.showArchived = showArchived;
		this.courseHeading = courseHeading;
		this.locationHeading = locationHeading;
		this.showArchivedLocations = showArchivedLocations;
	}

	public String getShowArchived()
	{
		return showArchived;
	}

	public void setShowArchived(String showArchived)
	{
		this.showArchived = showArchived;
	}

	public String getCourseHeading()
	{
		return courseHeading;
	}

	public void setCourseHeading(String courseHeading)
	{
		this.courseHeading = courseHeading;
	}

	public String getLocationHeading()
	{
		return locationHeading;
	}

	public void setLocationHeading(String locationHeading)
	{
		this.locationHeading = locationHeading;
	}

	public String getShowArchivedLocations()
	{
		return showArchivedLocations;
	}

	public void setShowArchivedLocations(String showArchivedLocations)
	{
		this.showArchivedLocations = showArchivedLocations;
	}
}
