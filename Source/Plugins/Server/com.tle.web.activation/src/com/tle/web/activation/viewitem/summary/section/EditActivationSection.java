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

package com.tle.web.activation.viewitem.summary.section;

import java.util.Date;

import javax.inject.Inject;

import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.util.UtcDate;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.activation.filter.SelectCourseDialog;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Calendar;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.viewitem.summary.content.AbstractContentSection;
import com.tle.web.viewitem.summary.section.ItemSummaryContentSection;

@Bind
public class EditActivationSection extends AbstractContentSection<EditActivationSection.EditActivationModel>
{
	@PlugKey("editactivation.error.fromafterbefore")
	private static Label LABEL_ERROR_NONSENSICAL_DATES;
	@PlugKey("editactivation.error.untilpast")
	private static Label LABEL_ERROR_UNTIL_PAST;
	@PlugKey("editactivation.error.pendingpast")
	private static Label LABEL_ERROR_PENDING_PAST;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@TreeLookup
	private ItemSummaryContentSection summarySection;
	@TreeLookup
	private ShowActivationsSection activationsSection;
	@Inject
	private ActivationService activationService;
	@AjaxFactory
	private AjaxGenerator ajax;
	@Inject
	private CourseInfoService courseService;

	@Component(name = "fd")
	private Calendar fromDate;
	@Component(name = "ud")
	private Calendar untilDate;
	@Component
	@PlugKey("editactivation.cancel")
	private Button cancelButton;
	@Component
	@PlugKey("editactivation.save")
	private Button saveButton;

	@Component(name = "sc")
	@PlugKey("editactivation.changecourse")
	private Button selectCourse;
	@Inject
	@Component
	private SelectCourseDialog selectCourseDialog;

	public void doEdit(SectionInfo info, String uuid)
	{
		getModel(info).setActivationId(uuid);

		summarySection.setSummaryId(info, this);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		EditActivationModel model = getModel(context);
		ActivateRequest request = activationService.getRequest(model.getActivationId());
		if( model.getCourse() == null )
		{
			model.setCourse(request.getCourse());
		}
		if( model.getError() == null )
		{
			fromDate.setDate(context, new UtcDate(request.getFrom()).conceptualDate());
			untilDate.setDate(context, new UtcDate(request.getUntil()).conceptualDate());
		}
		boolean active = request.getStatus() == ActivateRequest.TYPE_ACTIVE;
		selectCourse.setDisabled(context, active);
		fromDate.setDisabled(context, active);
		return viewFactory.createResult("editactivation.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		cancelButton.setClickHandler(events.getNamedHandler("cancelEdit"));
		selectCourse.setClickHandler(selectCourseDialog.getOpenFunction());
		selectCourseDialog.setAjax(true);
		selectCourseDialog.setOkCallback(ajax.getAjaxUpdateDomFunction(tree, null,
			events.getEventHandler("courseChanged"), "courseajax"));
		saveButton.setClickHandler(events.getNamedHandler("save"));
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		EditActivationModel model = getModel(info);
		ActivateRequest request = activationService.getRequest(model.getActivationId());
		if( isValid(info, request) )
		{
			request.setCourse(courseService.getByUuid(model.getCourseId()));
			request.setFrom(fromDate.getDate(info).toDate());
			request.setUntil(untilDate.getDate(info).toDate());
			request.setUser(CurrentUser.getUserID());
			activationService.updateActivation(request);
			cancelEdit(info);
		}
		else
		{
			info.preventGET();
		}
	}

	private boolean isValid(SectionInfo info, ActivateRequest request)
	{
		Date today = new Date();
		EditActivationModel model = getModel(info);
		if( fromDate.getDate(info).after(untilDate.getDate(info)) )
		{
			// not actually possible but JUST IN CASE
			model.setError(LABEL_ERROR_NONSENSICAL_DATES);
			return false;
		}
		if( untilDate.getDate(info).toDate().before(today) )
		{
			model.setError(LABEL_ERROR_UNTIL_PAST);
			return false;
		}
		if( request.getStatus() == ActivateRequest.TYPE_PENDING && fromDate.getDate(info).toDate().before(today) )
		{
			model.setError(LABEL_ERROR_PENDING_PAST);
			return false;
		}
		return true;
	}

	@EventHandlerMethod
	public void courseChanged(SectionInfo info, String courseUuid)
	{
		getModel(info).setCourse(courseService.getByUuid(courseUuid));
	}

	@EventHandlerMethod
	public void cancelEdit(SectionInfo info)
	{
		summarySection.setSummaryId(info, activationsSection.getSectionObject());
	}

	@Override
	public Class<EditActivationModel> getModelClass()
	{
		return EditActivationModel.class;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new EditActivationModel();
	}

	public class EditActivationModel
	{
		@Bookmarked(name = "aid")
		private String activationId;
		private CourseInfo course;
		@Bookmarked(name = "cid")
		private String courseId;
		private Label error;

		public String getCourseId()
		{
			if( courseId == null && course != null )
			{
				return course.getUuid();
			}
			return courseId;
		}

		public void setCourseId(String courseId)
		{
			this.courseId = courseId;
		}

		public CourseInfo getCourse()
		{
			return course;
		}

		public void setCourse(CourseInfo course)
		{
			this.course = course;
			setCourseId(course.getUuid());
		}

		public String getActivationId()
		{
			return activationId;
		}

		public void setActivationId(String activationId)
		{
			this.activationId = activationId;
		}

		public Label getError()
		{
			return error;
		}

		public void setError(Label error)
		{
			this.error = error;
		}

	}

	public Calendar getFromDate()
	{
		return fromDate;
	}

	public Calendar getUntilDate()
	{
		return untilDate;
	}

	public Button getSelectCourse()
	{
		return selectCourse;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}
}
