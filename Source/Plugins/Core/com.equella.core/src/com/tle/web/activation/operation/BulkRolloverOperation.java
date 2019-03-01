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

import com.google.inject.assistedinject.Assisted;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.i18n.LangUtils;
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
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.web.activation.section.ActivationResultsDialog;
import com.tle.web.bulk.operation.BulkOperationExecutor;
import com.tle.web.bulk.operation.BulkOperationExtension;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.CourseSelectionList;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.ReloadHandler;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Calendar;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.Option;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class BulkRolloverOperation extends AbstractPrototypeSection<Object>
    implements BulkOperationExtension {
  private static final String BULK_VALUE = "rollover";
  private static final String EXISTING_COURSE_ID = "existing";

  @PlugKey("operation.rollover.current")
  private static String KEY_CURRENT;

  @PlugKey("operation.")
  private static String KEY_NAME;

  @PlugKey("opresults.status")
  private static String KEY_STATUS;

  @TreeLookup private ActivationResultsDialog activationResultsDialog;

  @ViewFactory private FreemarkerFactory viewFactory;

  @Inject private ActivationService activationService;

  @Inject private CourseInfoService courseInfoService;
  @Inject private BundleCache bundleCache;
  @Inject private InstitutionService institutionService;

  @EventFactory private EventGenerator events;

  @Inject
  @Component(name = "c")
  private CourseSelectionList courses;

  @Component(name = "ce")
  private Checkbox cancelExisting;

  @Component(name = "fd")
  private Calendar fromDate;

  @Component(name = "td")
  private Calendar toDate;

  @Component(name = "rd")
  private Checkbox rolloverActivationDates;

  @BindFactory
  public interface RolloverExecutorFactory {
    RolloverActivationExecutor create(
        long courseId,
        @Assisted("from") Date from,
        @Assisted("until") Date until,
        @Assisted("cancel") boolean cancel,
        @Assisted("sameCourse") boolean sameCourse,
        @Assisted("rolloverDates") boolean rolloverDates);
  }

  public static class RolloverActivationExecutor implements BulkOperationExecutor {
    private final long courseId;
    private final Date from;
    private final Date until;
    private final boolean cancel;
    private final boolean sameCourse;
    private final boolean rolloverDates;

    @Inject private OperationFactory operationFactory;
    @Inject private ItemOperationFactory workflowFactory;
    @Inject private InstitutionService institutionService;

    @Inject
    public RolloverActivationExecutor(
        @Assisted final long courseId,
        @Assisted("from") final Date from,
        @Assisted("until") final Date until,
        @Assisted("cancel") final boolean cancel,
        @Assisted("sameCourse") final boolean sameCourse,
        @Assisted("rolloverDates") final boolean rolloverDates) {
      this.courseId = courseId;
      this.from = from;
      this.until = until;
      this.cancel = cancel;
      this.sameCourse = sameCourse;
      this.rolloverDates = rolloverDates;
    }

    @Override
    public WorkflowOperation[] getOperations() {
      final RolloverOperation rollover = operationFactory.createRollover(courseId, from, until);
      rollover.setUseSameCourse(sameCourse);
      rollover.setRolloverDates(rolloverDates);
      rollover.setCancel(cancel);
      return new WorkflowOperation[] {rollover, workflowFactory.reindexOnly(true)};
    }

    @Override
    public String getTitleKey() {
      return "com.tle.web.activation.bulk.rollover.title";
    }
  }

  @Override
  public BeanLocator<BulkOperationExecutor> getExecutor(
      final SectionInfo info, final String operationId) {
    final CourseInfo course = getSelectedCourse(info);
    final boolean sameCourse = (course == null);
    final long courseId = sameCourse ? 0 : course.getId();
    final boolean cancel = cancelExisting.isChecked(info);
    final boolean rolloverDates = rolloverActivationDates.isChecked(info);

    final Date from = toDate(fromDate.getDate(info), new Date());
    final Date until = toDate(toDate.getDate(info), new Date());

    return new FactoryMethodLocator<BulkOperationExecutor>(
        RolloverExecutorFactory.class,
        "create",
        courseId,
        from,
        until,
        cancel,
        sameCourse,
        rolloverDates);
  }

  private CourseInfo getSelectedCourse(final SectionInfo info) {
    final CourseInfo course = courses.getSelectedValue(info);
    if (course != null && course.getCode().equals("existing")) {
      return null;
    }
    return course;
  }

  @Nullable
  private Date toDate(@Nullable final TleDate date, final Date defaultDate) {
    if (date == null) {
      return defaultDate;
    }
    // treat user's midnight 'today' as right now
    final LocalDate nowLocal = new LocalDate(new UtcDate(), CurrentTimeZone.get());
    if (date.conceptualDate().equals(nowLocal.conceptualDate())) {
      return nowLocal.toDate();
    }
    // treat user's midnight 'tomorrow' as right now +24hours
    final LocalDate tomorrowLocal = nowLocal.addDays(1);
    if (date.conceptualDate().equals(tomorrowLocal.conceptualDate())) {
      return tomorrowLocal.toDate();
    }

    return UtcDate.convertUtcMidnightToLocalMidnight(date, CurrentTimeZone.get()).toDate();
  }

  @Override
  public void addOptions(final SectionInfo info, final List<Option<OperationInfo>> opsList) {
    opsList.add(
        new KeyOption<OperationInfo>(
            KEY_NAME + BULK_VALUE, BULK_VALUE, new OperationInfo(this, BULK_VALUE)));
  }

  @Override
  public Label getStatusTitleLabel(final SectionInfo info, final String operationId) {
    return new KeyLabel(KEY_STATUS, new KeyLabel(KEY_NAME + operationId + ".title"));
  }

  @Override
  public void registered(final String id, final SectionTree tree) {
    super.registered(id, tree);
    courses.setEventHandler(
        JSHandler.EVENT_CHANGE, events.getNamedHandler("updateDatesFromCourse"));
    courses.setAlwaysSelect(true);
    courses.setListModel(new BulkRolloverCourseListModel());
    // This ensures that "Use activation's current course" appear at the top of the REST result set
    courses.setRenderOptions(
        new CourseSelectionList.CourseSelectionListAutocompleteDropdownRenderOptions(
            institutionService) {
          @Override
          public Map<String, Object> getParameters(final PreRenderContext info) {
            final Map<String, Object> params = super.getParameters(info);
            params.put(
                "topOption",
                new ObjectExpression(
                    "id", EXISTING_COURSE_ID, "text", CurrentLocale.get(KEY_CURRENT)));
            return params;
          }
        });
    fromDate.setEventHandler(JSHandler.EVENT_CHANGE, new ReloadHandler());
    toDate.setEventHandler(JSHandler.EVENT_CHANGE, new ReloadHandler());
    rolloverActivationDates.setClickHandler(events.getNamedHandler("useExistingDates"));
  }

  @Override
  public void register(final SectionTree tree, final String parentId) {
    tree.registerInnerSection(this, parentId);
  }

  @EventHandlerMethod
  public void useExistingDates(final SectionInfo info) {
    fromDate.setDisabled(info, rolloverActivationDates.isChecked(info));
    toDate.setDisabled(info, rolloverActivationDates.isChecked(info));
  }

  @Override
  public boolean validateOptions(final SectionInfo info, final String operationId) {
    return (rolloverActivationDates.isChecked(info))
        || (fromDate.getDate(info) != null && toDate.getDate(info) != null);
  }

  @Override
  public boolean areOptionsFinished(final SectionInfo info, final String operationId) {
    return validateOptions(info, operationId)
        && activationResultsDialog.getModel(info).isShowOptions();
  }

  @Override
  public boolean hasExtraOptions(final SectionInfo info, final String operationId) {
    return true;
  }

  @Override
  public SectionRenderable renderOptions(final RenderContext context, final String operationId) {
    return viewFactory.createResult("rolloveractivations.ftl", this);
  }

  public SingleSelectionList<CourseInfo> getCourses() {
    return courses;
  }

  public Checkbox getCancelExisting() {
    return cancelExisting;
  }

  public Calendar getFromDate() {
    return fromDate;
  }

  public Calendar getToDate() {
    return toDate;
  }

  public Checkbox getrolloverActivationDates() {
    return rolloverActivationDates;
  }

  @Override
  public void prepareDefaultOptions(final SectionInfo info, final String operationId) {
    updateDatesFromCourse(info);
  }

  @EventHandlerMethod
  public void updateDatesFromCourse(final SectionInfo info) {
    final CourseInfo course = courses.getSelectedValue(info);
    final UtcDate[] dates = activationService.getDefaultCourseDates(course);
    // set the default control values
    fromDate.setDate(info, dates[0]);
    toDate.setDate(info, dates[1]);
  }

  @Override
  public boolean hasExtraNavigation(final SectionInfo info, final String operationId) {
    return false;
  }

  @Override
  public Collection<Button> getExtraNavigation(final SectionInfo info, final String operationId) {
    return null;
  }

  @Override
  public boolean hasPreview(final SectionInfo info, final String operationId) {
    return false;
  }

  @Override
  public ItemPack runPreview(
      final SectionInfo info, final String operationId, final long itemUuid) {
    return null;
  }

  @Override
  public boolean showPreviousButton(final SectionInfo info, final String opererationId) {
    return true;
  }

  protected class BulkRolloverCourseListModel extends CourseSelectionList.CourseSelectionListModel {
    public BulkRolloverCourseListModel() {
      super(courseInfoService);
    }

    @Override
    public String getDefaultValue(final SectionInfo info) {
      return EXISTING_COURSE_ID;
    }

    @Override
    public CourseInfo getValue(final SectionInfo info, final String value) {
      if (value.equals(EXISTING_COURSE_ID)) {
        final CourseInfo course = new CourseInfo();
        course.setUuid(EXISTING_COURSE_ID);
        course.setName(LangUtils.createTempLangugageBundle(KEY_CURRENT));
        return course;
      }
      return super.getValue(info, value);
    }
  }
}
