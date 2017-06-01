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

package com.tle.core.oauth;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Singleton;

@Singleton
@SuppressWarnings("nls")
public class OAuthFlowDefinitions
{
	public static final OAuthFlowDefinition AUTHORISATION_CODE_GRANT = new OAuthFlowDefinition(true, false, false,
		"client.editor.flow.acg.name", "client.editor.flow.acg.desc", "default", "acg");
	public static final OAuthFlowDefinition IMPLICIT_GRANT = new OAuthFlowDefinition(true, true, false,
		"client.editor.flow.ig.name", "client.editor.flow.ig.desc", "default", "ig");
	public static final OAuthFlowDefinition CLIENT_CREDENTIALS_GRANT = new OAuthFlowDefinition(false, true, true,
		"client.editor.flow.ccg.name", "client.editor.flow.ccg.desc", "default", "ccg");

	public static List<OAuthFlowDefinition> getAll()
	{
		List<OAuthFlowDefinition> allTheThings = new ArrayList<OAuthFlowDefinition>();
		allTheThings.add(AUTHORISATION_CODE_GRANT);
		allTheThings.add(CLIENT_CREDENTIALS_GRANT);
		allTheThings.add(IMPLICIT_GRANT);
		return allTheThings;
	}

	protected OAuthFlowDefinitions()
	{
		// constructor to silence Sonar
	}

	public static OAuthFlowDefinition getForId(String id)
	{
		for( OAuthFlowDefinition flow : getAll() )
		{
			if( flow.getId().equals(id) )
			{
				return flow;
			}
		}
		return null;
	}
}
