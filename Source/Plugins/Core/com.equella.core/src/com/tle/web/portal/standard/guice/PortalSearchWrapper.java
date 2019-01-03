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

package com.tle.web.portal.standard.guice;

import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.selection.home.sections.SelectionPortletRendererWrapper;

public class PortalSearchWrapper extends SelectionPortletRendererWrapper
{
	@PlugKey("selection.search.name")
	private static Label LABEL_TITLE;

	public PortalSearchWrapper()
	{
		setPortletType("search"); //$NON-NLS-1$
	}

	@Override
	protected Label getBoxLabel()
	{
		return LABEL_TITLE;
	}
}
