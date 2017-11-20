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

import java.util.List;

import javax.inject.Inject;

import org.java.plugin.registry.Extension;

import com.dytech.edge.wizard.WizardException;
import com.dytech.edge.wizard.WizardTimeoutException;
import com.google.inject.Provider;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.Check;
import com.tle.common.quota.exception.QuotaExceededException;
import com.tle.core.i18n.BundleCache;
import com.tle.core.item.service.ItemService;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.navigation.BreadcrumbService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.TwoColumnLayout;
import com.tle.web.sections.events.BeforeEventsListener;
import com.tle.web.sections.events.ReadyToRespondListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.registry.handler.CollectInterfaceHandler;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.selection.section.CourseListVetoSection;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.template.Decorations.MenuMode;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.wizard.WizardExceptionHandler;
import com.tle.web.wizard.WizardService;
import com.tle.web.wizard.WizardState;
import com.tle.web.wizard.impl.WizardCommand;
import com.tle.web.wizard.section.model.WizardForm;
import com.tle.web.workflow.tasks.ModerationService;

@NonNullByDefault
@SuppressWarnings("nls")
public class RootWizardSection extends TwoColumnLayout<WizardForm>
	implements
		BeforeEventsListener,
		ReadyToRespondListener,
		CourseListVetoSection
{
	protected static final PluginResourceHelper resources = ResourcesService.getResourceHelper(WizardCommand.class);

	@PlugKey("error.type.timedout")
	private static Label LABEL_TIMEOUTERROR;
	@PlugKey("error.type.quota")
	private static Label LABEL_QUOTAERROR;
	@PlugKey("error.type.other")
	private static Label LABEL_ERROR;

	@Inject
	private BundleCache bundleCache;
	@Inject
	private BreadcrumbService breadcrumbService;
	@Inject
	private WizardService wizardService;
	@Inject
	private ModerationService moderationService;
	@Inject
	private Provider<DefaultWizardSectionInfo> infoProvider;
	@Inject
	private ItemService itemService;

	private PluginTracker<WizardExceptionHandler> exceptionTracker;

	private CollectInterfaceHandler<WizardStateListener> stateListeners;

	@Override
	public Class<WizardForm> getModelClass()
	{
		return WizardForm.class;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		stateListeners = new CollectInterfaceHandler<WizardStateListener>(WizardStateListener.class);
		tree.addRegistrationHandler(stateListeners);
		super.registered(id, tree);
	}

	private WizardState initState(final SectionInfo info, final WizardState state)
	{
		final WizardForm model = getModel(info);
		state.setNoCancel(model.isNoCancel());

		wizardService.addToSession(info, state, true);
		model.setWizid(state.getWizid());
		return state;
	}

	@Override
	public void beforeEvents(SectionInfo info)
	{
		final WizardForm model = getModel(info);
		final String method = model.getMethod();
		WizardState state = null;
		if( !Check.isEmpty(method) )
		{
			model.setMethod(null);
			state = (WizardState) SectionUtils.dispatchToMethod(method, this, info);
		}
		else
		{
			final String wizid = model.getWizid();
			if( wizid != null )
			{
				state = wizardService.getFromSession(info, wizid);
			}
		}

		if( state == null )
		{
			if( info.isRendered() )
			{
				return;
			}
			throw new WizardException(new WizardTimeoutException(LABEL_TIMEOUTERROR.getText()));
		}

		final DefaultWizardSectionInfo winfo = infoProvider.get();
		info.setAttribute(WizardSectionInfo.class, winfo);
		info.setAttribute(ItemSectionInfo.class, winfo);
		winfo.setWizardState(state);

		List<WizardStateListener> listeners = stateListeners.getAllImplementors(info);
		for( WizardStateListener stateListener : listeners )
		{
			stateListener.handleWizardState(info, state);
		}
		// FIXME: save here??
	}

	@Override
	public void readyToRespond(SectionInfo info, boolean redirect)
	{
		final String wizid = getModel(info).getWizid();
		final WizardState state = wizardService.getFromSession(info, wizid);
		wizardService.updateSession(info, state);
	}

	public WizardState newitem(final SectionInfo info) throws Exception
	{
		final WizardForm model = getModel(info);
		final WizardState state = wizardService.newItem(model.getItemdefUuid());
		initState(info, state);
		state.setEditable(true);
		return state;
	}

	public void removeState(final SectionInfo info, final WizardState state)
	{
		final WizardForm model = getModel(info);
		wizardService.removeFromSession(info, model.getWizid(), false);
		final ItemKey itemId = state.getItemId();
		model.setWizid(null);
		model.setUuid(itemId.getUuid());
		model.setVersion(itemId.getVersion());
	}

	@Nullable
	public WizardState loaditem(final SectionInfo info) throws Exception
	{
		ItemKey key;
		final WizardForm model = getModel(info);
		if( moderationService.isModerating(info) )
		{
			key = moderationService.getCurrentTaskId(info);
		}
		else
		{
			key = getItemId(model.getUuid(), model.getVersion());
			String oldTaskId = info.getRequest().getParameter("taskId");
			if( oldTaskId != null )
			{
				moderationService.moderate(info, ModerationService.VIEW_METADATA, new ItemTaskId(key, oldTaskId), 0, 1);
				return null;
			}
		}
		final WizardState state = wizardService.loadItem(key, model.isEdit(), model.isRedraft());
		if( model.isNewversion() )
		{
			wizardService.newVersion(state);
		}
		return initState(info, state);
	}

	public WizardState cloneitem(final SectionInfo info) throws Exception
	{
		final WizardForm model = getModel(info);
		final ItemId key = getItemId(model.getUuid(), model.getVersion());

		WizardState newState;
		if( model.isEdit() )
		{
			// move
			newState = wizardService.moveItem(key, model.getItemdefUuid(), model.getTransform());
		}
		else
		{
			// clone
			WizardState oldState = wizardService.loadItem(key, false, model.isRedraft());
			newState = wizardService.cloneItem(oldState, model.getItemdefUuid(), model.getTransform(),
				model.isCloneAttachments());
		}

		return initState(info, newState);
	}

	private ItemId getItemId(String uuid, int version)
	{
		if( version == 0 )
		{
			return new ItemId(uuid, itemService.getLiveItemVersion(uuid));
		}
		else if( version == -1 )
		{
			return new ItemId(uuid, itemService.getLatestVersion(uuid));
		}
		return new ItemId(uuid, version);
	}

	@Override
	public SectionResult renderHtml(final RenderEventContext context)
	{
		final WizardForm model = getModel(context);
		if( model.getException() != null )
		{
			Throwable exception = model.getException();
			final String exceptionName = exception.getClass().getName();
			final List<Extension> extensions = exceptionTracker.getExtensions();
			for( final Extension extension : extensions )
			{
				if( extension.getParameter("exceptionClass").valueAsString().equals(exceptionName) )
				{
					final WizardExceptionHandler handler = exceptionTracker.getBeanByParameter(extension, "bean");
					return handler.handleException(context, exception);
				}
			}

			model.setReason(exception.getMessage());
			boolean isQuota = exception instanceof QuotaExceededException;
			boolean isTimeout = !isQuota && exception instanceof WizardTimeoutException;
			model.setShowErrorHelp(!isQuota);
			Decorations.getDecorations(context)
				.setTitle(isQuota ? LABEL_QUOTAERROR : (isTimeout ? LABEL_TIMEOUTERROR : LABEL_ERROR));

			GenericTemplateResult temp = new GenericTemplateResult();
			temp.addNamedResult("body", viewFactory.createResult("wizard/error.ftl", context));
			return temp;
		}

		context.getForm().setName("WizardForm");
		final WizardSectionInfo winfo = getWizardInfo(context);
		final WizardState state = winfo.getWizardState();
		final String wizid = model.getWizid();
		if( wizid != null )
		{
			wizardService.updateSession(context, state);
		}

		return super.renderHtml(context);
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		final String wizid = getModel(info).getWizid();
		WizardState state = wizardService.getFromSession(info, wizid);
		if( state.isEntryThroughEdit() )
		{
			crumbs.add(breadcrumbService.getSearchCollectionCrumb(info, state.getItemDefinition().getUuid()));

			crumbs.add(breadcrumbService.getViewItemCrumb(info, state.getItem()));

			decorations
				.setTitle(new KeyLabel(resources.key("breadcrumb." + state.getOperation().toString().toLowerCase())));
		}
		else
		{
			crumbs.add(breadcrumbService.getContributeCrumb(info));

			decorations.setTitle(new BundleLabel(state.getItemDefinition().getName(), bundleCache));
		}
		decorations.setMenuMode(MenuMode.COLLAPSED);
		decorations.addContentBodyClass("wizard-layout");
	}

	@Inject
	public void setPluginService(final PluginService pluginService)
	{
		exceptionTracker = new PluginTracker<WizardExceptionHandler>(pluginService, "com.tle.web.wizard",
			"exceptionHandler", null);
	}

	public void setException(SectionInfo info, Throwable cause)
	{
		getModel(info).setException(cause);
	}

	protected WizardSectionInfo getWizardInfo(SectionInfo info)
	{
		final WizardSectionInfo winfo = info.getAttributeForClass(WizardSectionInfo.class);
		if( winfo == null )
		{
			throw new Error("No WizardSectionInfo attribute in info");
		}
		return winfo;
	}
}
