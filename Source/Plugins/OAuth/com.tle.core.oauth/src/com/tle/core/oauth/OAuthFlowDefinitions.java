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
