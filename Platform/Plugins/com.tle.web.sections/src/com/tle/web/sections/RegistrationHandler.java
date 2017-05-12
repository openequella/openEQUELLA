package com.tle.web.sections;

import com.tle.web.sections.registry.TreeRegistry;

/**
 * Interface for extending the registration process.
 * <p>
 * During the {@link SectionTree} registration process, the
 * {@link RegistrationController} is responsible for calling
 * {@link #registered(String, SectionTree, Section)} on all the
 * {@code RegistrationHandler} objects before the {@code SectionTree} eventually
 * calls {@link Section#registered(String, SectionTree)} on the {@link Section}
 * itself.
 * <p>
 * After the tree has finished registering completely,
 * {@link #treeFinished(SectionTree)} is called by the
 * {@code RegistrationController} for each {@code RegistrationHandler}.
 * <p>
 * {@code RegistrationHandler}'s generally use introspection to look for
 * annotations in either the <code>Model</code> ({@link Section#getModelClass()}
 * ) or the {@code Section} itself.
 * <p>
 * The default implementor of the {@code RegistrationController},
 * {@link TreeRegistry} has a number of default {@code RegistrationHandler}'s
 * which deal with topics such as Bookmarking, Rendering and Tree Lookup.
 * 
 * @see TreeRegistry
 * @author jmaginnis
 */
public interface RegistrationHandler
{
	void registered(String id, SectionTree tree, Section section);

	void treeFinished(SectionTree tree);
}
