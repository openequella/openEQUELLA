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
import com.tle.web.wizard.command.Cancel;
import com.tle.web.wizard.command.EditInWizard;
import com.tle.web.wizard.command.Preview;
import com.tle.web.wizard.command.Save;
import com.tle.web.wizard.command.SaveAndContinue;
import com.tle.web.wizard.command.SaveDialog;
import com.tle.web.wizard.command.SelectThumbnail;
import com.tle.web.wizard.command.ViewSummary;
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

		ListProvider<WizardCommand> commands = new ListProvider<WizardCommand>(binder(), ImmutableList.of(
			EditInWizard.class, ViewSummary.class, SaveAndContinue.class, Preview.class, Save.class, Cancel.class,
			SelectThumbnail.class));
		ListProvider<SectionId> additional = new ListProvider<SectionId>(binder());
		additional.add(LockedByGroupSection.class);

		bind(new TypeLiteral<List<WizardCommand>>()
		{
		}).toProvider(commands);
		bind(new TypeLiteral<List<SectionId>>()
		{
		}).toProvider(additional);
		bind(ExtendedFreemarkerFactory.class).asEagerSingleton();
		bind(WizardExtendedFactory.class).annotatedWith(Names.named("TitleFactory")).to(NamedTitleFactory.class)
			.asEagerSingleton();
		install(new WizardTracker());
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
		protected void configure()
		{
			bindTracker(PackageTreeBuilder.class, "packagetreebuilder", "class").setIdParam("id");
		}
	}

}
