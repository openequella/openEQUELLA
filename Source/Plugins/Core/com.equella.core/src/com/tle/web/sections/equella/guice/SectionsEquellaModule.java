/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.equella.guice;

import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.tle.core.guice.PluginTrackerModule;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.login.LoginLink;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory;
import com.tle.web.sections.equella.impl.ModalErrorSection;
import com.tle.web.sections.equella.impl.RootModalSessionSection;
import com.tle.web.sections.standard.renderers.FreemarkerComponentRendererFactory;
import com.tle.web.template.RenderTemplate;
import com.tle.web.template.section.FooterSection;
import com.tle.web.template.section.HelpAndScreenOptionsSection;
import com.tle.web.template.section.HtmlStyleClass;
import com.tle.web.template.section.MenuSection;
import com.tle.web.template.section.ServerMessageSection;
import com.tle.web.template.section.TopBarSection;

/**
 * This is specific to this plugin, don't use elsewhere
 */
@SuppressWarnings("nls")
public class SectionsEquellaModule extends SectionsModule
{

	private NodeProvider templateTree()
	{
		NodeProvider node = node(RenderTemplate.class).placeHolder("ROOT_TEMPLATE_ID");
		node.child(ServerMessageSection.class);
		node.child(TopBarSection.class);
		node.child(HelpAndScreenOptionsSection.class);
		node.child(MenuSection.class);
		node.child(FooterSection.class);
		return node;
	}

	@Override
	protected void configure()
	{
		bind(PluginResourceHandler.class).toInstance(new PluginResourceHandler());
		bind(Object.class).annotatedWith(Names.named("$TEMPLATE$")).toProvider(templateTree());
		bind(SectionTree.class).annotatedWith(Names.named("modalTree")).toProvider(
			tree(node(RootModalSessionSection.class).innerChild(ModalErrorSection.class)));
		bind(Object.class).annotatedWith(Names.named("com.tle.web.sections.equella.FormRenderer"))
			.to(FormRenderer.class).in(Scopes.SINGLETON);
		bind(FreemarkerFactory.class).to(ExtendedFreemarkerFactory.class).asEagerSingleton();
		install(new PluginModule());
	}

	public static class FormRenderer extends FreemarkerComponentRendererFactory
	{
		public FormRenderer()
		{
			setTemplate("renderer/simpleform.ftl");
		}
	}

	private static class PluginModule extends PluginTrackerModule
	{
		@Override
		protected String getPluginId()
		{
			return "com.tle.web.sections.equella";
		}

		@Override
		protected void configure()
		{
			bindTracker(LoginLink.class, "loginLink", "bean").orderByParameter("order");
			bindTracker(HtmlStyleClass.class, "htmlStyleClass", "class");
		}
	}
}
