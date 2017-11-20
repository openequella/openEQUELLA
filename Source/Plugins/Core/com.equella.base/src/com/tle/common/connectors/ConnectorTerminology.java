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
