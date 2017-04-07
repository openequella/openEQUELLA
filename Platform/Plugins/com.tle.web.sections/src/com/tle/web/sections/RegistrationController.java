package com.tle.web.sections;

import com.tle.web.sections.generic.DefaultSectionTree;
import com.tle.web.sections.registry.TreeRegistry;

/**
 * Co-ordinates the calling of {@link RegistrationHandler} methods.
 * 
 * @see DefaultSectionTree
 * @see TreeRegistry
 * @author jmaginnis
 */
public interface RegistrationController
{
	void registered(String id, SectionTree tree, Section section);

	void treeFinished(SectionTree tree);
}
