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

import java.util.Map;

public class FullAjaxCaptureResult extends AjaxCaptureResult
{
	private final Map<String, Object> params;

	public FullAjaxCaptureResult(String html, String script, String divId, Map<String, Object> params)
	{
		super(html, script, divId);
		this.params = params;
	}

	public Map<String, Object> getParams()
	{
		return params;
	}
}
