package com.tle.web.sections.equella.utils;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionTree;

@Bind
@Singleton
public class UserLinkService
{
	@Inject
	private Provider<UserLinkSection> linkProvider;

	public UserLinkSection register(SectionTree tree, String parentId)
	{
		UserLinkSection uls = tree.lookupSection(UserLinkSection.class, null);
		if( uls == null )
		{
			uls = linkProvider.get();
			tree.registerInnerSection(uls, parentId);
		}
		return uls;
	}
}
