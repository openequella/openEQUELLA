package com.tle.web.connectors.export;

import java.util.List;

import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.entity.Connector;
import com.tle.web.sections.render.Label;

/**
 * @author Aaron
 */
public class BaseLMSExportModel
{
	private List<ConnectorCourse> coursesCache;
	private Connector connector;
	private Label error;

	public List<ConnectorCourse> getCoursesCache()
	{
		return coursesCache;
	}

	public void setCoursesCache(List<ConnectorCourse> coursesCache)
	{
		this.coursesCache = coursesCache;
	}

	public Connector getConnector()
	{
		return connector;
	}

	public void setConnector(Connector connector)
	{
		this.connector = connector;
	}

	public Label getError()
	{
		return error;
	}

	public void setError(Label error)
	{
		this.error = error;
	}
}
