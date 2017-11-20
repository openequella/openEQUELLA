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

package com.tle.web.customlinks.model;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.tle.web.customlinks.CustomLinkListComponent;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.equella.layout.OneColumnLayout.OneColumnLayoutModel;
import com.tle.web.sections.render.Label;

public class CustomLinksModel extends OneColumnLayoutModel
{
	@Bookmarked
	private boolean editing;
	@Bookmarked(name = "sessionId")
	private String sessionId;
	@Bookmarked(stateful = false)
	private boolean rendered;

	private List<CustomLinkListComponent> links;
	private Label heading;
	private Map<String, Object> errors = Maps.newHashMap();
	private String expressionPretty;
	private String fileName;
	private String entityUuid;

	public void setEditing(boolean editing)
	{
		this.editing = editing;
	}

	public boolean isEditing()
	{
		return editing;
	}

	public String getExpressionPretty()
	{
		return expressionPretty;
	}

	public void setExpressionPretty(String expressionPretty)
	{
		this.expressionPretty = expressionPretty;
	}

	public void setSessionId(String sessionId)
	{
		this.sessionId = sessionId;
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public void setErrors(Map<String, Object> errors)
	{
		this.errors = errors;
	}

	public Map<String, Object> getErrors()
	{
		return errors;
	}

	public void setRendered(boolean rendered)
	{
		this.rendered = rendered;
	}

	public boolean isRendered()
	{
		return rendered;
	}

	public void setHeading(Label heading)
	{
		this.heading = heading;
	}

	public Label getHeading()
	{
		return heading;
	}

	public void setLinks(List<CustomLinkListComponent> links)
	{
		this.links = links;
	}

	public List<CustomLinkListComponent> getLinks()
	{
		return links;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public String getFileName()
	{
		return fileName;
	}

	public String getEntityUuid()
	{
		return entityUuid;
	}

	public void setEntityUuid(String entityUuid)
	{
		this.entityUuid = entityUuid;
	}

}
