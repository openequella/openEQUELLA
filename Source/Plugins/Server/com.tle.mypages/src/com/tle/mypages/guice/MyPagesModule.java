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

package com.tle.mypages.guice;

import com.google.inject.name.Names;
import com.tle.mypages.web.MyPagesState;
import com.tle.mypages.web.section.MyPagesContributeSection;
import com.tle.mypages.web.section.MyPagesEditorSection;
import com.tle.mypages.web.section.MyPagesPageActionsSection;
import com.tle.mypages.web.section.MyPagesTitleSection;
import com.tle.mypages.web.section.RootMyPagesSection;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.guice.SectionsModule;

public class MyPagesModule extends SectionsModule
{
	private NodeProvider createEditorTree()
	{
		NodeProvider handlerNode = node(MyPagesContributeSection.class);
		handlerNode.child(MyPagesTitleSection.class);
		handlerNode.child(MyPagesPageActionsSection.class);
		NodeProvider editorNode = node(MyPagesEditorSection.class);
		handlerNode.child(editorNode);
		return handlerNode;
	}

	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		NodeProvider node = node(RootMyPagesSection.class);
		node.child(createEditorTree());
		bindNamed("/access/mypagesedit", node);
		bind(SectionTree.class).annotatedWith(Names.named("myPagesContentTree")).toProvider(tree(createEditorTree()));

		requestStaticInjection(MyPagesState.class);
	}
}
