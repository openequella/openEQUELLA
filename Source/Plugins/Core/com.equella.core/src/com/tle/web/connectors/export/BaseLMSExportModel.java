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
