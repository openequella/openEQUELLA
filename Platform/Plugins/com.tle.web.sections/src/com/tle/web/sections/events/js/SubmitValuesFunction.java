/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.events.js;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.generic.function.PrependedParameterFunction;

public class SubmitValuesFunction extends PrependedParameterFunction
{
	private SubmitEventFunction eventFunc;
	// private String firstParam;
	// private int numParams;
	private final ParameterizedEvent event;

	public SubmitValuesFunction(ParameterizedEvent event)
	{
		super(new SubmitEventFunction(), event.getEventId());
		this.eventFunc = (SubmitEventFunction) func;
		// this.firstParam = pevent.getEventId();
		// this.numParams = pevent.getParameterCount();
		this.event = event;
	}

	public String getFirstParam()
	{
		// return firstParam;
		return event.getEventId();
	}

	public void setValidate(boolean validate)
	{
		eventFunc.setValidate(validate);
	}

	public void setBlockFurtherSubmission(boolean blockFurtherSubmission)
	{
		eventFunc.setBlockFurtherSubmission(blockFurtherSubmission);
	}

	@Override
	public int getNumberOfParams(RenderContext context)
	{
		// return numParams;
		return event.getParameterCount();
	}

	public SubmitEventFunction getSubmitEventFunction()
	{
		return eventFunc;
	}

	public ParameterizedEvent getEvent()
	{
		return event;
	}
}