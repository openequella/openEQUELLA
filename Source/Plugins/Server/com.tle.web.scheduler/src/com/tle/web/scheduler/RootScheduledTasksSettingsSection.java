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

package com.tle.web.scheduler;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.core.scheduler.SchedulerService.Schedules;
import com.tle.core.services.config.ConfigurationService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.layout.OneColumnLayout.OneColumnLayoutModel;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@Bind
@SuppressWarnings("nls")
public class RootScheduledTasksSettingsSection extends OneColumnLayout<OneColumnLayoutModel>
{
	@PlugKey("title")
	private static Label TITLE_LABEL;
	@PlugKey("save.receipt")
	private static Label SAVE_RECEIPT_LABEL;
	@PlugKey("hour.")
	private static String HOUR_KEY_PREFIX;
	@PlugKey("day.")
	private static String DAY_KEY_PREFIX;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Inject
	private ScheduledTasksPrivilegeTreeProvider securityProvider;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private ConfigurationService configService;

	@Component
	private SingleSelectionList<Integer> dailyTaskHour;
	@Component
	private SingleSelectionList<Integer> weeklyTaskDay;
	@Component
	private SingleSelectionList<Integer> weeklyTaskHour;

	@Component
	@PlugKey("save")
	private Button saveButton;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		dailyTaskHour.setListModel(new RangeWithKeyPrefixListModel(0, 23, HOUR_KEY_PREFIX));
		weeklyTaskDay.setListModel(new RangeWithKeyPrefixListModel(0, 6, DAY_KEY_PREFIX));
		weeklyTaskHour.setListModel(new RangeWithKeyPrefixListModel(0, 23, HOUR_KEY_PREFIX));

		saveButton.setClickHandler(events.getNamedHandler("save"));
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_BEFORE_EVENTS)
	public void checkAuthorised(SectionInfo info)
	{
		securityProvider.checkAuthorised();
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(TITLE_LABEL);
		crumbs.add(SettingsUtils.getBreadcrumb());
	}

	@Override
	protected TemplateResult setupTemplate(RenderEventContext info)
	{
		Schedules s = configService.getProperties(new Schedules());
		dailyTaskHour.setSelectedValue(info, s.getDailyTaskHour());
		weeklyTaskDay.setSelectedValue(info, s.getWeeklyTaskDay());
		weeklyTaskHour.setSelectedValue(info, s.getWeeklyTaskHour());

		return new GenericTemplateResult(viewFactory.createNamedResult(BODY, "settings.ftl", this));
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		Schedules s = new Schedules();
		s.setDailyTaskHour(dailyTaskHour.getSelectedValue(info));
		s.setWeeklyTaskDay(weeklyTaskDay.getSelectedValue(info));
		s.setWeeklyTaskHour(weeklyTaskHour.getSelectedValue(info));

		configService.setProperties(s);

		receiptService.setReceipt(SAVE_RECEIPT_LABEL);
	}

	public SingleSelectionList<Integer> getDailyTaskHour()
	{
		return dailyTaskHour;
	}

	public SingleSelectionList<Integer> getWeeklyTaskDay()
	{
		return weeklyTaskDay;
	}

	public SingleSelectionList<Integer> getWeeklyTaskHour()
	{
		return weeklyTaskHour;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	@Override
	public Class<OneColumnLayout.OneColumnLayoutModel> getModelClass()
	{
		return OneColumnLayoutModel.class;
	}

	private static class RangeWithKeyPrefixListModel extends SimpleHtmlListModel<Integer>
	{
		private final String keyPrefix;

		public RangeWithKeyPrefixListModel(int from, int to, String keyPrefix)
		{
			this.keyPrefix = keyPrefix;
			for( int i = from; i <= to; i++ )
			{
				add(i);
			}
		}

		@Override
		protected Option<Integer> convertToOption(Integer day)
		{
			return new KeyOption<Integer>(keyPrefix + day.intValue(), day.toString(), day);
		}
	}
}
