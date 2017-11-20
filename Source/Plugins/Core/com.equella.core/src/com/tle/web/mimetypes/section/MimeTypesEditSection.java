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

package com.tle.web.mimetypes.section;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.mime.MimeEntry;
import com.tle.common.NameValue;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.common.beans.exception.ValidationError;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.institution.InstitutionService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.mimetypes.MimeTypeService.MimeEntryChanges;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.plugins.PluginTracker.ExtensionParamComparator;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.mimetypes.MimeEditExtension;
import com.tle.web.mimetypes.MimeEditorUtils;
import com.tle.web.mimetypes.MimeSearchPrivilegeTreeProvider;
import com.tle.web.mimetypes.model.MimeTypesEditModel;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.registry.handler.CollectInterfaceHandler;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TabLayout;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.model.TabSection;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class MimeTypesEditSection extends AbstractPrototypeSection<MimeTypesEditModel> implements HtmlRenderer
{
	private static PluginResourceHelper resources = ResourcesService.getResourceHelper(MimeTypesEditSection.class);
	public static final NameValue TAB_DETAILS = new BundleNameValue(resources.key("tab.details"), "details");
	public static final NameValue TAB_VIEWERS = new BundleNameValue(resources.key("tab.viewers"), "viewers");

	@PlugKey("edit.pagetitle")
	private static Label EDIT_TITLE_LABEL;
	@PlugKey("add.pagetitle")
	private static Label ADD_TITLE_LABEL;
	@PlugKey("edit.parentbreadcrumb.label")
	private static Label PARENT_BREADCRUMB_LABEL;
	@PlugKey("edit.parentbreadcrumb.title")
	private static Label PARENT_BREADCRUMB_TITLE;

	@Inject
	private MimeTypeService mimeService;
	@Inject
	private MimeSearchPrivilegeTreeProvider securityProvider;
	@Inject
	private InstitutionService institutionService;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Component
	private TabLayout tabs;
	@Component
	@PlugKey("button.save")
	private Button saveButton;
	@Component
	@PlugKey("mimetypes.button.cancel")
	private Button cancelButton;

	private PluginTracker<MimeEditExtension> extensions;
	private CollectInterfaceHandler<TabSection> tabSections;
	private CollectInterfaceHandler<MimeEntryEditSection> editors;
	private HtmlLinkState parentBreadcrumb;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		securityProvider.checkAuthorised();

		Decorations.getDecorations(context)
			.setTitle(getModel(context).getEditId() == 0 ? ADD_TITLE_LABEL : EDIT_TITLE_LABEL);

		Breadcrumbs breadcrumbs = Breadcrumbs.get(context);
		breadcrumbs.add(SettingsUtils.getBreadcrumb());
		breadcrumbs.add(parentBreadcrumb);

		final GenericTemplateResult result = new GenericTemplateResult();
		result.addNamedResult("body", viewFactory.createResult("mimeedit.ftl", context));
		result.addNamedResult("help", viewFactory.createResult("helponedit.ftl", context));
		return result;
	}

	@EventHandlerMethod
	public void save(final SectionInfo info)
	{
		MimeTypesEditModel model = getModel(info);
		try
		{
			mimeService.saveOrUpdate(model.getEditId(), new MimeEntryChanges()
			{
				@Override
				public void editMimeEntry(MimeEntry entry)
				{
					List<MimeEntryEditSection> editorList = editors.getAllImplementors(info);
					for( MimeEntryEditSection editor : editorList )
					{
						editor.saveEntry(info, entry);
					}
				}
			});
			goBack(info);
		}
		catch( InvalidDataException ve )
		{
			info.preventGET();
			ValidationError firstError = ve.getErrors().get(0);
			model.setErrorLabel(new KeyLabel(firstError.getKey(), firstError.getMessage()));
		}
	}

	@EventHandlerMethod
	public void cancel(SectionInfo info)
	{
		goBack(info);
	}

	private void goBack(SectionInfo info)
	{
		info.forwardToUrl(institutionService.institutionalise(MimeEditorUtils.MIME_BOOKMARK));
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		saveButton.setClickHandler(events.getNamedHandler("save"));
		cancelButton.setClickHandler(events.getNamedHandler("cancel"));

		parentBreadcrumb = new HtmlLinkState(new SimpleBookmark(MimeEditorUtils.MIME_BOOKMARK));
		parentBreadcrumb.setLabel(PARENT_BREADCRUMB_LABEL);
		parentBreadcrumb.setTitle(PARENT_BREADCRUMB_TITLE);

		// register plugin points
		tree.registerSections(extensions.getBeanList(), id);

		editors = new CollectInterfaceHandler<MimeEntryEditSection>(MimeEntryEditSection.class);
		tabSections = new CollectInterfaceHandler<TabSection>(TabSection.class);
		tree.addRegistrationHandler(editors);
		tree.addRegistrationHandler(tabSections);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		tabs.addTabSections(tabSections.getAllImplementors(tree));
	}

	@Override
	public Class<MimeTypesEditModel> getModelClass()
	{
		return MimeTypesEditModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "";
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public static void edit(SectionInfo info, long id)
	{
		SectionInfo forward = createEditUrl(info);
		MimeTypesEditSection mimeEditSection = forward.lookupSection(MimeTypesEditSection.class);
		mimeEditSection.editMimeType(forward, id);
		info.forwardAsBookmark(forward);
	}

	private void editMimeType(SectionInfo info, long id)
	{
		MimeEntry entry = mimeService.getEntryForId(id);
		getModel(info).setEditId(id);
		List<MimeEntryEditSection> editorList = editors.getAllImplementors(info);
		for( MimeEntryEditSection editor : editorList )
		{
			editor.loadEntry(info, entry);
		}
	}

	private static SectionInfo createEditUrl(SectionInfo info)
	{
		return info.createForward("/access/mimeedit.do");
	}

	public static void newEntry(SectionInfo info)
	{
		edit(info, 0);
	}

	public TabLayout getTabs()
	{
		return tabs;
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		extensions = new PluginTracker<MimeEditExtension>(pluginService, "com.tle.web.mimetypes", "editExtension",
			"id", new ExtensionParamComparator("order"));
		extensions.setBeanKey("class");
	}
}
