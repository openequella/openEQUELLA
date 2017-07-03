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

package com.tle.web.search.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import com.dytech.edge.common.Constants;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.settings.standard.SearchSettings;
import com.tle.common.settings.standard.SearchSettings.SearchFilter;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.renderers.list.CheckListRenderer;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.template.section.HelpAndScreenOptionsSection;

/**
 * @author Aaron
 */
@NonNullByDefault
public class EditSearchFilterSection extends AbstractPrototypeSection<EditSearchFilterSection.EditSearchFilterModel>
	implements
		HtmlRenderer
{
	@PlugKey("settings.title")
	private static Label BREADCRUMB_LABEL;
	@PlugKey("settings.filter.edit.page.title")
	private static Label EDIT_FILTER_LABEL;
	@PlugKey("settings.filter.new.page.title")
	private static Label NEW_FILTER_LABEL;
	@PlugKey("settings.filter.save.success")
	private static Label SAVE_FILTER_SUCCESS_LABEL;

	private static final String NEW_FILTER_ID = "new"; //$NON-NLS-1$

	@Inject
	private MimeTypeService mimeService;
	@Inject
	private ConfigurationService configConstants;
	@Inject
	private ReceiptService receiptService;

	@TreeLookup
	private OneColumnLayout<?> rootSection;

	@Component
	private TextField name;
	@Component
	private MimeTypesList mimeTypes;
	@Component
	@PlugKey("settings.filter.button.ok")
	private Button saveButton;
	@Component
	@PlugKey("settings.filter.button.cancel")
	private Button cancelButton;

	@ViewFactory
	private FreemarkerFactory view;
	@EventFactory
	private EventGenerator events;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		saveButton.setClickHandler(events.getNamedHandler("save")); //$NON-NLS-1$
		cancelButton.setClickHandler(events.getNamedHandler("cancel")); //$NON-NLS-1$

		mimeTypes.setListModel(new DynamicHtmlListModel<MimeEntry>()
		{
			@Override
			protected Iterable<MimeEntry> populateModel(SectionInfo info)
			{
				return mimeService.searchByMimeType(Constants.BLANK, 0, -1).getResults();
			}

			@Override
			protected Option<MimeEntry> convertToOption(SectionInfo info, MimeEntry mime)
			{
				String desc = mime.getDescription();
				if( !Check.isEmpty(desc) )
				{
					desc = desc + " (" + mime.getType() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				else
				{
					desc = mime.getType();
				}
				return new NameValueOption<MimeEntry>(new NameValue(desc, mime.getType()), mime);
			}
		});
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_MODAL_LOGIC)
	public void checkModal(SectionInfo info)
	{
		final EditSearchFilterModel model = getModel(info);
		if( !Check.isEmpty(model.getFilterUuid()) )
		{
			rootSection.setModalSection(info, this);
		}
	}

	@Override
	@SuppressWarnings("nls")
	public SectionResult renderHtml(RenderEventContext context)
	{
		final EditSearchFilterModel model = getModel(context);
		final String filterId = model.getFilterUuid();
		final Label titleLabel = (filterId.equals(NEW_FILTER_ID) ? NEW_FILTER_LABEL : EDIT_FILTER_LABEL);
		model.setTitle(titleLabel);
		Decorations.getDecorations(context).setTitle(titleLabel);

		HtmlLinkState linkState = new HtmlLinkState(BREADCRUMB_LABEL);
		linkState.setClickHandler(events.getNamedHandler("cancel"));

		Breadcrumbs crumbs = Breadcrumbs.get(context);
		crumbs.add(linkState);

		GenericTemplateResult gtr = new GenericTemplateResult();
		gtr.addNamedResult(OneColumnLayout.BODY, view.createResult("settings/editfilter.ftl", this));
		HelpAndScreenOptionsSection.addHelp(context, view.createResult("settings/editfilterhelp.ftl", this));
		return gtr;
	}

	@SuppressWarnings("nls")
	private boolean validate(SectionInfo info, EditSearchFilterModel model)
	{
		boolean valid = true;
		model.setNameError(null);
		model.setMimeError(null);
		if( Check.isEmpty(name.getValue(info)) )
		{
			model.setNameError("settings.filter.errors.name");
			valid = false;
		}

		final List<MimeEntry> selectedMimeTypes = mimeTypes.getSelectedValues(info);
		if( Check.isEmpty(selectedMimeTypes) )
		{
			model.setMimeError("settings.filter.errors.mimetypes");
			valid = false;
		}

		return valid;
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		final EditSearchFilterModel model = getModel(info);
		if( validate(info, model) )
		{
			final SearchSettings settings = configConstants.getProperties(new SearchSettings());
			SearchFilter f = getFilter(settings, model.getFilterUuid());
			if( f == null )
			{
				f = new SearchFilter();
				f.setId(UUID.randomUUID().toString());
				settings.getFilters().add(f);
			}
			f.setName(name.getValue(info));
			f.setMimeTypes(new ArrayList<String>(mimeTypes.getSelectedValuesAsStrings(info)));
			configConstants.setProperties(settings);

			clear(info, model);
			receiptService.setReceipt(SAVE_FILTER_SUCCESS_LABEL);
		}
	}

	@EventHandlerMethod
	public void cancel(SectionInfo info)
	{
		final EditSearchFilterModel model = getModel(info);
		clear(info, model);
	}

	private void clear(SectionInfo info, EditSearchFilterModel model)
	{
		// removes all the bollocks from the URL
		name.setValue(info, null);
		mimeTypes.setSelectedStringValues(info, null);
		model.setNameError(null);
		model.setMimeError(null);
		model.setFilterUuid(null);
	}

	@Nullable
	private SearchFilter getFilter(SearchSettings settings, String filterUuid)
	{
		for( SearchFilter filter : settings.getFilters() )
		{
			if( filter.getId().equals(filterUuid) )
			{
				return filter;
			}
		}
		return null;
	}

	// called from SearchSettingsSection
	public void setFilter(SectionInfo info, @Nullable SearchFilter filter)
	{
		if( filter != null )
		{
			getModel(info).setFilterUuid(filter.getId());
			name.setValue(info, filter.getName());
			mimeTypes.setSelectedStringValues(info, filter.getMimeTypes());
		}
	}

	// called from SearchSettingsSection
	public void newFilter(SectionInfo info)
	{
		getModel(info).setFilterUuid(NEW_FILTER_ID);
	}

	@Override
	public Class<EditSearchFilterModel> getModelClass()
	{
		return EditSearchFilterModel.class;
	}

	public TextField getName()
	{
		return name;
	}

	public MimeTypesList getMimeTypes()
	{
		return mimeTypes;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public static class EditSearchFilterModel
	{
		@Bookmarked(name = "f")
		private String filterUuid;
		@Bookmarked(name = "en")
		private String nameError;
		@Bookmarked(name = "em")
		private String mimeError;
		private Label title;

		public String getFilterUuid()
		{
			return filterUuid;
		}

		public void setFilterUuid(String filterUuid)
		{
			this.filterUuid = filterUuid;
		}

		public String getNameError()
		{
			return nameError;
		}

		public void setNameError(String nameError)
		{
			this.nameError = nameError;
		}

		public String getMimeError()
		{
			return mimeError;
		}

		public void setMimeError(String mimeError)
		{
			this.mimeError = mimeError;
		}

		public Label getTitle()
		{
			return title;
		}

		public void setTitle(Label titleKey)
		{
			this.title = titleKey;
		}
	}

	public static class MimeTypesList extends MultiSelectionList<MimeEntry>
	{
		public MimeTypesList()
		{
			super();
			setDefaultRenderer("checklist"); //$NON-NLS-1$
		}

		@Override
		public void rendererSelected(RenderContext info, SectionRenderable renderer)
		{
			CheckListRenderer clrenderer = (CheckListRenderer) renderer;
			clrenderer.setAsList(true);
		}
	}
}
