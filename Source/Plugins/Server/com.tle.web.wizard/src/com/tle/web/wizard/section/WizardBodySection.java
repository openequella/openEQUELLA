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

package com.tle.web.wizard.section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.wizard.WizardException;
import com.tle.common.Check;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.EquellaButtonExtension;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.generic.CachedData.CacheFiller;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.AppendedLabel;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.template.Decorations;
import com.tle.web.wizard.WizardState;
import com.tle.web.wizard.impl.WizardCommand;
import com.tle.web.wizard.section.model.WizardBodyModel;
import com.tle.web.wizard.section.model.WizardBodyModel.Tab;

public class WizardBodySection extends WizardSection<WizardBodyModel> implements AjaxPageUpdate
{
	@EventFactory
	private EventGenerator events;
	@Inject
	private ConfigurationService configurationService;

	@PlugKey("navigation.warning")
	private static Label LABEL_WARNING;

	@Component
	@PlugKey("nav.prev")
	private Button previousButton;
	@Component
	@PlugKey("nav.next")
	private Button nextButton;

	private TabsAndCommandsHandler regHandler;
	@Inject
	private List<WizardCommand> commands;
	@Inject
	private List<SectionId> additionalActions;
	private SubmitValuesFunction commandExecuteFunc;

	@Override
	public String getDefaultPropertyName()
	{
		return "nav"; //$NON-NLS-1$
	}

	@SuppressWarnings("nls")
	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		commandExecuteFunc = events.getSubmitValuesFunction("command");
		regHandler = new TabsAndCommandsHandler();
		tree.addRegistrationHandler(regHandler);

		nextButton.setClickHandler(events.getNamedHandler("navigate", 1));
		previousButton.setClickHandler(events.getNamedHandler("navigate", -1));

		if( additionalActions != null )
		{
			tree.registerSections(additionalActions, id);
		}
	}

	@SuppressWarnings("nls")
	@Override
	public void addAjaxDivs(AjaxRenderContext context)
	{
		context.addAjaxDivs("wizard-pagelist-page");
		context.addAjaxDivs("wizard-navigation");
		context.addAjaxDivs("wizard-major-actions");
		context.addAjaxDivs("wizard-actions");
	}

	public List<SectionTab> getTabs(SectionInfo info)
	{
		WizardBodyModel model = getModel(info);
		return model.getDisplayableTabs().get(info, new CacheFiller<List<SectionTab>>()
		{
			@Override
			public List<SectionTab> get(SectionInfo ifo)
			{
				List<SectionTab> tabs = new ArrayList<SectionTab>();
				List<SectionTabable> tabables = regHandler.getAllImplementors(ifo);
				for( SectionTabable sectionTabable : tabables )
				{
					sectionTabable.addTabs(ifo, tabs);
				}
				return tabs;
			}
		});
	}

	private List<WizardCommand> getCommands(SectionInfo info)
	{
		final WizardBodyModel model = getModel(info);
		return model.getDisplayableCommands().get(info, new CacheFiller<List<WizardCommand>>()
		{
			@Override
			public List<WizardCommand> get(SectionInfo ifo)
			{
				List<WizardCommand> commandList = new ArrayList<WizardCommand>();
				List<SectionCommandable> commandables = regHandler.getCommandables(ifo);
				for( SectionCommandable commandable : commandables )
				{
					commandable.addCommands(ifo, commandList);
				}
				if( model.isStandardCommands() )
				{
					commandList.addAll(commands);
				}
				return commandList;
			}
		});
	}

	/**
	 * This must run with a priority lower than:
	 * {@link PagesSection#submit(SectionInfo)}.
	 * 
	 * @param info
	 * @param section
	 * @param data
	 */
	@EventHandlerMethod(priority = SectionEvent.PRIORITY_NORMAL)
	public void tab(SectionInfo info, String section, String data)
	{
		int i = 0;
		for( SectionTab tab : getTabs(info) )
		{
			if( tab.getSectionName().equals(section) && tab.getData().equals(data) )
			{
				changeTab(info, i);
				break;
			}
			i++;
		}
	}

	/**
	 * This must run with a priority lower than:
	 * {@link PagesSection#submit(SectionInfo)}.
	 * 
	 * @param info
	 * @param commandExec
	 * @param commandData
	 */
	@EventHandlerMethod(priority = SectionEvent.PRIORITY_NORMAL)
	public void command(SectionInfo info, String commandExec, String commandData)
	{
		WizardBodyModel model = getModel(info);
		List<WizardCommand> commandList = getCommands(info);
		WizardSectionInfo winfo = getWizardInfo(info);
		for( WizardCommand command : commandList )
		{
			if( command.getValue().equals(commandExec) && command.isEnabled(info, winfo) )
			{
				try
				{
					model.getDisplayableCommands().clear();
					model.getDisplayableTabs().clear();
					command.execute(info, winfo, commandData);
					return;
				}
				catch( Exception e )
				{
					SectionUtils.throwRuntime(e);
				}
			}
		}
	}

	@Override
	public Class<WizardBodyModel> getModelClass()
	{
		return WizardBodyModel.class;
	}

	@SuppressWarnings("nls")
	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		WizardBodyModel model = getModel(context);
		if( model.getDisplaySection() == null )
		{
			int currentTab = model.getCurrentTab();
			List<SectionTab> tabs = getTabs(context);
			// number of tabs may have changed (due to status scripting)
			int tabsize = tabs.size();
			if( tabsize != 0 )
			{
				if( currentTab >= tabsize )
				{
					currentTab = tabsize - 1;
					model.setCurrentTab(currentTab);
				}
			}
			else
			{
				throw new WizardException("There is no wizard setup for this collection");
			}
			SectionTab tab = tabs.get(currentTab);
			tab.setCurrent(true);
			tab.setEnabled(true);
			SectionTabable tabSection = tab.getTabSection();
			tabSection.setupShowingTab(context, tab);
			model.setDisplaySection(tabSection);
		}
		WizardSectionInfo winfo = getWizardInfo(context);
		WizardState state = winfo.getWizardState();
		SectionRenderable tabRenderable = renderSection(context, model.getDisplaySection());
		model.setSections(Collections.singletonList((SectionResult) tabRenderable));

		setupCommands(context);

		model.setAdditionalActions(SectionUtils.renderSectionIds(context, additionalActions, new ResultListCollector())
			.getResultList());

		if( model.isTabNavigation() )
		{
			List<Tab> tabs = setupTabs(context);
			int currentTabIndex = getCurrentTab(context);
			boolean goForward = canGoForward(context);
			boolean goBack = currentTabIndex > 0;
			nextButton.setDisplayed(context, goForward);
			previousButton.setDisplayed(context, goBack);

			if( state.getWizard().isShowPageTitlesNextPrev() )
			{
				if( goBack )
				{
					Label prevPageLabel = tabs.get(currentTabIndex - 1).getContent().getLabel();
					if( !Check.isEmpty(prevPageLabel.getText()) )
					{
						previousButton.setLabel(context, prevPageLabel);
					}
				}
				if( goForward )
				{
					Label nextPageLabel = tabs.get(currentTabIndex + 1).getContent().getLabel();
					if( !Check.isEmpty(nextPageLabel.getText()) )
					{
						nextButton.setLabel(context, nextPageLabel);
					}
				}
			}
		}
		else
		{
			previousButton.setDisplayed(context, false);
			nextButton.setDisplayed(context, false);
		}

		if( (state.isLockedForEditing() || state.isNewItem()) && !configurationService.isAutoTestMode() )
		{
			context.getBody().addEventStatements(JSHandler.EVENT_BEFOREUNLOAD, new ReturnStatement(LABEL_WARNING));
		}

		String cssClass = state.getWizard().getAdditionalCssClass();
		if( !Check.isEmpty(cssClass) )
		{
			Decorations decorations = Decorations.getDecorations(context);
			decorations.addContentBodyClasses(cssClass);
		}
		return viewFactory.createTemplateResult("wizard/body.ftl", context);
	}

	@SuppressWarnings("nls")
	private List<Tab> setupTabs(SectionInfo info)
	{
		List<Tab> tabLinks = new ArrayList<Tab>();
		for( SectionTab tab : getTabs(info) )
		{
			HtmlComponentState link = new HtmlComponentState();
			link.setDisabled(!tab.isEnabled() || tab.isCurrent());
			link.setLabel(tab.getName());
			JSHandler onclick = tab.getOnclick();
			if( onclick == null )
			{
				String property = tab.getSectionName();
				String data = tab.getData();
				onclick = events.getNamedHandler("tab", property, data);
			}
			if( tab.isInvalid() )
			{
				link.setLabel(AppendedLabel.get(link.getLabel(), new TextLabel(" *")));
			}
			if( tab.isEnabled() && !tab.isCurrent() )
			{
				link.setClickHandler(onclick);
			}
			tabLinks.add(new Tab(tab.isCurrent(), link, tab.getUniqueId()));
		}
		getModel(info).setTabs(tabLinks);
		return tabLinks;
	}

	@SuppressWarnings("nls")
	private void setupCommands(SectionInfo info)
	{
		List<HtmlComponentState> majorActions = new ArrayList<HtmlComponentState>();
		List<HtmlComponentState> minorActions = new ArrayList<HtmlComponentState>();
		List<HtmlComponentState> moreActions = new ArrayList<HtmlComponentState>();

		final WizardSectionInfo winfo = getWizardInfo(info);
		for( WizardCommand command : getCommands(info) )
		{
			if( command.isEnabled(info, winfo) )
			{
				HtmlComponentState link = new HtmlComponentState();
				JSHandler handler = command.getJavascript(info, winfo, commandExecuteFunc);
				if( handler == null )
				{
					handler = new OverrideHandler(commandExecuteFunc, command.getValue(), "");
					String warning = command.getWarning(info, winfo);
					if( warning != null )
					{
						handler.addValidator(new Confirm(new KeyLabel(warning)));
					}
				}
				link.setClickHandler(handler);
				link.setLabel(new KeyLabel(command.getName()));

				if( command.isMajorAction() )
				{
					link.setRendererType(EquellaButtonExtension.ACTION_BUTTON);
					link.addClasses(command.getStyleClass());
					majorActions.add(link);
				}
				else
				{
					if( command.addToMoreActionList() )
					{
						moreActions.add(link);
					}
					else
					{
						minorActions.add(link);
					}
				}
			}
		}

		WizardBodyModel model = getModel(info);
		model.setMajorActions(majorActions);
		model.setMinorActions(minorActions);
		model.setMoreActions(moreActions);
	}

	public void setCommands(List<WizardCommand> commands)
	{
		this.commands = commands;
	}

	@EventHandlerMethod
	public void navigate(SectionInfo info, int dir)
	{
		WizardBodyModel model = getModel(info);
		changeTab(info, model.getCurrentTab() + dir);
	}

	private void changeTab(SectionInfo info, int newTab)
	{
		WizardBodyModel model = getModel(info);
		List<SectionTab> tabs = getTabs(info);
		int oldTab = model.getCurrentTab();
		if( oldTab < tabs.size() )
		{
			SectionTab sectionTab = tabs.get(oldTab);
			sectionTab.getTabSection().leavingTab(info, sectionTab);
		}
		model.setCurrentTab(newTab);
	}

	public boolean canGoForward(SectionInfo info)
	{
		WizardBodyModel model = getModel(info);
		return (getTabs(info).size() - 1 > model.getCurrentTab());
	}

	public int getCurrentTab(SectionInfo info)
	{
		WizardBodyModel model = getModel(info);
		return model.getCurrentTab();
	}

	public boolean isSaveableApartFromCurrent(SectionInfo info)
	{
		boolean saveable = true;
		for( SectionTab tab : getTabs(info) )
		{
			if( (tab.isInvalid() && !tab.isCurrent()) || !tab.isEnabled() )
			{
				saveable = false;
				break;
			}
		}

		return saveable;
	}

	public boolean isSaveable(SectionInfo info)
	{
		for( SectionTab tab : getTabs(info) )
		{
			if( tab.isInvalid() || !tab.isEnabled() )
			{
				return false;
			}
		}
		return true;
	}

	public boolean goToFirstUnfinished(SectionInfo info)
	{
		WizardBodyModel model = getModel(info);
		List<SectionTab> tabs = getTabs(info);
		int current = 0;
		for( SectionTab tab : tabs )
		{
			if( tab.isInvalid() || !tab.isEnabled() )
			{
				tab.getTabSection().unfinishedTab(info, tab);
				model.setCurrentTab(current);
				return true;
			}
			current++;
		}
		return false;
	}

	public void setModalDisplay(SectionInfo info, SectionId sectionId, boolean showTabs, boolean standardCommands)
	{
		WizardBodyModel model = getModel(info);
		model.setDisplaySection(sectionId);
		model.setTabNavigation(showTabs);
		model.setStandardCommands(standardCommands);
	}

	public Button getPreviousButton()
	{
		return previousButton;
	}

	public Button getNextButton()
	{
		return nextButton;
	}

	public List<SectionId> getAdditionalActions()
	{
		return additionalActions;
	}

	public void setAdditionalActions(List<SectionId> additionalActions)
	{
		this.additionalActions = additionalActions;
	}

}
