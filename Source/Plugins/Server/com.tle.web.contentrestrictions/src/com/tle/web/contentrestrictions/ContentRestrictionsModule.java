/**
 * 
 */
package com.tle.web.contentrestrictions;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;

/**
 * @author larry
 */
public class ContentRestrictionsModule extends SectionsModule
{

	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/contentrestrictions")).toProvider(
			contentRestrictionsTree());

	}

	private NodeProvider contentRestrictionsTree()
	{
		NodeProvider node = node(RootContentRestrictionsSection.class);
		node.innerChild(EditContentRestrictionsSection.class);
		return node;
	}

}
