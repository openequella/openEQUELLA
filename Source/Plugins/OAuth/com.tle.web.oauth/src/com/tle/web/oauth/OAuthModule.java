package com.tle.web.oauth;

import com.google.inject.name.Names;
import com.tle.web.oauth.section.OAuthClientEditorSection;
import com.tle.web.oauth.section.RootOAuthSection;
import com.tle.web.oauth.section.ShowOAuthSection;
import com.tle.web.sections.equella.guice.SectionsModule;
import com.tle.web.sections.registry.SectionsServlet;

public class OAuthModule extends SectionsModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("oauthTree")).toProvider(oauthTree());
		bind(SectionsServlet.class);
	}

	private NodeProvider oauthTree()
	{
		NodeProvider node = node(RootOAuthSection.class);
		node.innerChild(OAuthClientEditorSection.class);
		node.child(ShowOAuthSection.class);
		return node;
	}
}
