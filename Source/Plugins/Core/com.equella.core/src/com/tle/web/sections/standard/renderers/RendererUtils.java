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

package com.tle.web.sections.standard.renderers;

import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.standard.AbstractRenderedComponent;

public final class RendererUtils
{
	public static String getIdForObject(Object object)
	{
		if( object instanceof ElementId )
		{
			return ((AbstractRenderedComponent<?>) object).getSectionId();
		}
		else if( object instanceof String )
		{
			return (String) object;
		}
		return null;
	}

	private RendererUtils()
	{
		throw new Error();
	}
}
