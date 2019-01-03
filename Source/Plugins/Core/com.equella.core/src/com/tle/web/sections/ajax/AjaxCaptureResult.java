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

package com.tle.web.sections.ajax;

public class AjaxCaptureResult
{
	private final String html;
	private final String script;
	private final String divId;

	public AjaxCaptureResult(String html, String script, String divId)
	{
		this.html = html;
		this.script = script;
		this.divId = divId;
	}

	public AjaxCaptureResult(AjaxCaptureResult captured)
	{
		this.html = captured.html;
		this.script = captured.script;
		this.divId = captured.divId;
	}

	public String getHtml()
	{
		return html;
	}

	public String getScript()
	{
		return script;
	}

	public String getDivId()
	{
		return divId;
	}
}
