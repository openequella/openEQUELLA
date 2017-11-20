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

package com.tle.web.institution.database;

import java.util.List;
import java.util.Map;

import com.tle.web.sections.ajax.AbstractDOMResult;
import com.tle.web.sections.ajax.FullAjaxCaptureResult;

public class StatusUpdate extends AbstractDOMResult
{
	private Map<String, FullAjaxCaptureResult> updates;
	private List<FullAjaxCaptureResult> rows;
	private boolean finished;

	public StatusUpdate(AbstractDOMResult result)
	{
		super(result);
	}

	public List<FullAjaxCaptureResult> getRows()
	{
		return rows;
	}

	public void setRows(List<FullAjaxCaptureResult> rows)
	{
		this.rows = rows;
	}

	public boolean isFinished()
	{
		return finished;
	}

	public void setFinished(boolean finished)
	{
		this.finished = finished;
	}

	public Map<String, FullAjaxCaptureResult> getUpdates()
	{
		return updates;
	}

	public void setUpdates(Map<String, FullAjaxCaptureResult> updates)
	{
		this.updates = updates;
	}
}