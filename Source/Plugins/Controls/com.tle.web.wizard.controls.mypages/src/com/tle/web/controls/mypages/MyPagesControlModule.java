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

package com.tle.web.controls.mypages;

import com.google.inject.name.Names;
import com.tle.mypages.web.section.MyPagesContributeSection;
import com.tle.mypages.web.section.MyPagesEditorSection;
import com.tle.mypages.web.section.RootMyPagesSection;
import com.tle.web.sections.SectionNode;
import com.tle.web.sections.equella.guice.SectionsModule;

public class MyPagesControlModule extends SectionsModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		NodeProvider node = node(RootMyPagesSection.class);

		NodeProvider handlerNode = node(MyPagesContributeSection.class);
		node.child(handlerNode);

		handlerNode.child(MyPagesHandlerPageActionsSection.class);

		NodeProvider edNode = node(MyPagesEditorSection.class);
		edNode.child(MyPagesExtrasSection.class);
		handlerNode.child(edNode);

		bind(SectionNode.class).annotatedWith(Names.named("myPagesTree")).toProvider(node);
	}

}
