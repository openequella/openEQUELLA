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

package com.tle.web.contribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.common.text.NumberStringComparator;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.fedsearch.FederatedSearchService;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.TextBundle;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.remoterepo.service.RemoteRepoWebService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.contribute.QuickUploadSection;
import com.tle.web.selection.section.CourseListVetoSection;
import com.tle.web.selection.section.RootSelectionSection.Layout;
import com.tle.web.template.Decorations;
import com.tle.web.template.section.HelpAndScreenOptionsSection;
import com.tle.web.wizard.WebWizardService;
import com.tle.web.wizard.WizardInfo;
import com.tle.web.wizard.WizardService;

/**
 * Logic for presenting the wizard contribution categories and item definitions.
 */
@SuppressWarnings("nls")
public class ContributeSection extends AbstractPrototypeSection<ContributeSection.ContributeModel>
	implements
		HtmlRenderer,
		CourseListVetoSection
{
	@PlugKey("title")
	private static Label TITLE_LABEL;
	@PlugKey("remove")
	private static Label LABEL_REMOVE;
	@PlugKey("quick.contribute.category")
	private static Label LABEL_QUICK_CONTRIBUTE;

	@Inject
	private BundleCache bundleCache;
	@Inject
	private ItemDefinitionService itemDefinitionService;
	@Inject
	private FederatedSearchService federatedSearchService;
	@Inject
	private SelectionService selectionService;
	@Inject
	private RemoteRepoWebService remoteRepoWebService;
	@Inject
	private WizardService wizardService;
	@Inject
	private WebWizardService webWizardService;

	@Inject
	private QuickUploadSection quickUploadSection;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(quickUploadSection, id);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final Multimap<String, ItemDefinition> wizardHash = TreeMultimap.create(Format.STRING_COMPARATOR,
			new NumberStringComparator<ItemDefinition>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public String convertToString(ItemDefinition t)
				{
					return TextBundle.getLocalString(t.getName(), bundleCache, null, "");
				}
			});

		final List<ItemDefinition> contributableCollections = enumerateCreatable(context);
		final boolean singleCol = contributableCollections.size() == 1;

		for( ItemDefinition itemdef : contributableCollections )
		{
			final String category = itemdef.getWizardcategory();
			bundleCache.addBundle(itemdef.getName());
			bundleCache.addBundle(itemdef.getDescription());
			if( !Check.isEmpty(category) )
			{
				wizardHash.put(category, itemdef);
			}
		}
		final ContributeModel model = getModel(context);

		final List<WizardInfo> resumableWizards = wizardService.listWizardsInSession();
		Collections.sort(resumableWizards);

		final List<ResumableWizard> resumables = new ArrayList<ResumableWizard>();

		if( resumableWizards.size() > 0 )
		{
			for( WizardInfo wizInfo : resumableWizards )
			{
				// only show new items that are left in the contribution page
				if( wizInfo.isNewItem() )
				{
					final String uuid = wizInfo.getUuid();

					final ResumableWizard reWizard = new ResumableWizard();
					reWizard.setStartedDate(wizInfo.getStartedDate());

					final HtmlLinkState resumeLink = new HtmlLinkState(events.getNamedHandler("resume", uuid));

					reWizard.setResumeLink(resumeLink);
					reWizard.setCollectionName(new TextLabel(wizInfo.getCollectionName()));

					final HtmlLinkState removeLink = new HtmlLinkState(LABEL_REMOVE,
						events.getNamedHandler("removeWizard", uuid));
					reWizard.setRemoveLink(new LinkRenderer(removeLink));

					resumables.add(reWizard);
				}
			}
		}
		model.setResumables(resumables);

		final SelectionSession selectionSession = selectionService.getCurrentSession(context);
		if( selectionSession != null )
		{
			model.setHideResumable(true);
		}

		final List<TableState> categories = Lists.newArrayList();

		// FIXME: what's wrong with extension points?
		// Load quick contribute at the top
		if( selectionSession != null && selectionSession.getLayout() == Layout.COURSE
			&& quickUploadSection.canView(context) )
		{
			TableState quickCategory = new TableState();
			quickCategory.makePresentation();
			quickCategory.setColumnHeadings(LABEL_QUICK_CONTRIBUTE);
			quickCategory.addRow(quickUploadSection);
			categories.add(quickCategory);
		}

		for( Map.Entry<String, Collection<ItemDefinition>> entry : wizardHash.asMap().entrySet() )
		{
			final TableState category = new TableState();
			category.makePresentation();
			category.setColumnHeadings(entry.getKey());

			final Collection<ItemDefinition> collections = entry.getValue();

			for( ItemDefinition itemdef : collections )
			{
				final List<FederatedSearch> fedSearches = federatedSearchService
					.getForCollectionUuid(itemdef.getUuid());

				final Wizard wizard = new Wizard();
				wizard.setDescription(itemdef.getDescription());

				if( !fedSearches.isEmpty() )
				{
					final List<RemoteRepository> repos = new ArrayList<RemoteRepository>();

					for( FederatedSearch fs : fedSearches )
					{
						if( !fs.isDisabled() )
						{
							final RemoteRepository repository = new RemoteRepository();
							repository.setName(fs.getName());

							final HtmlLinkState repolink = new HtmlLinkState();
							repolink.setLabel(new BundleLabel(fs.getName(), bundleCache));
							repolink.setEventHandler("click", events.getNamedHandler("remoteRepo", fs.getUuid()));
							repository.setStart(new LinkRenderer(repolink));

							repos.add(repository);

						}
					}
					Collections.sort(repos, new NumberStringComparator<RemoteRepository>()
					{
						private static final long serialVersionUID = 1L;

						@Override
						public String convertToString(RemoteRepository r)
						{
							return TextBundle.getLocalString(r.getName(), bundleCache, null, "");
						}
					});
					wizard.setRemoteRepos(repos);
				}

				// If there is only one collection and no remote repositories
				if( singleCol && Check.isEmpty(wizard.getRemoteRepos()) )
				{
					webWizardService.forwardToNewItemWizard(context, contributableCollections.get(0).getUuid(), null,
						null, true);
					return null;
				}

				final HtmlLinkState link = new HtmlLinkState();
				link.setLabel(new BundleLabel(itemdef.getName(), bundleCache));

				if( !itemdef.isDenyDirectContribution() ) // Allows Direct
				{
					link.setEventHandler("click", events.getNamedHandler("contribute", itemdef.getUuid()));
					wizard.setStart(new LinkRenderer(link));
					category.addRow(viewFactory.createResultWithModel("contribute/collectionCell.ftl", wizard));
				}
				else if( itemdef.isDenyDirectContribution() && !fedSearches.isEmpty() )
				{
					/*
					 * Only display non direct collection link if there is at
					 * least 1 remote repo available
					 */
					if( !wizard.getRemoteRepos().isEmpty() )
					{
						link.setDisabled(true);
						wizard.setStart(new LinkRenderer(link));
						category.addRow(viewFactory.createResultWithModel("contribute/collectionCell.ftl", wizard));
					}
				}
			}
			categories.add(category);
		}

		model.setCategories(categories);

		Decorations decorations = Decorations.getDecorations(context);
		decorations.setTitle(TITLE_LABEL);
		decorations.setContentBodyClass("contribution-selection-page");

		GenericTemplateResult temp = new GenericTemplateResult();
		temp.addNamedResult(OneColumnLayout.BODY, viewFactory.createResult("contribute/contribute.ftl", context));
		HelpAndScreenOptionsSection.addHelp(context, viewFactory.createResult("contribute/help.ftl", context));
		return temp;
	}

	@EventHandlerMethod
	public void contribute(SectionInfo info, String collectionUuid) throws Exception
	{
		webWizardService.forwardToNewItemWizard(info, collectionUuid, null, null, true);
	}

	@EventHandlerMethod
	public void remoteRepo(SectionInfo info, String fedUuid)
	{
		remoteRepoWebService.forwardToSearch(info, federatedSearchService.getByUuid(fedUuid), true);
	}

	@EventHandlerMethod
	public void resume(SectionInfo info, String wizardUuid)
	{
		webWizardService.forwardToLoadWizard(info, wizardUuid);
	}

	@EventHandlerMethod
	public void removeWizard(SectionInfo info, String wizardUuid)
	{
		wizardService.removeFromSession(info, wizardUuid, true);
	}

	private List<ItemDefinition> enumerateCreatable(SectionInfo info)
	{
		List<ItemDefinition> results = itemDefinitionService.enumerateCreateable();

		SelectionSession css = selectionService.getCurrentSession(info);
		if( css != null && !css.isAllContributionCollections() )
		{
			selectionService.filterFullEntities(results, css.getContributionCollectionIds());
		}

		return results;
	}

	@Override
	public Class<ContributeModel> getModelClass()
	{
		return ContributeModel.class;
	}

	public static class ContributeModel
	{
		private List<ResumableWizard> resumables;
		private List<TableState> categories;
		private boolean hideResumable;

		public List<ResumableWizard> getResumables()
		{
			return resumables;
		}

		public void setResumables(List<ResumableWizard> resumables)
		{
			this.resumables = resumables;
		}

		public List<TableState> getCategories()
		{
			return categories;
		}

		public void setCategories(List<TableState> categories)
		{
			this.categories = categories;
		}

		public boolean isHideResumable()
		{
			return hideResumable;
		}

		public void setHideResumable(boolean hideResumable)
		{
			this.hideResumable = hideResumable;
		}
	}

	public static class ResumableWizard
	{
		private Date startedDate;
		private Label collectionName;
		private HtmlLinkState resumeLink;
		private SectionRenderable removeLink;

		public Date getStartedDate()
		{
			return startedDate;
		}

		public void setStartedDate(Date startedDate)
		{
			this.startedDate = startedDate;
		}

		public Label getCollectionName()
		{
			return collectionName;
		}

		public void setCollectionName(Label collectionName)
		{
			this.collectionName = collectionName;
		}

		public HtmlLinkState getResumeLink()
		{
			return resumeLink;
		}

		public void setResumeLink(HtmlLinkState resumeLink)
		{
			this.resumeLink = resumeLink;
		}

		public SectionRenderable getRemoveLink()
		{
			return removeLink;
		}

		public void setRemoveLink(SectionRenderable removeLink)
		{
			this.removeLink = removeLink;
		}
	}

	public static class Wizard
	{
		private LanguageBundle description;
		private SectionResult start;
		private List<RemoteRepository> remoteRepos;

		public LanguageBundle getDescription()
		{
			return description;
		}

		public void setDescription(LanguageBundle description)
		{
			this.description = description;
		}

		public SectionResult getStart()
		{
			return start;
		}

		public void setStart(SectionResult start)
		{
			this.start = start;
		}

		public List<RemoteRepository> getRemoteRepos()
		{
			return remoteRepos;
		}

		public void setRemoteRepos(List<RemoteRepository> remoteRepos)
		{
			this.remoteRepos = remoteRepos;
		}
	}

	public static class RemoteRepository
	{
		private LanguageBundle name;
		private SectionResult start;

		public SectionResult getStart()
		{
			return start;
		}

		public void setStart(SectionResult start)
		{
			this.start = start;
		}

		public LanguageBundle getName()
		{
			return name;
		}

		public void setName(LanguageBundle name)
		{
			this.name = name;
		}
	}
}
