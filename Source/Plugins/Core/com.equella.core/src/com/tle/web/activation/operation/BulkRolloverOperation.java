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

package com.tle.web.activation.operation;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.util.LocalDate;
import com.tle.common.util.TleDate;
import com.tle.common.util.UtcDate;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.activation.workflow.OperationFactory;
import com.tle.core.activation.workflow.RolloverOperation;
import com.tle.core.guice.Bind;
import com.tle.core.guice.BindFactory;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.web.bulk.operation.BulkOperationExecutor;
import com.tle.web.bulk.operation.BulkOperationExtension;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.ReloadHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Calendar;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class BulkRolloverOperation extends AbstractPrototypeSection<Object> implements BulkOperationExtension
{
	private static final String BULK_VALUE = "rollover";

	@PlugKey("operation.rollover.current")
	private static String KEY_CURRENT;
	@PlugKey("operation.")
	private static String KEY_NAME;
	@PlugKey("opresults.status")
	private static String KEY_STATUS;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private ActivationService activationService;

	@Inject
	private CourseInfoService courseInfoService;
	@Inject
	private BundleCache bundleCache;

	@EventFactory
	private EventGenerator events;

	@Component(name = "c")
	private SingleSelectionList<CourseInfo> courses;
	@Component(name = "ce")
	private Checkbox cancelExisting;
	@Component(name = "fd")
	private Calendar fromDate;
	@Component(name = "td")
	private Calendar toDate;
	@Component(name = "rd")
	private Checkbox rolloverActivationDates;

	@BindFactory
	public interface RolloverExecutorFactory
	{
		RolloverActivationExecutor create(long courseId, @Assisted("from") Date from, @Assisted("until") Date until,
			@Assisted("cancel") boolean cancel, @Assisted("sameCourse") boolean sameCourse,
			@Assisted("rolloverDates") boolean rolloverDates);
	}

	public static class RolloverActivationExecutor implements BulkOperationExecutor
	{
		private final long courseId;
		private final Date from;
		private final Date until;
		private final boolean cancel;
		private final boolean sameCourse;
		private final boolean rolloverDates;

		@Inject
		private OperationFactory operationFactory;
		@Inject
		private ItemOperationFactory workflowFactory;

		@Inject
		public RolloverActivationExecutor(@Assisted long courseId, @Assisted("from") Date from,
			@Assisted("until") Date until, @Assisted("cancel") boolean cancel,
			@Assisted("sameCourse") boolean sameCourse, @Assisted("rolloverDates") boolean rolloverDates)
		{
			this.courseId = courseId;
			this.from = from;
			this.until = until;
			this.cancel = cancel;
			this.sameCourse = sameCourse;
			this.rolloverDates = rolloverDates;
		}

		@Override
		public WorkflowOperation[] getOperations()
		{
			RolloverOperation rollover = operationFactory.createRollover(courseId, from, until);
			rollover.setUseSameCourse(sameCourse);
			rollover.setRolloverDates(rolloverDates);
			rollover.setCancel(cancel);
			return new WorkflowOperation[]{rollover, workflowFactory.reindexOnly(true)};
		}

		@Override
		public String getTitleKey()
		{
			return "com.tle.web.activation.bulk.rollover.title";
		}
	}

	@Override
	public BeanLocator<BulkOperationExecutor> getExecutor(SectionInfo info, String operationId)
	{
		CourseInfo course = courses.getSelectedValue(info);
		boolean sameCourse = course == null;
		long courseId = sameCourse ? 0 : course.getId();
		boolean cancel = cancelExisting.isChecked(info);
		boolean rolloverDates = rolloverActivationDates.isChecked(info);

		Date from = toDate(fromDate.getDate(info));
		Date until = toDate(toDate.getDate(info));
		from = from == null ? new Date() : from;
		until = until == null ? new Date() : until;

		return new FactoryMethodLocator<BulkOperationExecutor>(RolloverExecutorFactory.class, "create", courseId, from,
			until, cancel, sameCourse, rolloverDates);
	}

	@Nullable
	private Date toDate(@Nullable TleDate date)
	{
		if( date == null )
		{
			return null;
		}
		// treat user's midnight 'today' as right now
		final LocalDate nowLocal = new LocalDate(new UtcDate(), CurrentTimeZone.get());
		if( date.conceptualDate().equals(nowLocal.conceptualDate()) )
		{
			return nowLocal.toDate();
		}
		// treat user's midnight 'tomorrow' as right now +24hours
		final LocalDate tomorrowLocal = nowLocal.addDays(1);
		if( date.conceptualDate().equals(tomorrowLocal.conceptualDate()) )
		{
			return tomorrowLocal.toDate();
		}

		return UtcDate.convertUtcMidnightToLocalMidnight(date, CurrentTimeZone.get()).toDate();
	}

	@Override
	public void addOptions(SectionInfo info, List<Option<OperationInfo>> opsList)
	{
		opsList
			.add(new KeyOption<OperationInfo>(KEY_NAME + BULK_VALUE, BULK_VALUE, new OperationInfo(this, BULK_VALUE)));
	}

	@Override
	public Label getStatusTitleLabel(SectionInfo info, String operationId)
	{
		return new KeyLabel(KEY_STATUS, new KeyLabel(KEY_NAME + operationId + ".title"));
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		CourseListModel courseListModel = new CourseListModel();
		courseListModel.setSort(true);
		courses.setListModel(courseListModel);
		courses.setAlwaysSelect(true);
		courses.setEventHandler(JSHandler.EVENT_CHANGE, events.getNamedHandler("updateDatesFromCourse"));
		courses.setDefaultRenderer("defaultdropdown");

		fromDate.setEventHandler(JSHandler.EVENT_CHANGE, new ReloadHandler());
		toDate.setEventHandler(JSHandler.EVENT_CHANGE, new ReloadHandler());
		rolloverActivationDates.setClickHandler(events.getNamedHandler("useExistingDates"));
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@EventHandlerMethod
	public void useExistingDates(SectionInfo info)
	{
		fromDate.setDisabled(info, rolloverActivationDates.isChecked(info));
		toDate.setDisabled(info, rolloverActivationDates.isChecked(info));
	}

	@Override
	public boolean validateOptions(SectionInfo info, String operationId)
	{
		return (rolloverActivationDates.isChecked(info))
			|| (fromDate.getDate(info) != null && toDate.getDate(info) != null);
	}

	@Override
	public boolean areOptionsFinished(SectionInfo info, String operationId)
	{
		return validateOptions(info, operationId);
	}

	@Override
	public boolean hasExtraOptions(SectionInfo info, String operationId)
	{
		return true;
	}

	@Override
	public SectionRenderable renderOptions(RenderContext context, String operationId)
	{
		return viewFactory.createResult("rolloveractivations.ftl", this);
	}

	@Nullable
	protected List<CourseInfo> listCourses(SectionInfo info)
	{
		List<CourseInfo> courseList = courseInfoService.enumerate();
		if( courseList.isEmpty() )
		{
			return null; // fatalError(context, null,
			// "activatecal.error.nocourses");
		}
		return courseList;
	}

	public class CourseListModel extends DynamicHtmlListModel<CourseInfo>
	{
		@Nullable
		@Override
		protected Iterable<CourseInfo> populateModel(SectionInfo info)
		{
			return listCourses(info);
		}

		@Override
		protected Option<CourseInfo> getTopOption()
		{
			return new KeyOption<CourseInfo>(KEY_CURRENT, "", null);
		}

		@Override
		protected Option<CourseInfo> convertToOption(SectionInfo info, CourseInfo course)
		{
			return new NameValueOption<CourseInfo>(new BundleNameValue(course.getName(), course.getUuid(), bundleCache),
				course);
		}
	}

	public SingleSelectionList<CourseInfo> getCourses()
	{
		return courses;
	}

	public Checkbox getCancelExisting()
	{
		return cancelExisting;
	}

	public Calendar getFromDate()
	{
		return fromDate;
	}

	public Calendar getToDate()
	{
		return toDate;
	}

	public Checkbox getrolloverActivationDates()
	{
		return rolloverActivationDates;
	}

	@Override
	public void prepareDefaultOptions(SectionInfo info, String operationId)
	{
		updateDatesFromCourse(info);
	}

	@EventHandlerMethod
	public void updateDatesFromCourse(SectionInfo info)
	{
		CourseInfo course = courses.getSelectedValue(info);
		UtcDate[] dates = activationService.getDefaultCourseDates(course);
		// set the default control values
		fromDate.setDate(info, dates[0]);
		toDate.setDate(info, dates[1]);
	}

	@Override
	public boolean hasExtraNavigation(SectionInfo info, String operationId)
	{
		return false;
	}

	@Override
	public Collection<Button> getExtraNavigation(SectionInfo info, String operationId)
	{
		return null;
	}

	@Override
	public boolean hasPreview(SectionInfo info, String operationId)
	{
		return false;
	}

	@Override
	public ItemPack runPreview(SectionInfo info, String operationId, long itemUuid)
	{
		return null;
	}

	@Override
	public boolean showPreviousButton(SectionInfo info, String opererationId)
	{
		return true;
	}
}
