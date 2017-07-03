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

package com.tle.web.viewitem.summary.filter;

import javax.inject.Inject;

import com.dytech.edge.exceptions.DRMException;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.settings.standard.AutoLogin;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.item.service.DrmService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.render.HideableFromDRMSection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.ItemNameLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.selection.SelectionService;
import com.tle.web.template.Decorations;
import com.tle.web.template.section.MenuSection;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.DRMFilter;
import com.tle.web.viewitem.I18nDRM;
import com.tle.web.viewitem.section.PathMapper.Type;
import com.tle.web.viewitem.section.RootItemFileSection;
import com.tle.web.viewitem.summary.content.AbstractContentSection;
import com.tle.web.viewitem.summary.section.ItemDetailsAndActionsSummarySection;
import com.tle.web.viewitem.summary.section.SummarySection;
import com.tle.web.viewitem.summary.sidebar.ItemDetailsGroupSection;
import com.tle.web.viewitem.summary.sidebar.MajorActionsGroupSection;
import com.tle.web.viewitem.summary.sidebar.MinorActionsGroupSection;
import com.tle.web.viewurl.UseViewer;
import com.tle.web.viewurl.ViewItemFilter;
import com.tle.web.viewurl.ViewItemResource;

//@formatter:off
/**
 * The following logic applies:
 * If item has DRM
 *     If you are a Guest
 *         If preview allowed on the DRM
 *             If you can't view the item (VIEW + DISCOVER)
 *                 1 Kick to login screen
 *             Else
 *                 2 Show the reject / preview screen (no Accept)
 *         Else
 *             3 Kick to login screen
 *     Else If you can't view the item (VIEW + DISCOVER)
 *         4 Throw an access denied exception
 *     Else
 *         5 Show the accept / reject / preview screen
 */
//@formatter:on
@SuppressWarnings("nls")
public class DRMFilterSection extends AbstractContentSection<DRMFilterSection.DRMFilterModel>
	implements
		ViewItemFilter,
		DRMFilter
{
	static
	{
		PluginResourceHandler.init(DRMFilterSection.class);
	}

	@PlugURL("scripts/drmlicense.js")
	private static IncludeFile INC_JS;
	private static JSCallable LICENSE_FUNC = new ExternallyDefinedFunction("drmLicenseResponse", INC_JS);

	@PlugKey("summary.sidebar.summary.termsofuse.title")
	private static Label LABEL_TITLE;
	@PlugKey("filter.drm.error.guestandnopreview")
	private static Label LABEL_NOPREVIEW;

	@Inject
	private DrmService drmService;
	@Inject
	private ConfigurationService configService;
	@Inject
	private SelectionService selectionService;

	@TreeLookup
	private RootItemFileSection rootFileSection;
	@TreeLookup
	private SummarySection summarySection;
	@TreeLookup
	private MajorActionsGroupSection majorActionsGroupSection;
	@TreeLookup
	private MinorActionsGroupSection minorActionsGroupSection;
	//This is bad. Real bad.
	@TreeLookup(mandatory = false)
	private ItemDetailsAndActionsSummarySection integItemDetailsSection;
	@TreeLookup(mandatory = false)
	private ItemDetailsGroupSection itemDetailsSection;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@Component
	private Button acceptButton;
	@Component
	private Button previewButton;
	@Component
	private Button rejectButton;

	// The following is actually used... I know... shit way of doing it.
	@Inject
	@Component
	DRMLicenseDialog licenseDialog;

	private final JSCallable hideReject = new ExternallyDefinedFunction("hideReject", INC_JS);

	public Button getAcceptButton()
	{
		return acceptButton;
	}

	public Button getPreviewButton()
	{
		return previewButton;
	}

	public Button getRejectButton()
	{
		return rejectButton;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		rejectButton.addClickStatements(ScriptStatement.HISTORY_BACK);
		previewButton.setClickHandler(events.getSubmitValuesFunction("preview"));
	}

	@Override
	public int getOrder()
	{
		return 0;
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		rootFileSection.addFilterMapping(Type.ALWAYS, this);
	}

	@Override
	public ViewItemResource filter(SectionInfo info, ViewItemResource resource)
	{
		ViewableItem<Item> viewableItem = resource.getViewableItem();
		boolean summary = resource.getViewer() == summarySection;
		ItemKey itemId = viewableItem.getItemId();

		boolean mustShow = resource.getBooleanAttribute(ViewItemResource.KEY_MUST_SHOW);
		boolean alwaysSkip = !viewableItem.isDRMApplicable() || !viewableItem.isItemForReal();
		if( alwaysSkip )
		{
			drmService.addPreviewItem(itemId);
		}
		if( alwaysSkip || (!summary && !viewableItem.getPrivileges().contains("VIEW_ITEM")) )
		{
			return resource;
		}
		DRMFilterModel model = getModel(info);
		if( drmService.havePreviewedThisSession(itemId) )
		{
			model.setSkip(true);
			return resource;
		}

		boolean previewable = previewable(info);
		boolean compositionAllowed = compositionAllowed(info);
		boolean inComposition = drmService.isReferredFromDifferentItem(info.getRequest(), itemId);

		// only skip the drm acceptance if its not a composition or they are
		// allowed
		if( (model.isSkip() && (previewable || isTransientAcceptances())) || (inComposition && compositionAllowed) )
		{
			drmService.addPreviewItem(itemId);
			return resource;
		}

		// Don't reshow the drm previewable for files inside of a package if its
		// allowed in compositions (see #5479)
		if( previewable && compositionAllowed && drmService.isReferredFromSamePackage(info.getRequest(), itemId) )
		{
			drmService.addPreviewItem(itemId);
			return resource;
		}

		if( drmService.requiresAcceptanceCheck(itemId, summary, inComposition) )
		{
			if( mustShow )
			{
				resource.setAttribute(ViewItemResource.KEY_PREFER_STREAM, true);
				return resource;
			}

			Item item = viewableItem.getItem();
			tryDRMAuthorisation(item);
			DrmSettings rights = drmService.requiresAcceptance(item, summary, inComposition);
			if( rights != null )
			{
				I18nDRM drm = new I18nDRM(rights);
				model.setCanpreview(drm.canPreview());
				model.setDrm(drm);
				model.setItemTitle(new ItemNameLabel(item, this.bundleCache));

				// apart from the SearchPrevNextButtons, we hide the contents of
				// the actions column
				showActionSections(info, false);

				// Tell the content section to render this DRMSection as a
				// priority over the item summary it would usually show
				itemSummaryContentSection.setSubsiduarySectionId(info, this);

				return new UseViewer(resource, summarySection);
			}
		}
		return resource;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( selectionService.getCurrentSession(context) == null && Decorations.getDecorations(context).isMenuHidden() )
		{
			CssInclude nomenucss = CssInclude.include(MenuSection.getHiddenMenuCSS()).make();
			acceptButton.getState(context).addPreRenderable(nomenucss);
		}

		// May throw AccessDenied if preview not allowed and user is Guest
		JSHandler previewHandler = events.getNamedHandler("preview");
		initAcceptButton(context, acceptButton, events.getNamedHandler("accept"), previewHandler);

		rejectButton.addReadyStatements(context, hideReject, rejectButton.getElementId(context));
		Decorations.getDecorations(context).setTitle(LABEL_TITLE);
		return viewFactory.createResult("viewitem/drm/license.ftl", this);
	}

	@Override
	public void initAcceptButton(SectionInfo info, Button acceptButton, JSHandler handler, JSHandler previewHandler)
	{
		if( !CurrentUser.isGuest() )
		{
			acceptButton.setClickHandler(info, isTransientAcceptances() ? previewHandler : handler);
			return;
		}

		if( previewable(info) )
		{
			acceptButton.setDisplayed(info, false);
			return;
		}

		// throw access denied since a guest can never accept an agreement
		throw new AccessDeniedException(LABEL_NOPREVIEW.getText());
	}

	@Override
	public JSCallable getLicenseFunction()
	{
		return LICENSE_FUNC;
	}

	/**
	 * @return true if both current user was autoLoggedIn and the transientDrm
	 *         flag in login settings is true, otherwise false.
	 */
	private boolean isTransientAcceptances()
	{
		return CurrentUser.wasAutoLoggedIn()
			&& configService.getProperties(new AutoLogin()).isTransientDrmAcceptances();
	}

	@EventHandlerMethod
	public void accept(SectionInfo info)
	{
		ViewableItem<Item> viewableItem = rootFileSection.getViewableItem(info);
		drmService.acceptLicense(viewableItem.getItem());
	}

	@EventHandlerMethod(preventXsrf = false)
	public void preview(SectionInfo info)
	{
		getModel(info).setSkip(true);
		ViewableItem<Item> viewableItem = rootFileSection.getViewableItem(info);
		drmService.addPreviewItem(viewableItem.getItemId());
	}

	private boolean previewable(SectionInfo info)
	{
		ViewableItem<Item> viewableItem = rootFileSection.getViewableItem(info);
		DrmSettings drm = viewableItem.getItem().getDrmSettings();
		return (drm != null && drm.isPreviewAllowed());
	}

	private boolean compositionAllowed(SectionInfo info)
	{
		ViewableItem<Item> viewableItem = rootFileSection.getViewableItem(info);
		DrmSettings drm = viewableItem.getItem().getDrmSettings();
		return (drm != null && !drm.isStudentsMustAcceptIfInCompilation());
	}

	private void tryDRMAuthorisation(Item item)
	{
		try
		{
			drmService.isAuthorised(item, CurrentUser.getUserState().getIpAddress());
		}
		catch( DRMException ex )
		{
			if( CurrentUser.isGuest() )
			{
				throw new AccessDeniedException(CurrentLocale.get("com.tle.web.viewitem.drmfilter.drmprotected"));
			}
			throw ex;
		}
	}

	/**
	 * set most actions in the major, and the entirety of the minor, actions to
	 * be hidden
	 * 
	 * @param info
	 */
	private void showActionSections(SectionInfo info, boolean show)
	{
		if( majorActionsGroupSection.getChildSectionIds(info) != null )
		{
			for( SectionId renda : majorActionsGroupSection.getChildSectionIds(info) )
			{
				if( renda instanceof HideableFromDRMSection )
				{
					((HideableFromDRMSection) renda).showSection(info, show);
				}
			}
		}
		if( integItemDetailsSection != null )
		{
			integItemDetailsSection.showSection(info, show);
		}
		if( itemDetailsSection != null )
		{
			itemDetailsSection.showSection(info, show);
		}
		minorActionsGroupSection.showSection(info, show);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "drm";
	}

	@Override
	public Class<DRMFilterModel> getModelClass()
	{
		return DRMFilterModel.class;
	}

	public static class DRMFilterModel
	{
		@Bookmarked(name = "s", legacyName = "skip")
		private boolean skip;

		private Label itemTitle;

		private I18nDRM drm;
		private boolean canpreview;

		public Label getItemTitle()
		{
			return itemTitle;
		}

		public void setItemTitle(Label itemTitle)
		{
			this.itemTitle = itemTitle;
		}

		public boolean isCanpreview()
		{
			return canpreview;
		}

		public void setCanpreview(boolean canpreview)
		{
			this.canpreview = canpreview;
		}

		public I18nDRM getDrm()
		{
			return drm;
		}

		public void setDrm(I18nDRM drm)
		{
			this.drm = drm;
		}

		public boolean isSkip()
		{
			return skip;
		}

		public void setSkip(boolean skip)
		{
			this.skip = skip;
		}
	}

	@Override
	public void setSkip(SectionInfo info, boolean b)
	{
		getModel(info).setSkip(b);
	}

	public boolean isPreviewing(SectionInfo info)
	{
		return getModel(info).isSkip();
	}
}
