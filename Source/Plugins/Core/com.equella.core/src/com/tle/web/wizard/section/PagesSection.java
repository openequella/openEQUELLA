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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.itemdef.Wizard;
import com.tle.beans.item.Item;
import com.tle.common.Check;
import com.tle.core.i18n.BundleCache;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.wizard.LERepository;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.ajax.JSONResponseCallback;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.AbstractDirectEvent;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.registry.handler.CollectInterfaceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.template.Decorations;
import com.tle.web.wizard.WebWizardPage;
import com.tle.web.wizard.WizardService;
import com.tle.web.wizard.WizardState;
import com.tle.web.wizard.page.AjaxUpdateData;
import com.tle.web.wizard.page.ControlResult;
import com.tle.web.wizard.page.PageUpdateCallback;

@NonNullByDefault
public class PagesSection extends WizardSection<PagesSection.PagesModel> implements SectionTabable, WizardStateListener
{
	@Inject
	private ItemHelper itemHelper;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private WizardService wizardService;

	@AjaxFactory
	private AjaxGenerator ajax;
	private JSCallable reloadFunction;

	private CollectInterfaceHandler<AjaxPageUpdate> pageUpdaters;

	@PlugKey("error.receipt.cause")
	private static String KEY_RECEIPT_CAUSE;

	@Override
	public String getDefaultPropertyName()
	{
		return "pages"; //$NON-NLS-1$
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		pageUpdaters = new CollectInterfaceHandler<AjaxPageUpdate>(AjaxPageUpdate.class);
		tree.addRegistrationHandler(pageUpdaters);
		reloadFunction = PageUpdateCallback.getReloadFunction(ajax.getAjaxFunction("reloadControls")); //$NON-NLS-1$
	}

	@Override
	public void addTabs(SectionInfo info, List<SectionTab> tabs)
	{
		WizardSectionInfo winfo = getWizardInfo(info);
		WizardState state = winfo.getWizardState();
		int i = 0;
		for( WebWizardPage page : getPages(state) )
		{
			if( page.isViewable() )
			{
				SectionTab tab = new SectionTab(this, new TextLabel(page.getPageTitle()), Integer.toString(i));

				Wizard wizard = winfo.getWizardState().getWizard();
				tab.setEnabled(page.isSubmitted() || wizard.isAllowNonSequentialNavigation());
				tab.setInvalid(!page.isValid());
				tabs.add(tab);
			}
			i++;
		}
	}

	@Override
	public void handleWizardState(SectionInfo info, final WizardState state)
	{
		PagesModel model = getModel(info);

		int renderedPage = model.getCurrentPage();
		if (renderedPage == -1)
		{
			renderedPage = 0;
		}
		final WebWizardPage page = getPage(info, renderedPage, false, model.getCurrentPage() != -1);
		if( model.isSubmit() )
		{
			info.queueEvent(new AbstractDirectEvent(SectionEvent.PRIORITY_AFTER_EVENTS, getSectionId())
			{
				@Override
				public void fireDirect(SectionId sectionId, SectionInfo info) throws Exception
				{
					final LERepository repository = page.getRepository();
					synchronized( repository.getThreadLock() )
					{
						page.saveToDocument(info);
						page.setSubmitted(true);
						itemHelper.updateItemFromXml(state.getItemPack());
						wizardService.checkPages(state);
						wizardService.updateSession(info, state);
					}
				}
			});
		}
	}

	@AjaxMethod
	public JSONResponseCallback reloadControls(AjaxRenderContext context, AjaxUpdateData controlData)
	{
		ReloadCallback callback = new ReloadCallback(context, controlData);
		for( AjaxPageUpdate updater : pageUpdaters.getAllImplementors(context) )
		{
			updater.addAjaxDivs(context);
		}
		return callback;
	}

	/**
	 * For multi-page wizards (ie, pretty much all wizards), we provide for a
	 * flag set in the wizard to determine that the Prev and Next buttons shall
	 * display the page titles (ie of the previous, and next pages) rather than
	 * simply 'Prev', 'Next'.
	 * 
	 * @param info
	 * @param pageNum
	 * @param load
	 * @param params
	 * @return
	 */
	protected WebWizardPage getPage(SectionInfo info, int pageNum, boolean load, boolean params)
	{
		WizardSectionInfo winfo = getWizardInfo(info);
		WizardState state = winfo.getWizardState();
		List<WebWizardPage> pages = getPages(state);
		if( pages.isEmpty() )
		{
			throw new Error("No pages in wizard");
		}
		WebWizardPage page = pages.get(pageNum);
		boolean doLoad = load;
		try
		{
			if( !page.isLoaded() )
			{
				page.setReloadFunction(reloadFunction);
				page.createPage();
				doLoad = true;
			}
			if( doLoad )
			{
				page.loadFromDocument(info);
				page.saveDefaults();
			}
			page.ensureTreeAdded(info, params);
			return page;
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	protected List<WebWizardPage> getPages(WizardState state)
	{
		Item item = state.getItem();
		if( item != null )
		{
			return wizardService.getWizardPages(state);
		}
		return Collections.emptyList();
	}

	@SuppressWarnings("nls")
	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		PagesModel model = getModel(context);
		WebWizardPage page = getPage(context, model.getCurrentPage(), true, false);
		Map<String, List<ControlResult>> results = page.renderPage(context, model.getUpdateData(), "wizard-controls");
		model.setPageTitle(page.getPageTitle());
		model.setPageResults(results);
		model.setSubmit(true);
		List<Label> errorList = Lists.newArrayList();
		for( HTMLControl control : page.getControls() )
		{
			if( control.isViewable() && control.getMessage() != null && (control.isMandatory() || control.isInvalid()) )
			{
				errorList.add(new KeyLabel(KEY_RECEIPT_CAUSE, new TextLabel(control.getTitle()),
					control.getMessage()));
			}
		}
		model.setErrors(errorList);

		String classes = page.getAdditionalCss();
		if( !Check.isEmpty(classes) )
		{
			Decorations.getDecorations(context).addContentBodyClasses(classes);
		}

		return viewFactory.createNamedResult("section_page", getFreemarkerTemplate(), context);
	}

	protected String getFreemarkerTemplate()
	{
		return "wizard/pagessection.ftl";
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new PagesModel();
	}

	@Override
	public void setupShowingTab(SectionInfo info, SectionTab tab)
	{
		PagesModel model = getModel(info);
		model.setCurrentPage(Integer.parseInt(tab.getData()));
	}

	public static class PagesModel
	{
		@Bookmarked(name = "pg", nodefault = true)
		private int currentPage = -1;
		// Redmine #6665 - to avoid having controls on a page reset when tree
		// nodes are opened on a popup
		@Bookmarked(name = "s", stateful = false)
		private boolean submit;
		private AjaxUpdateData updateData;
		private Map<String, List<ControlResult>> pageResults;
		private String pageTitle;
		private List<Label> errors;

		public List<Label> getErrors()
		{
			return errors;
		}

		public void setErrors(List<Label> errors)
		{
			this.errors = errors;
		}

		public int getCurrentPage()
		{
			return currentPage;
		}

		public void setPageTitle(String pageTitle)
		{
			this.pageTitle = pageTitle;
		}

		public String getPageTitle()
		{
			return pageTitle;
		}

		public void setCurrentPage(int currentPage)
		{
			this.currentPage = currentPage;
		}

		public Map<String, List<ControlResult>> getPageResults()
		{
			return pageResults;
		}

		public void setPageResults(Map<String, List<ControlResult>> pageResults)
		{
			this.pageResults = pageResults;
		}

		public boolean isSubmit()
		{
			return submit;
		}

		public void setSubmit(boolean submit)
		{
			this.submit = submit;
		}

		public AjaxUpdateData getUpdateData()
		{
			return updateData;
		}

		public void setUpdateData(AjaxUpdateData updateData)
		{
			this.updateData = updateData;
		}
	}

	public class ReloadCallback extends PageUpdateCallback
	{
		public ReloadCallback(AjaxRenderContext context, AjaxUpdateData reloadData)
		{
			super(context, reloadData);
			PagesModel model = getModel(context);
			model.setUpdateData(reloadData);
		}

		@Override
		public Map<String, List<ControlResult>> getPageResults()
		{
			return getModel(context).getPageResults();
		}
	}

	@Override
	public void leavingTab(SectionInfo info, SectionTab tab)
	{
		unfinishedTab(info, tab);
	}

	@Override
	public void unfinishedTab(SectionInfo info, SectionTab tab)
	{
		getPage(info, Integer.parseInt(tab.getData()), true, false).setShowMandatory(true);
	}

}
