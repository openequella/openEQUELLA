/*
 * Copyright 2019 Apereo
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
