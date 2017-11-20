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

package com.tle.web.coursedefaults;

import java.text.ParseException;
import java.util.Date;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.settings.standard.CourseDefaultsSettings;
import com.tle.common.util.TleDate;
import com.tle.common.util.UtcDate;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Calendar;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author larry
 */
@SuppressWarnings("nls")
@NonNullByDefault
public class CourseDefaultsSettingsSection
	extends
		OneColumnLayout<CourseDefaultsSettingsSection.CourseDefaultsSettingsModel>
{
	@PlugKey("coursedefaults.title")
	private static Label TITLE_LABEL;
	@PlugKey("coursedefaults.settings.save.receipt")
	private static Label SAVE_RECEIPT_LABEL;

	/**
	 * Constraints on order of dates (ie start <= end) established by notAfter &
	 * notBefore parameters in the freemarker template.
	 */
	@Component(name = "sdt", stateful = false)
	private Calendar startDate;
	@Component(name = "edt", stateful = false)
	private Calendar endDate;
	@Component
	@PlugKey("settings.save.button")
	private Button saveButton;
	@Component
	@PlugKey("settings.clear.button")
	private Button clearButton;
	@Component(stateful = false)
	@PlugKey("portionrestrictions.checkbox")
	private Checkbox portionRestrictions;

	@EventFactory
	private EventGenerator events;

	@AjaxFactory
	private AjaxGenerator ajax;

	private static final String CLEAR_DIV = "clear";

	@Inject
	private ConfigurationService configService;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private CourseDefaultsSettingsPrivilegeTreeProvider securityProvider;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		final JSHandler updateClear = new OverrideHandler(
			ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("showClear"), CLEAR_DIV));

		startDate.setEventHandler(JSHandler.EVENT_CHANGE, updateClear);
		endDate.setEventHandler(JSHandler.EVENT_CHANGE, updateClear);

		clearButton.setClickHandler(events.getNamedHandler("clear"));
		saveButton.setClickHandler(events.getNamedHandler("save"));
	}

	/**
	 */
	@Override
	protected TemplateResult setupTemplate(RenderEventContext info)
	{
		securityProvider.checkAuthorised();
		if( !getModel(info).isLoaded() )
		{
			CourseDefaultsSettings settings = getCourseDefaultsSettings();
			portionRestrictions.setChecked(info, settings.isPortionRestrictionsEnabled());

			Date start = null;
			try
			{
				if( !Check.isEmpty(settings.getStartDate()) )
				{
					start = CourseDefaultsSettings.parseDate(settings.getStartDate());
				}
			}
			catch( ParseException pe )
			{
				// Ignore
			}
			if( start != null )
			{
				startDate.setDate(info, new UtcDate(start));
			}

			Date end = null;
			try
			{
				if( !Check.isEmpty(settings.getEndDate()) )
				{
					end = CourseDefaultsSettings.parseDate(settings.getEndDate());
				}
			}
			catch( ParseException pe )
			{
				// Ignore
			}
			if( end != null )
			{
				endDate.setDate(info, new UtcDate(end));
			}
			getModel(info).setLoaded(true);
		}
		else
		{
			// filthy, filthy hack to get around the clear button fudging things
			// up
			portionRestrictions.setChecked(info, portionRestrictions.isChecked(info));
		}
		checkForDates(info);

		return new GenericTemplateResult(viewFactory.createNamedResult(BODY, "coursedefaultssettings.ftl", this));
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(TITLE_LABEL);
		crumbs.addToStart(SettingsUtils.getBreadcrumb());
		checkForDates(info);
	}

	private CourseDefaultsSettings getCourseDefaultsSettings()
	{
		return configService.getProperties(new CourseDefaultsSettings());
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		saveSystemConstants(info);
		receiptService.setReceipt(SAVE_RECEIPT_LABEL);
	}

	@EventHandlerMethod
	public void clear(SectionInfo info)
	{
		startDate.clearDate(info);
		endDate.clearDate(info);
		getModel(info).setShowClearLink(false);
		getModel(info).setLoaded(true);
	}

	@EventHandlerMethod
	public void showClear(SectionInfo info)
	{
		checkForDates(info);
	}

	private void checkForDates(SectionInfo info)
	{
		CourseDefaultsSettingsModel model = getModel(info);
		boolean atLeastOneDateRendered = (startDate.isDateSet(info) || endDate.isDateSet(info));
		model.setShowClearLink(atLeastOneDateRendered);
	}

	private void saveSystemConstants(SectionInfo info)
	{
		final CourseDefaultsSettings courseDefaultsSettings = getCourseDefaultsSettings();
		courseDefaultsSettings.setStartDate(extractFormattedDateFromControl(info, startDate));
		courseDefaultsSettings.setEndDate(extractFormattedDateFromControl(info, endDate));
		courseDefaultsSettings.setPortionRestrictionsEnabled(portionRestrictions.isChecked(info));
		configService.setProperties(courseDefaultsSettings);

		getModel(info).setLoaded(false);
	}

	/**
	 * static because all variables are passed in , no access to members
	 * required.
	 * 
	 * @param info
	 * @param dateControl
	 * @return formatted string, or null if no date exists or cannot be
	 *         formatted.
	 */
	@Nullable
	private static String extractFormattedDateFromControl(SectionInfo info, Calendar dateControl)
	{
		TleDate controlDate = dateControl.getDate(info);
		String dateAsString = null;
		try
		{
			dateAsString = CourseDefaultsSettings
				.formatDateToPlainString(controlDate == null ? null : controlDate.toDate());
		}
		catch( IllegalArgumentException iae )
		{
			// plough on to return null
		}
		return dateAsString;
	}

	/**
	 * @return the startDate
	 */
	public Calendar getStartDate()
	{
		return startDate;
	}

	/**
	 * @return the endDate
	 */
	public Calendar getEndDate()
	{
		return endDate;
	}

	/**
	 * @return the saveButton
	 */
	public Button getSaveButton()
	{
		return saveButton;
	}

	/**
	 * @return the clearButton
	 */
	public Button getClearButton()
	{
		return clearButton;
	}

	public Checkbox getPortionRestrictions()
	{
		return portionRestrictions;
	}

	@Override
	public Class<CourseDefaultsSettingsModel> getModelClass()
	{
		return CourseDefaultsSettingsModel.class;
	}

	public static class CourseDefaultsSettingsModel extends OneColumnLayout.OneColumnLayoutModel
	{
		@Bookmarked
		private boolean loaded;
		@Bookmarked(stateful = false)
		private boolean showClearLink;

		public boolean isLoaded()
		{
			return loaded;
		}

		public void setLoaded(boolean loaded)
		{
			this.loaded = loaded;
		}

		public boolean isShowClearLink()
		{
			return showClearLink;
		}

		public void setShowClearLink(boolean showClearLink)
		{
			this.showClearLink = showClearLink;
		}

	}
}
