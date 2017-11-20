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

package com.tle.web.wizard.guice;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.tle.core.guice.ListProvider;
import com.tle.core.guice.PluginTrackerModule;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory;
import com.tle.web.sections.equella.guice.SectionsModule;
import com.tle.web.viewitem.summary.sidebar.LockedByGroupSection;
import com.tle.web.wizard.PackageTreeBuilder;
import com.tle.web.wizard.WizardState;
import com.tle.web.wizard.command.*;
import com.tle.web.wizard.impl.WizardCommand;
import com.tle.web.wizard.render.WizardExtendedFactory;
import com.tle.web.wizard.section.DuplicateDataSection;
import com.tle.web.wizard.section.PagesSection;
import com.tle.web.wizard.section.PreviewSection;
import com.tle.web.wizard.section.RootWizardSection;
import com.tle.web.wizard.section.SelectThumbnailSection;
import com.tle.web.wizard.section.WizardBodySection;

@SuppressWarnings("nls")
public class WizardModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		NodeProvider rootNode = node(RootWizardSection.class);
		rootNode.child(PreviewSection.class);
		rootNode.child(wizardNav());
		rootNode.child(SelectThumbnailSection.class);
		bind(Object.class).annotatedWith(Names.named("/access/runwizard")).toProvider(rootNode);

		ListProvider<WizardCommand> commands = new ListProvider<WizardCommand>(binder(),
			ImmutableList.of(EditInWizard.class, ViewSummary.class, Approve.class, Reject.class, SaveAndContinue.class, Preview.class, Save.class,
				Cancel.class, SelectThumbnail.class));
		ListProvider<SectionId> additional = new ListProvider<SectionId>(binder());
		additional.add(LockedByGroupSection.class);

		bind(new TypeLiteral<List<WizardCommand>>()
		{
		}).toProvider(commands);
		bind(new TypeLiteral<List<SectionId>>()
		{
		}).toProvider(additional);
		bind(WizardExtendedFactory.class).annotatedWith(Names.named("TitleFactory")).to(NamedTitleFactory.class)
			.asEagerSingleton();
		install(new WizardTracker());

		requestStaticInjection(WizardState.class);
	}

	private NodeProvider wizardNav()
	{
		NodeProvider node = node(WizardBodySection.class).placeHolder("WIZARD_NAVIGATION");
		node.child(PagesSection.class);
		node.child(DuplicateDataSection.class);
		node.innerChild(SaveDialog.class);
		return node;
	}

	public static class NamedTitleFactory extends WizardExtendedFactory
	{
		public NamedTitleFactory()
		{
			setName("wizardTitles");
		}
	}

	private static class WizardTracker extends PluginTrackerModule
	{
		@Override
		protected String getPluginId()
		{
			return "com.tle.web.wizard";
		}

		@Override
		protected void configure()
		{
			bindTracker(PackageTreeBuilder.class, "packagetreebuilder", "class").setIdParam("id");
		}
	}

}
